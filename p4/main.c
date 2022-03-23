#include <stdio.h>
#include "mfs.h"
#include "udp.h"

int main(){
	int rc = MFS_Init("rocket.cs.edu", 10000);
	return rc;
}
