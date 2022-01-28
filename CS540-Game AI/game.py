# -*- coding: utf-8 -*-
"""
Created on Sat Apr 10 10:11:36 2021

@author: hugaoyi
"""
import random
import copy
import time
class Teeko2Player:
    """ An object representation for an AI game player for the game Teeko2.
    """
    board = [[' ' for j in range(5)] for i in range(5)]
    pieces = ['b', 'r']
    #define a deepth here
    #deepth = 10

    def __init__(self):
        """ Initializes a Teeko2Player object by randomly selecting red or black as its
        piece color.
        """
        self.my_piece = random.choice(self.pieces)
        self.opp = self.pieces[0] if self.my_piece == self.pieces[1] else self.pieces[1]
        self.depth = 3  
        self.turn = ''
        
    def make_move(self, state):
        """ Selects a (row, col) space for the next move. You may assume that whenever
        this function is called, it is this player's turn to move.

        Args:
            state (list of lists): should be the current state of the game as saved in
                this Teeko2Player object. Note that this is NOT assumed to be a copy of
                the game state and should NOT be modified within this method (use
                place_piece() instead). Any modifications (e.g. to generate successors)
                should be done on a deep copy of the state.

                In the "drop phase", the state will contain less than 8 elements which
                are not ' ' (a single space character).

        Return:
            move (list): a list of move tuples such that its format is
                    [(row, col), (source_row, source_col)]
                where the (row, col) tuple is the location to place a piece and the
                optional (source_row, source_col) tuple contains the location of the
                piece the AI plans to relocate (for moves after the drop phase). In
                the drop phase, this list should contain ONLY THE FIRST tuple.

        Note that without drop phase behavior, the AI will just keep placing new markers
            and will eventually take over the board. This is not a valid strategy and
            will earn you no points.
        """
        
   
        
        # select an unoccupied space randomly
        # TODO: implement a minimax algorithm to play better
         
        #return the value and state we want
        final, new_state = self.max_value(state,0, float("-inf"), float("+inf"))
        
        move_list={}
        
        #find the differences between the new state and old state          
        for x in range(5):
            for y in range(5):
                if state[x][y] != new_state[x][y]:
                    #find the different place of ' ' to be destionation 
                    if state[x][y] == ' ':
                        move_list["Des"]=(x,y)
                    #find the different place of filled piece as source
                    if state[x][y] == self.my_piece:
                        move_list["Src"]=(x,y)
        #if we have 2 elements
        #then get both the Des and Src
        if len(move_list) == 2:
            move = [move_list.get("Des"),move_list.get("Src")]
        else:
            #if only has 1 element
            #then only extract Des
            move = [move_list.get("Des")]
        #replace with new state
        state = new_state
        
        self.turn = self.opp
        return move

    def opponent_move(self, move):
        """ Validates the opponent's next move against the internal board representation.
        You don't need to touch this code.

        Args:
            move (list): a list of move tuples such that its format is
                    [(row, col), (source_row, source_col)]
                where the (row, col) tuple is the location to place a piece and the
                optional (source_row, source_col) tuple contains the location of the
                piece the AI plans to relocate (for moves after the drop phase). In
                the drop phase, this list should contain ONLY THE FIRST tuple.
        """
        # validate input
        if len(move) > 1:
            source_row = move[1][0]
            source_col = move[1][1]
            if source_row != None and self.board[source_row][source_col] != self.opp:
                self.print_board()
                print(move)
                raise Exception("You don't have a piece there!")
            if abs(source_row - move[0][0]) > 1 or abs(source_col - move[0][1]) > 1:
                self.print_board()
                print(move)
                raise Exception('Illegal move: Can only move to an adjacent space')
        if self.board[move[0][0]][move[0][1]] != ' ':
            raise Exception("Illegal move detected")
        # make move
        self.place_piece(move, self.opp)
        self.turn = self.my_piece
        

    def place_piece(self, move, piece):
        """ Modifies the board representation using the specified move and piece

        Args:
            move (list): a list of move tuples such that its format is
                    [(row, col), (source_row, source_col)]
                where the (row, col) tuple is the location to place a piece and the
                optional (source_row, source_col) tuple contains the location of the
                piece the AI plans to relocate (for moves after the drop phase). In
                the drop phase, this list should contain ONLY THE FIRST tuple.

                This argument is assumed to have been validated before this method
                is called.
            piece (str): the piece ('b' or 'r') to place on the board
        """
        if len(move) > 1:
            #i feel like it has something wrong
            self.board[move[1][0]][move[1][1]] = ' '
        self.board[move[0][0]][move[0][1]] = piece

    def print_board(self):
        """ Formatted printing for the board """
        for row in range(len(self.board)):
            line = str(row)+": "
            for cell in self.board[row]:
                line += cell + " "
            print(line)
        print("   A B C D E")

    def game_value(self, state):
        """ Checks the current board status for a win condition

        Args:
        state (list of lists): either the current state of the game as saved in
            this Teeko2Player object, or a generated successor state.

        Returns:
            int: 1 if this Teeko2Player wins, -1 if the opponent wins, 0 if no winner

        TODO: complete checks for diagonal and 3x3 square corners wins
        """
        # check horizontal wins
        for row in state:
            for i in range(2):
                if row[i] != ' ' and row[i] == row[i+1] == row[i+2] == row[i+3]:
                    return 1 if row[i]==self.my_piece else -1

        # check vertical wins
        for col in range(5):
            for i in range(2):
                if state[i][col] != ' ' and state[i][col] == state[i+1][col] == state[i+2][col] == state[i+3][col]:
                    return 1 if state[i][col]==self.my_piece else -1

        # TODO: check \ diagonal wins
       
        for row in range(2):
            for i in range(2):
                if state[row][i] != ' ' and state[row][i] == state[row+1][i+1] == state[row+2][i+2] == state[row+3][i+3]:
                    return 1 if state[row][i] == self.my_piece else -1
       
        # TODO: check / diagonal wins
        for row in range(2):
            for i in range(3,5):
                if state[row][i] != ' ' and state[row][i] == state[row+1][i-1] == state[row+2][i-2] == state[row+3][i-3]:
                    return 1 if state[row][i] == self.my_piece else -1
       
        # TODO: check 3x3 square corners wins
        for row in range(3):
            for i in range(3):
                if state[row][i] != ' ' and state[row][i] == state[row][i+2] == state[row+2][i] == state[row+2][i+2]:
                    return 1 if state[row][i] == self.my_piece else -1
        return 0 # no winner yet
   

                    

    def succ(self, state):
        succ = []
        total_b = 0
        total_r = 0
        for row in state:
            total_b += row.count('b')
            total_r += row.count('r')
        #move phase
        if total_b == 4 and total_r ==4:
            for row in range(5):
                for col in range(5):
                    #find the marker
                    
                        
                    if state[row][col] == self.my_piece:
                        #then  move it 
                        #name all 8 possibialities
                        left = copy.deepcopy(state)
                        right = copy.deepcopy(state)
                        up = copy.deepcopy(state)
                        down = copy.deepcopy(state)
                        upper_left = copy.deepcopy(state)
                        upper_right = copy.deepcopy(state)
                        lower_left = copy.deepcopy(state)
                        lower_right = copy.deepcopy(state)
                        #top 
                        if row == 0:
                            if state[row+1][col] == ' ' :
                                down[row+1][col] = self.my_piece
                                down[row][col] = ' ' 
                                succ.append(down)
                            #top left
                            if col == 0:
                                if state[row][col+1] == ' ' :
                                    right[row][col+1] = self.my_piece
                                    right[row][col] = ' ' 
                                    succ.append(right)
                                if state[row+1][col+1] == ' ' :
                                    lower_right[row+1][col+1] = self.my_piece
                                    lower_right[row][col] = ' ' 
                                    succ.append(lower_right)
                            #top-right
                            if col == 4:
                                if state[row][col-1] == ' ' :
                                    left[row][col-1] = self.my_piece
                                    left[row][col] = ' ' 
                                    succ.append(left)
                                  
                                if state[row+1][col-1] == ' ' :
                                    lower_left[row+1][col-1] = self.my_piece
                                    lower_left[row][col] = ' ' 
                                    succ.append(lower_left)
                        #left only
                        if col ==0 and (row != 0 and row != 4):
                            if state[row-1][col+1] == ' ' :
                                upper_right[row-1][col+1] = self.my_piece
                                upper_right[row][col] = ' '
                                succ.append(upper_right)
                            
                            if state[row][col+1] == ' ' :
                                right[row][col+1] = self.my_piece
                                right[row][col] = ' ' 
                                succ.append(right)
                            if state[row+1][col+1] == ' ' :
                                lower_right[row+1][col+1] = self.my_piece
                                lower_right[row][col] = ' '
                                succ.append(lower_right)
                        #right only
                        if col ==4 and (row != 0 and row != 4):
                            if state[row-1][col-1] == ' ' :
                                upper_left[row-1][col-1] = self.my_piece
                                upper_left[row][col] = ' '
                                succ.append(upper_left) 
                            
                            if state[row][col-1] == ' ' :
                                left[row][col-1] = self.my_piece
                                left[row][col] = ' ' 
                                succ.append(left)
                            if state[row+1][col-1] == ' ' :
                                lower_left[row+1][col-1] = self.my_piece
                                lower_left[row][col] =' '
                                succ.append(lower_left)
                        #bottom
                        if row == 4 :
                            if state[row-1][col] == ' ' :
                                up[row-1][col] = self.my_piece
                                up[row][col] = ' ' 
                                succ.append(up)
                            if col == 0:
                                if state[row][col+1] == ' ' :
                                    right[row][col+1] = self.my_piece
                                    right[row][col] = ' '
                                    succ.append(right)
                                if state[row-1][col+1] == ' ' :
                                    upper_right[row-1][col+1] = self.my_piece
                                    upper_right[row][col] = ' '
                                    succ.append(upper_right)
                            if col == 4:
                                if state[row][col-1] == ' ' :
                                    left[row][col-1] = self.my_piece
                                    left[row][col] = ' '
                                    succ.append(left)
                                
                                if state[row-1][col-1] == ' ' :
                                    upper_left[row-1][col-1] = self.my_piece
                                    upper_left[row][col] = ' '
                                    succ.append(upper_left)
                        #regular cases
                        if row != 0 and row != 4 and col != 0 and col != 4:
                            
                            if state[row][col-1] == ' ' :
                                left[row][col-1] = self.my_piece
                                left[row][col] = ' '
                                succ.append(left)
                            if state[row-1][col] == ' ' :
                                up[row-1][col] = self.my_piece
                                up[row][col] = ' '
                                succ.append(up)
                            if state[row][col+1] == ' ' :
                                right[row][col+1] = self.my_piece
                                right[row][col] = ' '
                                succ.append(right)
                            if state[row+1][col] == ' ' :
                                down[row+1][col] = self.my_piece
                                down[row][col] = ' '
                                succ.append(down)
                            if state[row+1][col+1] == ' ' :
                                lower_right[row+1][col+1] = self.my_piece
                                lower_right[row][col] = ' '
                                succ.append(lower_right)
                            if state[row+1][col-1] == ' ' :
                                lower_left[row+1][col-1] = self.my_piece
                                lower_left[row][col] = ' '
                                succ.append(lower_left)
                            if state[row-1][col+1] == ' ' :
                                upper_right[row-1][col+1] = self.my_piece
                                upper_right[row][col] = ' '
                                succ.append(upper_right)
                            if state[row-1][col-1] == ' ' :
                                upper_left[row-1][col-1] = self.my_piece
                                upper_left[row][col] = ' '
                                succ.append(upper_left)
        #drop phase   
        else:

            for row in range(5):
                for i in range(5):
                    if state[row][i] == ' ':
                        neo_list = copy.deepcopy(state)
                        neo_list[row][i] = self.my_piece
                        #print(neo_list)
                        succ.append(neo_list)
        return succ


    def max_value(self, state, depth, alpha, beta):
        x = self.game_value(state)
        #modify x
        possible = self.succ(state)
        #print(possible)
        if  x== -1 or x == 1:
            return x, state
        
            #check 
        if depth == self.depth :
            return self.heuristic_game_value(state),state
        
        
      
        depth += 1
        
        child = state
        for i in possible:
            var1, neo_ =self.mini_value(i,depth,alpha,beta)
            if var1 > alpha:
                child = i  
                alpha = var1
            if alpha > beta:
                return beta, i
        

        return alpha, child

                
    def mini_value(self,state,depth, alpha, beta):
        x = self.game_value(state)
        #modify x
        #print("running")
        #print(x)
        possible = self.succ(state)
        if  x== -1 or x == 1:
            return x, state
        
            #check 
        if depth == self.depth :
            return self.heuristic_game_value(state),state
        
        depth += 1
        child = state
        for i in possible:
            var1, neo =self.max_value(i,depth, alpha, beta)
            if var1 < beta:
                beta = var1
                child = i 
            if alpha > beta:
                return alpha, i 
        return beta, child
    #d could     be any value defined by ourselves?
    #how to find ex
    #how to know it is whose turn?
    def bigger(self,x,y):
        if x>=y:
            return x
        else:
            return y
    
        
    def heuristic_game_value(self, state):
        x = self.game_value(state)
        #modify x
        if  x== -1 or x == 1:
            return x
        else:
            #store the value of ex of black 
            #and ex of red
            #contains all lines
            case = []
            #hori
            for row in state:
                for i in range(2):
                    segment = row[i:i+4]
                    if segment.count('r') >0 or segment.count('b') >0:
                        case.append(segment)
                       
            
            #ver
            col_val= 0
            while col_val <4:
                column = []
                for row in range(5):
                    column.append(state[row][col_val])
                col_val += 1
                for i in range(2):
                    segment = column[i:i+4]
                    if segment.count('r') >0 or segment.count('b') >0:
                        case.append(segment)
               
           
            #\diagonal
            for x in range(2):
                for y in range(2):
                    segment = []
                    segment.append(state[x][y])
                    segment.append(state[x+1][y+1])
                    segment.append(state[x+2][y+2])
                    segment.append(state[x+3][y+3])
                    if segment.count('r') >0 or segment.count('b') >0:
                        case.append(segment)
           
           
            #/diagonal
            for x in range(2):
                for y in range(3,5):
                    segment = []
                    segment.append(state[x][y])
                    segment.append(state[x+1][y-1])
                    segment.append(state[x+2][y-2])
                    segment.append(state[x+3][y-3])
                    if segment.count('r') >0 or segment.count('b') >0:
                        case.append(segment)
            #square

            for x in range(3):
                for y in range(3):
                    segment = []
                    segment.append(state[x][y])
                    segment.append(state[x][y+2])
                    segment.append(state[x+2][y])
                    segment.append(state[x+2][y+2])
                    if segment.count('r') >0 or segment.count('b') >0:
                        case.append(segment)
           
            ex = 0
            for i in case:
                if i.count(self.my_piece) == 3 and i.count(' ') == 1:
                    ex += 0.99
                if i.count(self.my_piece) == 2 and i.count(' ') == 2:
                    ex += 0.5
                if i.count(self.my_piece) == 1 and i.count(' ') == 3:
                    ex += 0.2
                if i.count(self.opp) == 3 and i.count(' ') == 1:
                    ex -= 0.99
                if i.count(self.opp) == 2 and i.count(' ') == 2:
                    ex -= 0.5
                if i.count(self.opp) == 1 and i.count(' ') == 3:
                    ex -= 0.2
            ex = ex/len(case)
            return ex
            
