#include <stdio.h>
#include "udp.h"
#include "mfs.h"
#include "msg.h"

#define SIZE_CR (1028)
#define SIZE_INODE (64)
#define SIZE_IMAP (64)
#define SIZE_DIR (4096)

int fd;

//struct
struct msg
{
	int type;
	int inum;
	char buffer[10000];
	int block;
};

struct inode
{
  int size;
  int type;
  int dp[14];
};

struct cr{
	int entry[256];
	int end;
};

struct imap_s{
	int seq[16];
};


struct cr cp;


int Init(char* file)
{
	fd = open(file, O_CREAT | O_RDWR, S_IRWXU);
	if(fd == -1) return -1;
	//if fd has content
	

	//or not 
	
	cp.entry[0] = SIZE_CR + SIZE_DIR + SIZE_INODE;
	for(int i = 1; i < 256; i++){
		cp.entry[i] = -1;
	}	
	cp.end = SIZE_CR + SIZE_DIR + SIZE_INODE + SIZE_IMAP;
	lseek(fd, cp.end, SEEK_SET);
	
	write(fd, (char *) &cp, SIZE_CR);
	fsync(fd);

	MFS_DirEnt_t db[128];
	strcpy(db[0].name, ".");
	db[0].inum = 0;
	strcpy(db[1].name, "..");
	db[1].inum = 0;
	for(int i = 2; i < 128; i++){
		db[i].inum = -1;
	}
	lseek(fd, SIZE_CR, SEEK_SET);
	write(fd, (char *) &db, SIZE_DIR);
	fsync(fd);

	struct inode node;
	node.size = SIZE_DIR;
	node.type = 0;
	node.dp[0] = SIZE_CR;
	for(int i = 1; i < 14; i++){
		node.dp[i] = -1;
	}
	lseek(fd, SIZE_CR + SIZE_DIR, SEEK_SET);
	write(fd, (char *)&node, SIZE_INODE);
	fsync(fd);


	struct imap_s imap;
	imap.seq[0] = SIZE_CR + SIZE_DIR;
	for(int i = 1;i < 16; i++){
		imap.seq[i] = -1;
	}
	lseek(fd, SIZE_CR + SIZE_DIR+SIZE_INODE, SEEK_SET);
	write(fd,(char *)&imap, SIZE_IMAP);
	fsync(fd);




	return 0;
}

int Lookup(int pinum, char* name)
{
	struct imap_s imap;
	int row = pinum / 16;
	int col = pinum % 16;
	int addr_imap = cp.entry[row];
	lseek(fd, addr_imap, SEEK_SET);
	read(fd, &imap, sizeof(struct imap_s));
	struct inode node;
	int temp = imap.seq[col];
	lseek(fd, temp, SEEK_SET);
	read(fd, &node, sizeof(struct inode));
	
	//access the datablock
	MFS_DirEnt_t* db;
	if(node.type != 0) return -1; 
	int temp_check = node.dp[0];
	lseek(fd, temp_check, SEEK_SET);
	read(fd, &db, SIZE_DIR);
	
	for(int i = 0; i< 128; i++){
		if(strcmp(db[i].name,name) == 0){
			return db[i].inum;
		}	
	}
	return -1;
}
int Stat()
{
	return 0;

}

int Write()
{
	
	return 0;

}

int Read()
{
	return 0;

}

int Creat()
{
	return 0;

}

int Unlink()
{

	return 0;
}

int Shutdown()
{
	fsync(fd);
	int code = close(fd);
	if(code >= 0) return 0;
	else return -1;
}

int imap[16];
#define BUFFER_SIZE (1000)
// server code
int main(int argc, char *argv[]) {
   // struct sockaddr_in addr1;
    int flag = -1;
	if(argc != 3) exit(1);
    int rc = Init(argv[2]);
    int port = atoi(argv[1]);
    // int port = 30000;
    //printf("Server working on port 000000000000000000: %d\n", port);
    int sd = UDP_Open(port);
    assert(sd > -1);
    while(1){
        struct sockaddr_in addr1;
	printf("server::waiting\n");
	struct msg mesg;
	flag = mesg.type;
        char message[BUFFER_SIZE];
	rc = UDP_Read(sd, &addr1, message, BUFFER_SIZE);
	printf("server:: read message [size:%d contents:(%s)]\n", rc, message);
	printf("type %d\n",flag);
	if (rc > 0){
		rc = UDP_Write(sd, &addr1, "nihao", 50);
		printf("server::reply\n");
	}
	flag = -1;
	switch(flag)
	{
		case 0:
			rc = Lookup(mesg.inum, mesg.buffer);
			printf("enter case0\n");
			if(rc >= 0)
			{
				printf("look up success\n");
				rc = UDP_Write(sd,&addr1,"0Success",8);
			}
			break;
		case 1:
			rc = Stat(mesg.inum,(MFS_Stat_t*) mesg.buffer);
			printf("enter case1\n");
			if(rc == 0)
			{
				rc = UDP_Write(sd,&addr1,"1Success",8);
			}
			break;
		case 2:
			rc = write(mesg.inum, mesg.buffer, mesg.block);
			
			printf("enter case2\n");
			if(rc == 0)
			{
				rc = UDP_Write(sd,&addr1,"2Success",8);
			}
			break;
		case 3:
			rc = Read(mesg.inum, mesg.buffer,mesg.block);
			printf("enter case3\n");
			if(rc == 0)
			{
				rc = UDP_Write(sd,&addr1,"3Success",8);
			}
			break;
		case 4:
			rc = Creat(mesg.inum,mesg.block, mesg.buffer);
			printf("enter case4\n");
			if(rc == 0)
			{
				rc = UDP_Write(sd,&addr1,"4Success",8);
			}
			break;
		case 5:
			rc = Unlink(mesg.inum, mesg.buffer);
			printf("enter case5\n");
			if(rc == 0)
			{
				rc = UDP_Write(sd,&addr1,"5Success",8);
			}
			break;
		case 6:
			printf("enter case6\n");
			rc = Shutdown();
			if(rc == 0)
			{
				//
				rc = UDP_Write(sd,&addr1,"6Success",8);
				exit(0);
			}
			break;
	
	}
    }
    printf("yesy\n");
    return 0; 
    
}
    


 

