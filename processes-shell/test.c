#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <assert.h>
#include <ctype.h>
#include <sys/wait.h>
#include <fcntl.h>
char* InsertSpace(char* origion, int size, int pos)
{
        char* new = malloc((size+1)*sizeof(char));
        int o_pos = 0;
        for(int x = 0; x< size+1; x++)
        {
                if(x == pos)
                 {
                      	 new[x] = ' ';
                         continue;
                }
                 new[x] = origion[o_pos];
                 o_pos++;

        }
     return new;
}

int main()
{
	char* temp = "01234>5678";

	char* x = InsertSpace(temp,10,5);
        char* a = InsertSpace(x,10,7);
	temp = a;
	printf("out:%s\n",temp);

}