############################################################################
#
# THE FOLLOWING CODE IS FOR SAMPLE GAMEPLAY ONLY
#
############################################################################
def main():
    print('Hello, this is Samaritan')
    ai = Teeko2Player()
    piece_count = 0
    turn = 0

    # drop phase
    while piece_count < 8 and ai.game_value(ai.board) == 0:

        # get the player or AI's move
        if ai.my_piece == ai.pieces[turn]:
           
            ai.print_board()
            move = ai.make_move(ai.board)
            ai.place_piece(move, ai.my_piece)
            print(ai.my_piece+" moved at "+chr(move[0][1]+ord("A"))+str(move[0][0]))
           
        else:
            move_made = False
            ai.print_board()
            print(ai.opp+"'s turn")
            while not move_made:
                player_move = input("Move (e.g. B3): ")
                while player_move[0] not in "ABCDE" or player_move[1] not in "01234":
                    player_move = input("Move (e.g. B3): ")
                try:
                    ai.opponent_move([(int(player_move[1]), ord(player_move[0])-ord("A"))])
                    move_made = True
                except Exception as e:
                    print(e)

        # update the game variables
        piece_count += 1
        turn += 1
        turn %= 2

    # move phase - can't have a winner until all 8 pieces are on the board
    while ai.game_value(ai.board) == 0:

        # get the player or AI's move
        if ai.my_piece == ai.pieces[turn]:
            
            ai.print_board()
            move = ai.make_move(ai.board)
            ai.place_piece(move, ai.my_piece)
            print(ai.my_piece+" moved from "+chr(move[1][1]+ord("A"))+str(move[1][0]))
            print("  to "+chr(move[0][1]+ord("A"))+str(move[0][0]))
           
        else:
            move_made = False
            ai.print_board()
            print(ai.opp+"'s turn")
            while not move_made:
                move_from = input("Move from (e.g. B3): ")
                #start_time = time.time()
        
                while move_from[0] not in "ABCDE" or move_from[1] not in "01234":
                    move_from = input("Move from (e.g. B3): ")
                move_to = input("Move to (e.g. B3): ")
                while move_to[0] not in "ABCDE" or move_to[1] not in "01234":
                    move_to = input("Move to (e.g. B3): ")
                try:
                    ai.opponent_move([(int(move_to[1]), ord(move_to[0])-ord("A")),
                                    (int(move_from[1]), ord(move_from[0])-ord("A"))])
                    move_made = True
                except Exception as e:
                    print(e)
        #end_time = time.time()
        #elapsed= end_time- start_time
      #  print("the process has run:",elapsed)
        # update the game variables
        turn += 1
        turn %= 2

    ai.print_board()
    if ai.game_value(ai.board) == 1:
        print("AI wins! Game over.")
    else:
        print("You win! Game over.")


if __name__ == "__main__":
    main()
