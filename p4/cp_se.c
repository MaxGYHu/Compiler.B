#include <stdio.h>
#include "mfs.h"
#include "udp.h"
#include <stdint.h>

typedef struct msg{
        int type; //0=loopup 1= stat 2= write 3=read 4= creat 5= unlick 6= shutdown
        int inum;
        char buffer[10000];
        int block;

}msg;



struct cr
{
	int point[256];
	int end;
};

struct cr cp;

struct inode
{

  int size;
  int type;//directory = 0 regular = 1, 
  int db[14];// 14 direct pointers 4kb each

};


//int imap[16];

//struct cell* log; 
#define BUFFER_SIZE (4096)
#define SIZE_CR (1028)
#define SIZE_INODE (64)
#define SIZE_IMAP (1024)
#define SIZE_DIR (4096)

struct sockaddr_in addrSnd, addrRcv;


int fd;
int sd;
int rc;

MFS_DirEnt_t* creat_dir(int inum, int pinum)
{
	MFS_DirEnt_t * dir = malloc( 128*sizeof(MFS_DirEnt_t));
	for(int i = 0; i< 128; i++)
	{
		//to itself	
		if(i == 0 )
		{
		      	dir[i].inum = inum;
			strcpy(dir[i].name,".");
		}
			//partent inode num
		if(i == 1) 
		{
			
			dir[i].inum = pinum;
			strcpy(dir[i].name,"..");
		}
		else  dir[i].inum = -1;
	}
	return dir;
}

int Init(char* name)
{
	fd = open(name, O_CREAT | O_RDWR, S_IRWXU);
	if(fd == -1) return -1;
	//root directory
	//struct cr cp;
	lseek(fd, sizeof(cp), SEEK_SET);
	struct inode root; 
	root.size = 128*(sizeof(MFS_DirEnt_t));
	root.type = 0;
	//overlap the db,make it only has 1 db
	MFS_DirEnt_t* first_db = creat_dir(0,0);
	
	//assign the first db
	root.db[0] = SIZE_CR;
	//assign all other DB to -1
	for(int i = 1; i< 14; i++)
	{
		//
		root.db[i] = -1;
	}
	int* imap = malloc(16 *sizeof(int));
	//information ?
	//CR + DATA + INODE
	//position of inode
	imap[0] = SIZE_CR + BUFFER_SIZE;
	for(int i= 1; i< 16; i++)
	{
		imap[i] = -1;
	}

	//connect cr and piece inode
	
	cp.point[0] = SIZE_CR + BUFFER_SIZE + SIZE_INODE;
	// char message[BUFFER_SIZE];
	//set all other pointers in CR
	for(int i=1; i< 256; i++)
	{
		cp.point[i] = -1;
//		printf("**point[x]:%d\n",cp.point[i]);
	}
	//cr + datablock + inode + piece imap

	//end. 
	char* buffer[BUFFER_SIZE];
	//write cr
	cp.end = SIZE_CR + BUFFER_SIZE + SIZE_INODE + SIZE_IMAP;
	memcpy(buffer, &cp, SIZE_CR);
	write(fd, buffer, SIZE_CR);
	fsync(fd);
	//write db
	memcpy(buffer, &first_db, SIZE_CR);
	write(fd, buffer, BUFFER_SIZE);
	fsync(fd);
	//write inode
	memcpy(buffer, &root, SIZE_CR);
	write(fd, buffer, SIZE_INODE);
	fsync(fd);
	//write piece of imap
	memcpy(buffer, &imap, SIZE_CR);
	write(fd, buffer, 16*sizeof(struct inode));
	fsync(fd);
	return 0;
}




struct inode access_node(int addr, int col)
{
	struct inode node;
	lseek(fd,addr, SEEK_SET);
	//access the imap	
	int* temp = malloc(SIZE_IMAP);
	//or memcpy?
	read(fd,&temp, SIZE_IMAP);
	
	
	//access the inode
	//minus 1?
	addr = temp[col -1];
	if(addr == -1){
		node.type = -1;  
		return node;
	}
	lseek(fd, addr, SEEK_SET);
	read(fd,&node, SIZE_INODE);

	return node;


}

