# -*- coding: utf-8 -*-
"""
Created on Sun Feb 28 00:53:52 2021

@author: PC
"""

import csv
import math
import random
import numpy as np
from matplotlib import pyplot as plt


#input the filename or filepath
#return the data
def load_data(filepath):
    total_list = []
    with open(filepath, newline='') as csvfile:
        count = 0
        reader = csv.DictReader(csvfile)
        for line in reader:
            #only need 20 rows
            if count == 20:
                break
            #delete the columns dont need
            del line['Generation']
            del line['Legendary']
            # convert the string to integral
            line['#'] = int(line['#'])
            line['Total'] =int(line['Total'])
            line['HP'] = int(line['HP'])
            line['Attack'] = int(line['Attack'])
            line['Defense'] = int(line['Defense'])
            line['Sp. Atk'] = int(line['Sp. Atk'])
            line['Sp. Def'] = int(line['Sp. Def'])
            line['Speed'] = int(line['Speed'])
            
            # append it to the output list
            total_list.append(line)
            
            count += 1
    return total_list

#input the data
#return the value of x and y
def calculate_x_y(stats):
    x = stats['Attack'] + stats['Sp. Atk'] + stats['Speed']
    y = stats['Defense'] + stats['HP'] + stats['Sp. Def']
    return (x,y)


#input 2 clusters and label
#get the shortest between 2 clusters
#return the shortest distance, the corresponding 2 indeices
def euclidean_distance(cluster1, cluster2, label):

   
    small = 9999999
    #extract each point from cluster
    for point1 in cluster1:
        #exteact point from cluster
        for point2 in cluster2:
            #get the position by label
            d = math.sqrt((label[point2][0] - label[point1][0]) ** 2 + (label[point2][1] - label[point1][1]) ** 2)
           #update the small value
            if d < small:
                small = d
               
                num1 = point1
                num2 = point2
    #print(small)
    return small, num1, num2


#input the valid_data and label
#return the shortest clusters and their distance
def distance_between_cluster(data, label):

    # smallest = euclidean_distance(input[0][1], input[])
    small = 9999
    final_index1 = 0
    final_index2 = 0
    for i in range(len(data) - 1):   # loop through the input
        for j in range(i+1, len(data)):  # loop through the input
            #calculate distance between all the clusters
            dist, index1, index2 = euclidean_distance(data[i], data[j], label)   # get the points distanc
            if dist < small:
                small = dist
                final_index1 = index1
                final_index2 = index2

        
    return final_index1, final_index2, small


#input the data
#return all the matrix

def hac(dataset):
   
   
    # store all the position
    # so we get the position by the indices
    label = []
    # put the point with its index in a new list
    label = [ x for x in dataset if not (math.isnan(x[0]) or math.isnan(x[1]))]
    # filter out all the data contains NaN
    
    #where store all the clusters
    #all clusters have one point at beginning
    valid_data = [[l] for l in range(len(label)) ]
    #where strore all the clusters
    cluster = [] 
    
    for x in range(len(valid_data)):
        cluster.append([x,0,0,1])

     
   # only store the clusters over or equal 2 points
    new_cluster=[]
    # i is m+ position which the index from the new_cluster
    i = 20
    while cluster[-1][3] != 20:  # where last element doesnot include all the points
        # if the cluster is empty
        # get the closet points index and distance
        
        index1, index2, smalest_dist= distance_between_cluster( valid_data, label)
        # append the index and distan to 
  
        # get the index we want within the valid_data
        for row in valid_data:
            if index1 in row:
                pos1 = valid_data.index(row)
            if index2 in row:
                pos2 = valid_data.index(row)

       #update index if needed
        if len(valid_data[pos1]) > 1:
            #go from the last element, which is newest
            j = len(new_cluster)-1
            while j >=0:
                if index1 in new_cluster[j][1]:
                    #print("cluster,new:", new_cluster[j])
                    index1 = new_cluster[j][0]
                    break
                #j goes to last element
                j -= 1
       #update index if needed
        if len(valid_data[pos2]) > 1:
            j = len(new_cluster)-1
            while j >=0:
                if index2 in new_cluster[j][1]:
                    index2 = new_cluster[j][0]
                    
                    break
                 #j goes to last element
                j -= 1
        # get the total points by their length
        total = len(valid_data[pos1]) + len (valid_data[pos2])
        #combine 2 lists 
        valid_data[pos1] = valid_data[pos1] + valid_data[pos2]
        
        new_cluster.append([i,valid_data[pos1]])
        #update the position
        i += 1
        #remove one
        valid_data.pop(pos2)

        #put the smaller index first
        if index1 < index2:
            cluster.append([index1, index2, smalest_dist, total])
        if index1 > index2:
            cluster.append([index2, index1, smalest_dist, total])
            
       
        
       #convert into np
    return np.asmatrix(cluster[20:])

#generate random positions
#return the point
def random_x_y(m):
    total = []
    for num in range(m):
        x = random.randint(1, 359)
        y = random.randint(1, 359)
        total.append((x,y))
    return total

#display the image
def imshow_hac(dataset):
   
    
   
    # output array
    label = []
    # put the point with its index in a new list
     # filter out all the data contains NaN
    label = [ x for x in dataset if not (math.isnan(x[0]) or math.isnan(x[1]))]
    valid_data = [[l] for l in range(len(label)) ]
    cluster = [] 
    for x in range(len(valid_data)):
        cluster.append([x,0,0,1])

     
   
    new_cluster=[]
    i = 20
    plt.figure()
    while cluster[-1][3] != 20:  # when there are more clusters
        # if the cluster is empty
        # get the closet points index and distance
        
        index1, index2, smalest_dist= distance_between_cluster( valid_data, label)
        # append the index and distan to 
        

        
        for row in valid_data:
            if index1 in row:
                pos1 = valid_data.index(row)
            if index2 in row:
                pos2 = valid_data.index(row)

        ele1 = label[index1]
        ele2 = label[index2]
        if len(valid_data[pos1]) > 1:
            j = len(new_cluster)-1
            while j >=0:
                if index1 in new_cluster[j][1]:
                    #print("cluster,new:", new_cluster[j])
                    index1 = new_cluster[j][0]
                    break
                j -= 1

        if len(valid_data[pos2]) > 1:
            j = len(new_cluster)-1
            while j >=0:
                if index2 in new_cluster[j][1]:
                    index2 = new_cluster[j][0]
                    
                    break
                j -= 1
        
        total = len(valid_data[pos1]) + len (valid_data[pos2])
        valid_data[pos1] = valid_data[pos1] + valid_data[pos2]
        
        new_cluster.append([i,valid_data[pos1]])
        
        i += 1
        valid_data.pop(pos2)
        #print(valid_data[index1_data],"index1 position")
        if index1 < index2:
            cluster.append([index1, index2, smalest_dist, total])
        if index1 > index2:
            cluster.append([index2, index1, smalest_dist, total])
         
        x_val = [label[el][0] for x in valid_data for el in x]
        
        y_val = [label[el][1] for x in valid_data for el in x]
        
        plt.scatter(x_val,y_val)
        plt.plot([ele1[0],ele2[0]],[ele1[1],ele2[1]])
        plt.pause(0.1) 
    plt.show()


