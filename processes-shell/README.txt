Reading file: if we are reading more than 1 file, then return error and exit. DDo same thing if we have bad input file. Then we start to running the main program, which is the lsh_loop();

getinput(): we will get each char from the instream, essentially we will not continue if we have leading white spaces. 

built-in command: cd, exit, path and loop.
They do open file, kill the process, set different path and keep running a command.

Lsh_loop(): if we are not reading a file, then we want to read the input, which we will achieve by getinput().  If we are reading a file, then we want to elimiate all the leading and trailing spaces  as well. 
Note that if we have the ">" symbol, where has no space before or after, then we add spaces to these places so that we are able to parse the instream.
Also, if we have ">" it can only be 1 and can only be in the second last position. Otherwise it's an error. 

If the cmd is not any of the build-command, we want to put them in to the run() prorgam, where we basically do redirection and execure other commands.