int Lookup(int pinum, char *name){
 	if(pinum < 0 || pinum > 4096) return -1; 
	int pindex= pinum / 16;
	int index = pinum % 16;
	//access the imap
	int addr = cp.point[pindex];
	if(addr == -1) return -1;
	//access and if allocation of size is correct 	

	struct inode node = access_node(addr, index);
	if(node.type == -1) return -1;

	//access the data block
	addr = node.db[0];
	
	if(addr == -1) return -1;
	MFS_DirEnt_t* list = malloc(SIZE_DIR); 	
	lseek(fd, addr, SEEK_SET);
	read(fd, &list, SIZE_DIR);

	for(int x; x < 128; x++)
	{
		
		MFS_DirEnt_t each = list[x];
		if(strcmp(each.name, name) == 0 )
		{
			return each.inum;	
		}
		if(each.inum == -1)
		{
			return -1;
			
		}
	}	
	return -1;
}





int Stat(int inum, MFS_Stat_t *m){
 	if(inum < 0 || inum > 4096) return -1; 
	int row = inum /16;
	int col = inum % 16;
	int addr = cp.point[row];
	if(addr == -1) return -1;
	//access and if allocation of size is correct 
	struct inode node = access_node(addr, col);
	if(node.type == -1) return -1;
 	m->type = node.type;
  	m->size = node.size;
	return 0;
}

int Write(int inum, char *buffer, int block){
  	
	// what if inum is directory and block > 1?
	// struct inodes inode;
  	
	if(inum < 0 || inum > 4096 || block < 0 || block >14)
	{
	  	return -1;
  	}
	int row = inum /16;
	int col = inum % 16;
	int addr = cp.point[row];
	// if(addr == -1) return -1;
	// access and if allocation of size is correct 

	struct inode node = access_node(addr, col);
	if(node.type == 0 && block > 1) return -1;
	//access the data block
	addr = node.db[block];
	
	if(addr == -1) return -1;
	write(addr,buffer, BUFFER_SIZE);
  	fsync(fd);
  	return 0;

}

int Read(int inum, char *buffer, int block)
{
	if(block < 0 || block > 14 || inum < 0 || inum > 4096) return -1;
	if(strlen(buffer) > 4096) return -1;
	int pindex= inum / 16;
	int index = inum % 16;
	// int* imap = &cr[pindex];
  	int addr = cp.point[pindex];
	if(addr == -1) return -1;
  	// struct inode node = *addr; 
  	
	struct inode node = access_node(addr, index);
  	
	if(node.type == -1) return -1;	
	addr = node.db[block];
  	read(addr,buffer, BUFFER_SIZE);
	// fsync(data);
  	return 0;
}


int Creat(int pinum, int type, char *name)
{

 	if(pinum < 0 || pinum > 4096) return -1; 
	int row= pinum / 16;
	int col = pinum % 16;
	int addr = cp.point[row];
	
	if(addr == -1) return -1;
  	struct inode node = access_node(addr, col);

 	//if its directory
	if(node.type != 0) return -1;	


	MFS_DirEnt_t* list;
	lseek(fd, addr, SEEK_SET);
	read(fd, &list, SIZE_DIR);
	for(int x= 2; x<128; x++)
	{
		
		MFS_DirEnt_t data = list[x];
		//struct inode log_node; 
		int inum = 0;
		if(strcmp(data.name,name) ==0 ) return 0;
		if( data.inum == -1)
		{
			memcpy( data.name, &name, 28);	
			inum = list[x -1].inum +1;	
			row = inum /16;
 			col = inum % 16;
			
			addr = cp.point[row];
			if(addr == -1) return -1;
			
			//it is the inode where we are going to change
  			struct inode neo = access_node(addr, col);
			addr = cp.point[row];
			if(addr == -1) return -1;
			//access and if allocation of size is correct 
			lseek(fd,addr, SEEK_SET);
			//temp new imap
			int* temp = malloc(sizeof(int)*16);
			char* buffer[BUFFER_SIZE];

			//if its file
			if(type == 1)
			{
				//size?
				neo.size = 14*4096;
				neo.type = 1;
				for(int l =0 ; l<14; l++ )
				{
					neo.db[l] = -1;
				}
				memcpy(buffer, &neo, SIZE_INODE);
				write(fd, buffer, SIZE_INODE);
				fsync(fd);	
				temp[col -1] = cp.end;
				
				memcpy(buffer, &temp, SIZE_IMAP);
				write(fd, buffer, SIZE_IMAP);
				fsync(fd);	
				cp.end = cp.end + SIZE_INODE + SIZE_IMAP;
				//add into the imap
			//new piecec of inode
  			//update ?
			}
				//if its direc
			if(type == 0)
			{

				//create the first data block
				neo.size = 128*sizeof(MFS_DirEnt_t);
				neo.type = 0;
				MFS_DirEnt_t* list = creat_dir(inum, pinum);
				for(int i =1; i< 14; i++)
				{
					neo.db[i] = -1;
				}
				memcpy(buffer, &list, BUFFER_SIZE);
				write(fd, buffer, BUFFER_SIZE);
				fsync(fd);	
				neo.db[0] = cp.end;
				memcpy(buffer, &neo, SIZE_INODE);
				write(fd, buffer,SIZE_INODE);
				//New imap
				temp[col - 1] = cp.end + BUFFER_SIZE;
				cp.end = cp.end + BUFFER_SIZE + SIZE_INODE + SIZE_IMAP;
				memcpy(buffer, &temp, SIZE_IMAP);
				write(fd, buffer, SIZE_IMAP);
				fsync(fd);
			}
			
			return 0;

			}
	}

		
		

	return -1;
}


