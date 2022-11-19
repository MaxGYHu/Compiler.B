#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Nov 17 21:29:56 2022

@author: hugaoyi
"""

from keras.datasets import mnist
(train_images, train_labels), (test_images, test_labels) = mnist.load_data()


import os
from tqdm import tqdm

# torch imports
import torch
import torch.optim as optim
import torch.nn as nn
import torch.nn.functional as F
from  torch.utils.data import DataLoader

# helper functions for computer vision
import torchvision.datasets as datasets
from torch.utils.data import Dataset
import torchvision.transforms as transforms
from PIL import Image
import numpy as np



class Relu(nn.Module):
    def __init__(self, input_shape=(32, 32), num_classes=100):
        super(Relu, self).__init__()
        #create the first layer
        self.relu = torch.nn.ReLU() 
        #then create the conv layer
        self.conv1 = torch.nn.Conv2d(1,10,5)
        #create the max pooling
        self.pool1 = torch.nn.MaxPool2d(2, 2)
        #create the second conv layer
        self.conv2 = torch.nn.Conv2d(10, 20, 5)
        #correct the max pooling
        self.pool2 = torch.nn.MaxPool2d(2, 2)
        #create the flatten layer
        self.linear2 = torch.nn.Linear(256,125)
        self.linear3 = torch.nn.Linear(125,100)


    def forward(self, input):
        x = self.pool1(F.relu(self.conv1(input)))
        x = self.pool1(F.relu(self.conv2(x)))
        # certain operations
        return x
    
class LRelu(nn.Module):
    def __init__(self, input_shape=(32, 32), num_classes=100):
        super(Relu, self).__init__()
        #create the first layer
        self.relu = torch.nn.LeakyReLU() 
        #then create the conv layer
        self.conv1 = torch.nn.Conv2d(1,10,5)
        #create the max pooling
        self.pool1 = torch.nn.MaxPool2d(2, 2)
        #create the second conv layer
        self.conv2 = torch.nn.Conv2d(10, 20, 5)
        #correct the max pooling
        self.pool2 = torch.nn.MaxPool2d(2, 2)
        #create the flatten layer
        self.linear2 = torch.nn.Linear(256,125)
        self.linear3 = torch.nn.Linear(125,100)


    def forward(self, input):
        x = self.pool1(F.Leakyrelu(self.conv1(input)))
        x = self.pool1(F.relu(self.conv2(x)))
        # certain operations
        return x


class Sig(nn.Module):
    def __init__(self, input_shape=(32, 32), num_classes=100):
        super(Relu, self).__init__()
        #create the first layer
        self.relu = torch.nn.Sigmoid() 
        #then create the conv layer
        self.conv1 = torch.nn.Conv2d(1,10,5)
        #create the max pooling
        self.pool1 = torch.nn.MaxPool2d(2, 2)
        #create the second conv layer
        self.conv2 = torch.nn.Conv2d(10, 20, 5)
        #correct the max pooling
        self.pool2 = torch.nn.MaxPool2d(2, 2)
        #create the flatten layer
        self.linear2 = torch.nn.Linear(256,125)
        self.linear3 = torch.nn.Linear(125,100)


    def forward(self, input):
        x = self.pool1(F.Sigmoid(self.conv1(input)))
        x = self.pool1(F.relu(self.conv2(x)))
        # certain operations
        return x
    
#set device
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

#hyperparameters
input_size = 784
num_classes = 10
learning_rate = 0.001
batch_size = 64
num_epochs = 1



    #non-linear 
# Load Data
train_dataset = datasets.MNIST(root='dataset/', train=True, transform = transforms.ToTensor(), download = True)
train_loader = DataLoader (dataset= train_dataset, batch_size = batch_size, shuffle= True)
test_dataset = datasets.MNIST (root='dataset/', trains = False, transform=transforms.ToTensor(), dounload =True)
test_loader = DataLoader(dataset = test_dataset, batch_size=batch_size, shuffle = True)


"""
data = list(np.random.randint(0, 255, size=(10, 3, 128, 128)))
targets = list(np.random.randint(2, size=(10)))

transform = transforms.Compose([transforms.Resize(64), transforms.ToTensor()])
dataset = MyDataset(data, targets, transform=transform)
train_loader2 = DataLoader(dataset, batch_size=5)
"""

# Initialize network
model = Relu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
# Train Network
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
           
        

        
#LeakyRelu
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
# Train Network
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
        


#Sig
model = Sig(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
# Train Network
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
        
        
        
#(3)

#baseline
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
#Adam
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        loss.backward()






#criterion = nn.CrossEntropyLoss()
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
#Adam
optimizer = optim.Adam(model.parameters(), Ir = learning_rate)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
        
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
#Adagrad
optimizer = torch.optim.Adagrad(model.parameters(), lr=0.01)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
        
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
#Adagrad
optimizer = torch.optim.Adagrad(model.parameters(), lr=0.01)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
     
    
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
#Adagrad
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)

#(4)
#baseline
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
#Adagrad
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
        
#dropout
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
model = nn.Dropout(p=0.2)
criterion = nn.CrossEntropyLoss()
#Adagrad
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        optimizer.step()
        print(data.shape)
        
#grad_clip
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
criterion = nn.CrossEntropyLoss()
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.4)
        optimizer.step()
        print(data.shape)

#BatchNorm
model = LRelu(input_size=input_size, nun_classes = num_classes).to(device)
# Loss and optimizer
model = nn.BatchNorm2d(model.parameters())
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)
for epoch in range (num_epochs) :
    for batch_idx,(data, targets) in enumerate(train_loader):
        data = data.to(device =device)
        targets = targets.to(device = device)
        data = data.reshape(data.shape[0], -1)
        
        scores = model(data)
        loss = criterion(scores,targets)
        optimizer.zero_gard()
        loss.backward()
        
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.4)
        optimizer.step()
      