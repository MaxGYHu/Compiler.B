#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "udp.h"
#include "mfs.h"

char *hostname_ = NULL;
int port_ = 0;
int ready = 0;

int sendMessage(msg *msg1, msg *message, char *hostname, int port)
{
    int sd = -1;
    int tmpPort = 23451;
    while (sd == -1) {
        sd = UDP_Open(tmpPort++);
    }

    struct sockaddr_in addr, addr2;
    int rc = UDP_FillSockAddr(&addr, hostname, port);

    fd_set fd;


    struct timeval time;
    time.tv_sec = 3;
    time.tv_usec = 0;

    while (1) {
        FD_ZERO(&fd);
        FD_SET(sd, &fd);
        UDP_Write(sd, &addr, (char*)msg1, sizeof(msg));
        if (select(sd + 1, &fd, NULL, NULL, &time)) {
            rc = UDP_Read(sd, &addr2, (char*)message, sizeof(msg));
            if (rc > 0) {
                UDP_Close(sd);
                return 0;
            }
        }
    }
}

int MFS_Init(char *hostname, int port) {
    hostname_ = strdup(hostname);
    port_ = port;
    ready = 1;
    printf("______inilzaition__________");
    return 0;
}

int MFS_Lookup(int pinum, char *name) {
    printf("mark, MFS_LOOKUP Starts =============");
    if (ready != 1 || strlen(name) >= 28 || name == NULL || pinum < 0 || pinum >= 4096) {
        return -1;
    }
    msg msg1, msg2;
    msg1.inum = pinum;
    msg1.func = 0;
    strcpy((char*)&(msg1.name), name);
    printf("%s\n",name);
    printf("******************before sned\n");
    if (sendMessage(&msg1, &msg2, hostname_, port_) < 0) {
        return -1;
    }
    return msg2.inum;
}

int MFS_Stat(int inum, MFS_Stat_t *m) {
    if (ready != 1) {
        return -1;
    }
    if(inum < 0 || inum >= 4096) {
        return -1;
    }
    msg msg1, msg2;
    msg1.inum = inum;
    msg1.func = 1;
    printf("=================start stat=============");
    if (sendMessage(&msg1, &msg2, hostname_, port_) < 0) {
        return -1;
    }
    m->type = msg2.stat.type;
    m->size = msg2.stat.size;
    return 0;
}

int MFS_Write(int inum, char *buffer, int block) {
    if (ready != 1 || inum < 0 || inum >= 4096 || block < 0 || block >= 14) {
        return -1;
    }

    msg msg1, msg2;
    msg1.inum = inum;
    msg1.block = block;
    msg1.func = 2;
    for (int i = 0; i < SIZE_BLOCK; i++) {
        msg1.buffer[i] = buffer[i];
    }
    if (sendMessage(&msg1, &msg2, hostname_, port_) < 0) {
        return -1;
    }
    return 0;
}

int MFS_Read(int inum, char *buffer, int block) {
    if (ready != 1 || inum < 0 || inum >= 4096 || block < 0 || block >= 14) {
        return -1;
    }

    msg msg1, msg2;
    msg1.inum = inum;
    msg1.block = block;
    msg1.func = 3;
    if (sendMessage(&msg1, &msg2, hostname_, port_) < 0) {
        return -1;
    }
    for (int i = 0; i < SIZE_BLOCK; i++) {
        buffer[i] = msg2.buffer[i];
    }
    return 0;
}

int MFS_Creat(int pinum, int type, char *name) {
    printf("---------mark0");
    if (ready != 1 || strlen(name) >= 28 || name == NULL || pinum < 0 || pinum >= 4096) {
        return -1;
    }

    msg msg1, msg2;
    msg1.inum = pinum;
    msg1.type = type;
    msg1.func = 4;
    strcpy(msg1.name, name);
    printf("-------------mark1");
    if (sendMessage(&msg1, &msg2, hostname_, port_) < 0 || msg2.inum == -1) {
          printf("----------msg2.inum:%d\n",msg2.inum);
	  return -1;
    }
    printf("----------------mark2");
    return 0;
}

int MFS_Unlink(int pinum, char *name) {
    if (ready != 1 || strlen(name) >= 28 || name == NULL || pinum < 0 || pinum >= 4096) {
        return -1;
    }
    msg msg1, msg2;
    msg1.inum = pinum;
    msg1.func = 5;
    strcpy(msg1.name, name);
    if (sendMessage(&msg1, &msg2, hostname_, port_) < 0 || msg2.inum == -1) {
        return -1;
    }
    return 0;
}

int MFS_Shutdown() {
    msg msg1, msg2;
    msg1.func = 6;
    printf("+++++++++++++++++tring to send+++++++++++++");
    if (sendMessage(&msg1, &msg2, hostname_, port_) < 0) {
        printf("***send failed***");
	   return -1;
    }
    printf("****send success****");
    return 0;
}