int Unlink(int pinum, char *name){
 	if(pinum < 0 || pinum > 4096) return -1; 
	int row= pinum / 16;
	int col = pinum % 16;
	int addr = cp.point[row];
	if(addr == -1) return -1;
	

	struct inode node = access_node(addr, col);
	if(node.type != 0) return -1;
	//type
	addr = node.db[0];
	
	if(addr == -1) return -1;
	MFS_DirEnt_t* list = malloc(SIZE_DIR); 	
	lseek(fd, addr, SEEK_SET);
	read(fd, &list, SIZE_DIR);



	for(int x = 2; x < 128; x++)
	{
		int itself = list[x].inum; 
		if(strcmp(list[x].name, name) == 0 )
		{	
			//check if its dire
			int row2= itself / 16;
			int col2 = itself % 16;


			int addr2 = cp.point[row2];
 			if(addr2 == -1) return -1;
			struct inode node2 = access_node(addr,col);
			if(node2.type == -1 ) return -1;
			
			
			//its dire
			
			if(node2.type == 0)
			{
				
				int block_p= node2.db[2];
				lseek(fd,block_p,SEEK_SET);

				MFS_DirEnt_t* third;
				read(fd, &third, BUFFER_SIZE);
				//check if its emppty
				if(third[2].inum != -1) return -1;
			
			}
			//new datablock neo							
			MFS_DirEnt_t* neo = malloc(SIZE_DIR);
			neo[x].inum = -1;
			int datablock = cp.end;
			char* buffer[BUFFER_SIZE];
			memcpy(buffer, &neo, SIZE_DIR);
			write(fd, buffer, SIZE_DIR);
				
			//inode points to new db
			node2.db[col2 - 1] = datablock;
			memcpy(buffer, &node2, SIZE_INODE);
			write(fd, buffer,SIZE_INODE);
			
			//new imap
			int* imap;
			lseek(fd,row2, SEEK_SET);
			read(fd, &imap,SIZE_IMAP);
			imap[col2 -1] = -1;

			memcpy(buffer, &imap, SIZE_IMAP);
			write(fd,buffer,SIZE_IMAP);
			//new inode



			//change the inode in pinum 
			
			
			//the node2 	
					 
			//write inode maybe

			return list[x].inum;	
		}
		if(list[x].inum == -1)
		{
			//write here maybe? its update as well.
			return -1;
			
		}
	}	
	return -1;
}

int Shutdown(){
	//fsyc()
	printf("no\n");
	exit(0);
	return 0; 
}


void print()
{
	for(int i=0; i< 256; i++)
	{

	//	printf("addr: %d ",);
		int addr = cp.point[i];
		printf("addr: %d ",addr);
		lseek(fd,addr, SIZE_IMAP);
		int* here = malloc(sizeof(int)*16);
		read(fd, &here, sizeof(int)*16);
		for(int x= 0;x<16; x++)
		{
			printf("%d, ",here[x]);
				
		}

	}

}




