# -*- coding: utf-8 -*-
"""
Created on Fri Apr 16 13:09:51 2021

@author: PC
"""
import copy
import heapq

source = [1, 2, 3, 4, 5, 6, 7, 8, 0]

def manhattan(des, state):
    #get the corresponding row and col in here
    des_row = state.index(des) // 3
    des_col = state.index(des) % 3
    #if its ok to define the source here
    src_row = source.index(des) // 3
    src_col = source.index(des) % 3
    #get the manhattan distance from this formluar
    return abs(des_row - src_row) + abs(des_col - src_col)
 
def swap(index, state, var):
    #sawp the values in 2 different positions
    case = copy.deepcopy(state)
    case[index] =  case[index + var ]
    case[index + var] = 0
    return case

def Heuristic(succ):
    #calculate the Heuristic value
    total = 0
    #accumulate all the Heuristic values
    #from 1 to 8
    for i in range(1,9):
        total += manhattan(i, succ)
    return total
        


#change it to 2d or keep in 1d
def print_succ(state):
    success = succ(state)
    for i in success:
        hvalue = Heuristic(i)
        print(i, "h={}".format(hvalue))


def succ(state):
    succ = []
    #go through all the elements in state
    for index in range(9):
        #swap the 0 and element nearby 
        if state[index] == 0:
            #index is not at the left most position 
            if index % 3 != 2:
                
                try:
                    case = swap(index, state, 1)
                    succ.append(case)
                except Exception: 
                    pass
            if index % 3 != 0:
                try:
                    case = swap(index, state, -1)
                    succ.append(case)
                except Exception: 
                    pass
            if index <= 5:
                try:
                    case = swap(index, state, 3)
                    succ.append(case)
                except Exception: 
                    pass
            if index >= 3:
                try:
                    case = swap(index, state, -3)
                    succ.append(case)
                except Exception: 
                    pass
    succ = sorted(succ)
    return succ

move = 0

def path(close):
    result = []

    #start from the result

    
    #get the value of the start
    parent = close.get(str(source))
    #add them into result
    result.append((source,parent[1],parent[0]))
  
    while True:
        #get the parent of the current node
        next_node = close.get(str(parent[2]))

        if next_node == None:
            break
        #add 
        result.append((parent[2], next_node[1], next_node[0]))
        
        parent = close.get(str(parent[2]))
         
   
        

    result.reverse()
    #print(result)
    i = 0
    while i< len(result):
        print("{} h={} moves: {}".format(result[i][0], result[i][1],result[i][2]))

        i += 1
 
def solve(state):
    final = []
    close = {}
    hx =  Heuristic(state)
   
    g = 0
    parent_index = -1
    heapq.heappush(final, (hx + g , state, (g,  hx, parent_index) ) )
  
    while final:
    
        parent = heapq.heappop(final)
 
        node = parent [1]
        
        hx = Heuristic(node)
        
        close[str(node)] = (parent[2][0], parent[2][1],parent[2][2] )

        #print(node)
        if node == source:
            path(close)
            return 
        #how to extract the value from parent
        g = parent[2][0]+1
        sucors = succ(node)
        for i in sucors:
            if str(i) not in close:
                hx =  Heuristic(i)
                heapq.heappush(final, (g+hx, i, (g,  hx, str(node)) ) )
        


if __name__ == "__main__":
     #print_succ([4,0,3,5,1,8,7,2,6])
    
   # print_succ([4,3,8,5,1,6,7,2,0])
    solve([4,3,8,5,1,6,7,2,0])

