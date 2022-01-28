#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <assert.h>
#include <ctype.h>
#include <sys/wait.h>
#include <fcntl.h>

char**  path;
int path_size = 1; 
int argc  = 0;

int loop_count = 1;
int redict = 0;
char* file_name;

int fd1;
int fd2;
int current;
/*
 *implementation of exit
 *
 *exit the process
 */
void Exit(void)
{
	exit(0);
}

/*
 *call this function when we want to print out an error mesg
 */
void error(void)
{
	char error_message[30] = "An error has occurred\n";
        write(STDERR_FILENO, error_message, strlen(error_message));

}
/*
 *implementation of cd
 *
 * open a directory
 */
void cd(int argc, char** argv)
{
	if(argc == 0 || argc >1 ) 	
	{
		error();
	}
	else
	{
		
		//printf("dirs: %s\n", argv[0]);
		
		int n = chdir(argv[0]);
		if(n == -1) 
		{
			error();
		}
	}
        return; 	
}
/*
 *set the input as new path
 *
 * fork?
 */
void set_path(int argc, char** argv)
{
	path_size = 0;

	char** new_path = NULL; //= malloc(sizeof(char*));
	for(int i = 0;i< argc; i++ )
	{
	

		if(i == 0)
		{
			new_path = malloc(sizeof(char*));
			new_path[i] = argv[i];
					
		}
		else
		{
			char** temp = realloc(new_path, sizeof(char*)*(path_size+1));
			temp[path_size] = argv[i];
			new_path = temp; 
		}	
		path_size++; 
	}	
	path = new_path;
}
/*
 *read the input to string
 *
 *
 */
char* getinput(void)
{
	char * scanner = malloc(sizeof(char));
	int size = 0;
	int each;

	int start = 0;
	while(1)
	{
		
		each = getchar();
		if(each == ' ' && start == 0) continue;
		if(each != ' ' ) start = 1;
		if(each == EOF || each == '\n')
		{
			scanner[size] = '\0';
			return scanner;

		}
		else
		{
			if(size > 1)
			{
				char *temp = realloc(scanner,(size+1)*sizeof(char));
				temp[size] =each;
				scanner = temp;
				//free temp?			

			}
			else
			{
				scanner[size] = each;
		
			}
			size ++;
		}
		

	
	}
}
/*
 *where the gut is, might need to change the element in args to make ls
 *remove cmd
 */
void run(int size_input, char** argv,char* location, char* cmd)
{

	int rc = fork();
	//child	
	if(rc == 0 )
	{
				
		char **args;
		if(loop_count > 1)
		{
			args = malloc(sizeof(char*)*(size_input -1));
		
			for(int n = 1; n<size_input -2 ; n++) args[n] = argv[n-1];
			args[size_input - 2] = NULL;
		}
		else
		{
			args = malloc(sizeof(char*)*(size_input + 1));	
		
			for(int n = 1; n<size_input ; n++) args[n] = argv[n-1];
		
			args[size_input] = NULL;
		}
		args[0] = cmd;
		//args[1] = NULL;
		//args[size_input] = NULL;

		
		int fd;
		if(redict == 1)
		{
			
			fd = open(file_name, O_WRONLY |O_CREAT| O_TRUNC, 0666);
			//fd = open(file_name, O_WRONLY |O_CREAT| O_TRUNC, S_IRUSR| S_IWUSR);
			dup2(fd,STDOUT_FILENO);
				
		
		}
	/*	
		for(int x= 0; x< size_input -1 ; x++) 
		{		printf("agrs:%s\n",args[x]);
		
		}*/
		execv(location, args);
		printf("failed!");
		exit(0);
	}
	else
	{
	//parent
		if(rc > 0)
		{
			(void)wait(NULL);
			if(redict == 1)
			{
				dup2(STDOUT_FILENO,current);
				redict = 0;
			}
			return; 
		}
		else	
		{
			error();
			return;
		}
	}

}

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
char *trimwhitespace(char *str)
{
  char *end;

  // Trim leading space
  while(isspace((unsigned char)*str)) str++;

  if(*str == 0)  // All spaces?
    return str;

  // Trim trailing space
  end = str + strlen(str) - 1;
  while(end > str && isspace((unsigned char)*end)) end--;

  // Write new null terminator character
  end[1] = '\0';

  return str;
}

int isNum(char* things)
{
        for(int a = 0; a< strlen(things); a++)
        {
                if(isdigit(things[a]) == 0) return 0;

        }
        return 1;
}

int searchloop(char** instream,int size_input)
{
        for(int l = 0; l < size_input; l++)
        {
                if(strcmp(instream[l],"$loop")== 0) return l;

        }
        return -1;


}

