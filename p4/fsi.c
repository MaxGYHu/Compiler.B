#include <stdio.h>
#include "udp.h"
#include "fsi.h"
#include "msg.h"

void* cr; 
struct inode{
  int size;
  int type;//directory = 0 regular = 1, 
  void* db[14];// 14 direct pointers 4kb each

};

struct inode* imap;

struct cell{
	int db;
	struct inode ip;
	struct inode* piece;

}
//struct cell* log; 
#define BUFFER_SIZE (4096)


struct sockaddr_in addrSnd, addrRcv;


int sd;
int rc;

int inodes[4096]; // the inum's size is 4096



int inode_info(int inum, inode *inode){
  // the size of inum from 0 - 4096, make sure the indeo is valid
  if(inum < 0 || inum >4096 || indeos[inum] == -1){
    return -1; // invaild inode 
  }  
  lseek(sd,inodes[inum],SEEL_SET);
  // The file offset is set to offset bytes.
  rc = read(sd,inode,sizeof(inode_s));
  printf("return code is %d, and inode size is %d, inode type is %d",rc,inode->size, inode->type);
  return rc;
}


int Lookup(int pinum, char *name){
	if(pinum > 4096) return -1;
	void* addr = imap[pinum];
	struct inode curr = &addr;
	if(curr.type != 0) return -1;
	for(int i = 0; i< 14; i++)
	{
		struct MFS_DirEnt_t* addr_db = curr.db[i];
		int x = 0;
		while(x < 128)
		{
			if(strcmp(addr_db[x].name, name) == 0 )
			{
				return addr_db[x].inum;	
			}
			if(addr_db[x] == NULL){
				return -1;
			
			}
		}
	}	
	return -1;
}



int Stat(int inum, MFS_Stat_t *m){
  if(inum > 4096) return -1;
  struct inode cur = imap[inum-1];
  if(cur == NULL) return -1;
  m->type = curr.type;
  m->size = curr.size;
  return 0;
}

int Write(int inum, char *buffer, int block){
  struct inodes inode;
  struct sockaddr_in addr;
  if(input ->type != 0 || inum < 0 || inum > 4096 || block < 0 || blokc >14){
	  return -1;
  }
  int piece = inum / 16;
  if(imap[piece *16] == -1) return -1;
  int offset = inum % 16;
  if(imap[piece * 16 + offest] == -1) return -1;
  lseek(imap, piece*16+offest,SEEK_SET);
  char message[BUFFER_SIZE];
  read( ,message,block);
  data = inode.data[block];
  write(inum,buffer,data);
  return 0;
}

int Read(int inum, char *buffer, int block){
	if(block < 0 || block > 14 || inum < 0 || inum > 4096) return -1;
	if(strlen(buffer) > 4096) return -1;
 	int pec_num = inum/16;
	struct inode* piece = imap;
	lseek(piece,pec_num,sizeof(inode)*16);
	int exist = piece; 
	if(exit == -1) return -1;
	int pos = inum % 16;
	exist = piece; 
	if(exit == -1) return -1;
	struct inode curr = piece[pos];
	read(curr.db[block], buffer, BUFFER_SIZE);
	return 0;
}


int Creat(int pinum, int type, char *name){
	if(pinum > 4096) return -1;	
	void* addr = imap[pinum];
	struct inode curr = &addr;
	//if its directory
	//creat all struct MSF_DirEnt_t?
/*	if(type == 1)
	{
			
	}
*/
       	for(int i = 0; i< 14; i++)
	{
		struct MFS_DirEnt_t* addr_db = curr.db[i];
		int x = 0;
		while(x < 128)
		{
			if((addr_db + x)->inum == -1)
			{
				//if its file
				if(type == 0)
				{
					(addr_db + x)->inum = (addr_db + x - 1) ->inum + 1;
					memcpy( addr_db.name, &name, 28);

					return 0;
				}
			}
		}
	}	
		

	return -1;
}


int Unlink(int pinum, char *name){
  	void* addr = imap[pinum];
	struct inode curr = &addr;
	if(curr.type != 0) return -1;
	for(int i = 0; i< 14; i++)
	{
		struct MFS_DirEnt_t* addr_db = curr.db[i];
		int x = 0;
		while(x < 128)
		{
			//?
			if(strcmp( (addr_db + x)->name, name) == 0 )
			{
				//make it to -1?
				(addr_db + x)-> inum = -1;
				return 0;	
			}
			if((addr_db + x)->inum == -1){
				return -1;
			
			}
		}
	}	
	
	return -1;
}

int Shutdown(){
	exit(0);
	return 0; 
}
