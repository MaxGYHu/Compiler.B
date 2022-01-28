# -*- coding: utf-8 -*-
"""
Created on Mon Mar  1 00:19:59 2021

@author: PC
"""
#@print("​Train Epoch: {%d} Accuracy: {%d} ({0:.2%}) Loss: {%.3f}".format(epoch, inputs, inputs/60000, running_loss/2000))
import numpy as np
x = [[1,0,0],[0,6,0],[0,0,3],[1,6,3]]
x = np.array(x)
print(x)
print(np.mean(x, axis = 0))
x_cent = x - np.mean(x, axis = 0)
print(x_cent)
n= 4
l = np.dot(np.transpose(x_cent), x_cent)/(n-1)
print(l)
eigvalue, eigvector = eigh(S, subset_by_index=[n-m,n-1])