int main(int argc, char* argv[])
{

/*	if(argv[3] != NULL){
	       	exit(1);
	}
	if(argc != 3)
	{
		exit(1);
	}
	int port = atoi(argv[1]);
	int temp = Init(argv[2]);
//	print();
	printf("temp:%d\n",temp);
	printf("srever is waiting\n");
	
	do
	{
		
		//sd = UDP_Open(30001);
		sd = UDP_Open(port);
		port++;

	}
	while(sd < -1);
	printf("our wokring port: %d\n",port--);	

*/
/*	
	while(1){
		
		// msg fromClient;
		struct msg fromClient;
		struct sockaddr_in addr;
		char message[BUFFER_SIZE];
		printf("before rc\n");
		int rc = UDP_Read(sd, &addr, message, BUFFER_SIZE);
		printf("after rc\n");
		if(rc > 0){
			printf("enter if\n");
			int tmp;
			printf("type:%d\n",fromClient.type);
			if(fromClient.type == 0){
			       	printf("1\n");
				tmp = Lookup(fromClient.inum,fromClient.buffer);
				if(tmp != -1){
			        char reply[BUFFER_SIZE];
				sprintf(reply, "Lookup::success\n");
				rc = UDP_Write(sd, &addr, reply, BUFFER_SIZE);
				printf("server:: reply\n");
				}
			}
  			if(fromClient.type == 1)
			{
				//MFS_Stat_t m;
				//memcpy(&m, fromClient.buffer, sizeof(MFS_Stat_t));
			       
			       	printf("2\n");
				tmp = Stat(fromClient.inum, (MFS_Stat_t*) fromClient.buffer);
				if(tmp == 0)
				{
          			char reply[BUFFER_SIZE];
              			sprintf(reply, "Stat::success\n");
              			rc = UDP_Write(sd, &addr, reply, BUFFER_SIZE);
              				printf("server:: reply\n");
				}
			}
			if(fromClient.type == 2){
			      	tmp = Write(fromClient.inum, fromClient.buffer, fromClient.block);
			       	printf("3\n");
				if(tmp == 0){
              			char reply[BUFFER_SIZE];
              			sprintf(reply, "Write::success\n");
              			rc = UDP_Write(sd, &addr, reply, BUFFER_SIZE);
              			printf("server:: reply\n");
				}
			}
			if(fromClient.type == 3){
			       	tmp = Read(fromClient.inum, fromClient.buffer, fromClient.block);
			       	printf("4\n");
				if(tmp == 0){
 		                char reply[BUFFER_SIZE];
              			sprintf(reply, "Read::success\n");
              			rc = UDP_Write(sd, &addr, reply, BUFFER_SIZE);
              			printf("server:: reply\n");
				}
			}
			if(fromClient.type == 4){
			       	tmp = Creat(fromClient.inum, fromClient.block, fromClient.buffer);
			       	printf("5\n");
				if(tmp == 0){
              			char reply[BUFFER_SIZE];
              			sprintf(reply, "Creat::success\n");
              			rc = UDP_Write(sd, &addr, reply, BUFFER_SIZE);
              			printf("server:: reply\n");
				}
			}
	 		if(fromClient.type == 5){
			       	tmp = Unlink(fromClient.inum, fromClient.buffer);
			       	printf("6\n");
				if(tmp == 0){
              			char reply[BUFFER_SIZE];
              			sprintf(reply, "Unlink::success\n");
              			rc = UDP_Write(sd, &addr, reply, BUFFER_SIZE);
              			printf("server:: reply\n");
				}
			}
			if(fromClient.type == 6){
			       	tmp = Shutdown();
			       	printf("7\n");
				if(tmp == 0){ // necessary ???
              			char reply[BUFFER_SIZE];
              			sprintf(reply, "Shutdown::success\n");
              			rc = UDP_Write(sd, &addr, reply, BUFFER_SIZE);
              			printf("server:: reply\n");
				}
				}
			}
		}
*/

	int a = UDP_Open(30001);
	struct sockaddr_in addrSnd;
	while(1)
	{
		struct msg mes;
		rc = UDP_Read(a, &addrSnd,(char*)&mes, BUFFER_SIZE);
		//mes = (struct msg*) buffer;
		// memcpy(&mes,(struct msg*)buffer,sizeof(msg*));
		
		printf("---------%d\n",mes.type);
		//if(rc != -1) break;
	}

	return 0;
}
