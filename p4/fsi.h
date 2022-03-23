#ifndef __fsi_h__
#define __fsi_h__

#include <stdio.h>
#include "mfs.h"


struct inode_s{
  int type; // dirctory file: 0    regualr file: 1
  int size; // number of last byte
  int dpoint[14]; // direct point

};

struct imap_s{
  int inodes[16]; //each pieces of inode map has 16 entries
  int igroup[256]; // there are 256 piceces in inode map whith 16 entries a group
};

struct fsi_DirEnt_t {
    char name[28];  // up to 28 bytes of name in directory (including \0)
    int  inum;      // inode number of entry (-1 means entry not used)
};


int fsi_Init(char *hostname, int port);
int fsi_Lookup(int pinum, char *name);
int fsi_Stat(int inum, MFS_Stat_t *m);
int fsi_Write(int inum, char *buffer, int block);
int fsi_Read(int inum, char *buffer, int block);
int fsi_Creat(int pinum, int type, char *name);
int fsi_Unlink(int pinum, char *name);
int fsi_Shutdown();


#endif // __fsi_h__
