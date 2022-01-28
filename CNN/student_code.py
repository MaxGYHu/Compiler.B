# python imports
import os
from tqdm import tqdm

# torch imports
import torch
import torch.nn as nn
import torch.optim as optim

# helper functions for computer vision
import torchvision
import torchvision.transforms as transforms

import numpy as np

class LeNet(nn.Module):
    def __init__(self, input_shape=(32, 32), num_classes=100):
        super(LeNet, self).__init__()
        #create the first layer
        #relu first 
        self.relu = torch.nn.ReLU() 
        #then create the conv layer
        self.conv2d1 = torch.nn.Conv2d(3 ,6, 5 ,1, padding=0)
        #create the max pooling
        self.pool1 = torch.nn.MaxPool2d(2, 2)
        #create the second conv layer
        self.conv2d2 = torch.nn.Conv2d(6, 16, 5,1)
        #correct the max pooling
        self.pool2 = torch.nn.MaxPool2d(2, 2)
        #create the flatten layer
        self.flatten3 = torch.nn.Flatten()
        #cal row and column
        #create 3 linear layers
        self.linear1 = torch.nn.Linear(16*5*5, 256)
        self.linear2 = torch.nn.Linear(256,125)
        self.linear3 = torch.nn.Linear(125,100)


    def forward(self, x):
    
        shape_dict = {
                }
        x = self.relu(self.conv2d1(x))
        x = self.pool1(x)
       
        #add list into dict
        list_info = x.size()
        shape_dict[1] = list(list_info)
        
        #add the 2nd conv layer to shape
        x = self.relu(self.conv2d2(x))
        x = self.pool2(x)
        list_info = x.size()
        shape_dict[2] = list(list_info)
        
        #add the flatten layer to shape
        x = self.flatten3(x)
        list_info = x.size()
        shape_dict[3] = list(list_info)

        #add the linear layer 
        x = self.relu(self.linear1(x))
        list_info = x.size()
        shape_dict[4] =list(list_info)

        #add the 2nd linear layer
        x = self.relu(self.linear2(x))
        list_info = x.size()
        shape_dict[5] = list(list_info)

        #add the 3rd linear layer
        x = self.linear3(x)
        list_info = x.size()
        shape_dict[6] = list(list_info)



        # certain operations
        return x, shape_dict


def count_model_params():
    '''
    return the number of trainable parameters of LeNet.
    '''
    model = LeNet()
    model_params = 0.0
    for k,v in  model.named_parameters():
        #transfer to numpy
        arr = np.array(list(v.size()))
        #add to the model_params
        model_params += np.prod(arr)
    return model_params/1e6


def train_model(model, train_loader, optimizer, criterion, epoch):
    """
    model (torch.nn.module): The model created to train
    train_loader (pytorch data loader): Training data loader
    optimizer (optimizer.*): A instance of some sort of optimizer, usually SGD
    criterion (nn.CrossEntropyLoss) : Loss function used to train the network
    epoch (int): Current epoch number
    """
    model.train()
    train_loss = 0.0
    for input, target in tqdm(train_loader, total=len(train_loader)):
        ###################################
        # fill in the standard training loop of forward pass,
        # backward pass, loss computation and optimizer step
        ###################################

        # 1) zero the parameter gradients
        optimizer.zero_grad()
        # 2) forward + backward + optimize
        output, _ = model(input)
        loss = criterion(output, target)
        loss.backward()
        optimizer.step()

        # Update the train_loss variable
        # .item() detaches the node from the computational graph
        # Uncomment the below line after you fill block 1 and 2
        train_loss += loss.item()

    train_loss /= len(train_loader)
    print('[Training set] Epoch: {:d}, Average loss: {:.4f}'.format(epoch+1, train_loss))

    return train_loss


def test_model(model, test_loader, epoch):
    model.eval()
    correct = 0
    with torch.no_grad():
        for input, target in test_loader:
            output, _ = model(input)
            pred = output.max(1, keepdim=True)[1]
            correct += pred.eq(target.view_as(pred)).sum().item()

    test_acc = correct / len(test_loader.dataset)
    print('[Test set] Epoch: {:d}, Accuracy: {:.2f}%\n'.format(
        epoch+1, 100. * test_acc))

    return test_acc

"""
data = torch.zeros(1,3,32,32)
model = LeNet()
out, shape= model(data)
print(shape)
print(count_model_params())
"""
