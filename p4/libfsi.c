#include <stdio.h>
#include "udp.h"
#include "fsi.h"
#include "msg.h

struct inode{
  int size;
  int type;//regular = 0, directory = 1;
  int** db;// 14 direct pointers 4kb each

};

struct seq{
	int** db;
	struct inode* imap;
};

struct log{
	void* cr;
	struct seq* content;

};

#define BUFFER_SIZE(1000)


struct sockaddr_in addrSnd, addrRcv;


int sd;
int rc;

int inodes[4096]; // the inum's size is 4096



int inode_info(int inum, inode_s *inode){
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



int fsi_Init(char *hostname, int port){
  return 0;
}
int fsi_Lookup(int pinum, char *name){
  return 0;
}
int fsi_Stat(int inum, MFS_Stat_t *m){
  return 0;
}
int fsi_Write(int inum, char *buffer, int block){
  return 0;
}
int fsi_Read(int inum, char *buffer, int block){
  return 0;
}
int fsi_Creat(int pinum, int type, char *name){
  return 0;
}
int fsi_Unlink(int pinum, char *name){
  return 0;
}
int fsi_Shutdown(){
  return 0;
}