void lsh_loop(FILE* fp)
{
	
	char *line = NULL;
	size_t len = 0;
	ssize_t read;
	
//WHAT IF WE enter a file name can't open
  	while(1)
	{
		
	if(fp == NULL)
	{	
		printf("wish> ");
		line = getinput();
	}
	else
	{
		//bacth
		fscanf(fp," ");
		if(read = getline(&line,&len,fp) == -1) break;
		line[strcspn(line,"\n")] = 0;
	}
	char* now = trimwhitespace(line);
	line = now;
	loop_count = 1;
        redict = 0;	
	//printf("line:%s\n",line);
	//what if our input is like exit + smh?	
	//parsing
	char **instream = malloc(sizeof(char*)*10);
        for(int i = 0; i< 10; i++) instream[i]= malloc(30*sizeof(char));

                //parsing start
        char *input, *string, *tofree;

        tofree = string = strdup(line);
        assert(string != NULL);
        int size_input = 0;
	//check if we need to do redirection 
	size_t len = strlen(string);
	size_t pos = strcspn(string, ">");
	if(pos != len )
	{
	//	printf("match");
		//if match
		if(pos == len-1)
		{
			error();
			continue;

		}
		if(pos == 0)
		{
			error();
			continue;
		}
		redict = 1; 

		char sit = 'a';
		if(string[pos - 1] != ' ')
		{
			if(string[pos + 1] != ' ' )
			{
				sit = 'b';
			}
			else sit = 'c'; 
		
		}
		else
		{
			if(string[pos + 1] != ' ') sit = 'd';
		}
		char* temp = NULL;
		char* temp2 = NULL;
		switch(sit)
		{
			case 'a':
				break;
			case 'b':
				//need add space both 
				temp = InsertSpace(string,len,pos);	
	        	        temp2 = InsertSpace(temp,len,pos+2);
        	        	string = temp2;
				break;
			case 'c':
				//add only before
				temp = InsertSpace(string,len,pos);	
				string = temp; 
				break;
			case 'd':
				//add only after
				temp = InsertSpace(string,len,pos + 1);
				string = temp;
				break;
		}
		//segfault?

		//printf("string:%s\n",string);
	}

	while( (input = strsep(&string, " ")) != NULL)
	{
	//	input[strcspn(input," ")] = 0;
		//printf("input:%s\n",i
		instream[size_input]= input;
                size_input ++;
        }
	char** argv;
	argv = malloc( (size_input - 1)*sizeof(char*));
	memcpy(argv, &instream[1], (size_input-1)*sizeof(char*));
	//chceck if redirectory
	if(redict == 1)
	{
		int count = 0;
		for(int x = 0 ; x < size_input; x++)
		{
			if(strcmp(instream[x], ">") == 0)  count++;
		}
		if(count > 1)
		{
			error();
			continue;
		}
		if(size_input >= 3 && strcmp(instream[size_input-2],">")== 0)
		{
		redict = 1;
		file_name = instream[size_input -1 ];
		char** new_argv = malloc( (size_input - 3)*sizeof(char*));

		memcpy(new_argv, &instream[1], (size_input - 3)*sizeof(char*));
	 	argv = new_argv;
	
		}
		else
		{
			error();
			continue;
		}

	
	
	}
	if(strcmp(instream[0],"exit") == 0)
	{
	
		if(size_input >= 2)
		{	
			error();
			continue;
		
		}	
		Exit();
	}

	if(strcmp(instream[0],"cd") == 0)
	{
		cd(size_input - 1, argv);
		continue;
	
	}
	//loop start	
	
	if(strcmp(instream[0],"loop") == 0)
	{
		if(size_input >= 3 && isNum(instream[1]) == 1)
		{
			loop_count = atoi( instream[1] );
			char ** new_argv = malloc( (size_input - 3)*sizeof(char*));
			memcpy(new_argv, &instream[3], (size_input-3)*sizeof(char*));
			argv = new_argv;
		}
		else
		{
			error();
			continue;
		}
	}
	//loop set up
	if(strcmp(instream[0],"path") == 0)
	{
		//printf("do");
		set_path(size_input - 1, argv);		
	}
	else
	{
	
		int pos_loop = searchloop(instream,size_input);
		for(int l = 0; l< loop_count; l++)
		{
			//printf("in[-1]:%s\n",instream[size_input-1]);
			

			if(pos_loop != -1)
			{
				char buffer[BUFSIZ];
				sprintf(buffer, "%d", l+1);
				argv[pos_loop - 3 ] = buffer;
				instream[pos_loop]= buffer;
			}	
			//printf("filename: %s\n",file_name);
			int success = 0;
			for(int i = 0; i< path_size; i++)
			{
				char *location = malloc(sizeof(char)*20);
				char* cmd;
				if(strcmp(instream[0],"loop")==0) cmd = instream[2];
				else cmd = instream[0];
					

				strcpy(location,path[i]);
				strcat(location,"/");
				if(strcmp(instream[0],"loop") == 0 )
					strcat(location,instream[2]);
				else
					strcat(location,instream[0]);
			
				if(access(location, X_OK) == 0)
				{
					
			for(int x=0; x<size_input -1; x++) printf("argv:%s\n",argv[x]);
					success = 1;
					run(size_input, argv, location,cmd);
					break;
				}
			//	else success = 0;	
			
			}
			if(success == 0) 	error();
				
		}
	}
		
	//free(line);
    	//free(args);
  	}
	
}
int main(int argc, char** argv)
{
	path = malloc(sizeof(char*));
	path[0] = "/bin";	
	char* file = malloc(sizeof(char)*40);
	FILE* fp = NULL;
	if(argc == 2) 
	{
		strcpy(file,argv[1]);
		
		fp = fopen(file,"r");
		if(fp == NULL) 
		{
			error();
			exit(1);
		}
	}
	else if (argc > 2)
	{
		error();
		exit(1);
	}
	lsh_loop(fp);

	exit(0);


}

