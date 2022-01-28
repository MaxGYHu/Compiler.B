import numpy as np
from matplotlib import pyplot as plt
import csv
import math
import random
from numpy.linalg import inv
from decimal import Decimal
# Feel free to import other packages, if needed.
# As long as they are supported by CSL machines.


def get_dataset(filename):
    """
    TODO: implement this function.

    INPUT: 
        filename - a string representing the path to the csv file.

    RETURNS:
        An n by m+1 array, where n is # data points and m is # features.
        The labels y should be in the first column.
    """
    dataset = []
    
    with open(filename, newline='') as csvfile:

        reader = csv.DictReader(csvfile)
        for line in reader:
           # convert them all into float
           #del the first column tho
            del line['IDNO']
            line['BODYFAT'] = float(line['BODYFAT'])
            line['DENSITY'] =float(line['DENSITY'])
            line['AGE'] =int(line['AGE'])
            line['WEIGHT'] = float(line['WEIGHT'])
            line['HEIGHT'] = float(line['HEIGHT'])
            line['ADIPOSITY'] = float(line['ADIPOSITY'])
            line['NECK'] = float(line['NECK'])
            line['CHEST'] = float(line['CHEST'])
            line['ABDOMEN'] = float(line['ABDOMEN'])
            line['HIP'] = float(line['HIP'])
            line['THIGH'] = float(line['THIGH'])
            line['KNEE'] = float(line['KNEE'])
            line['ANKLE'] = float(line['ANKLE'])
            line['BICEPS'] = float(line['BICEPS'])
            line['FOREARM'] = float(line['FOREARM'])
            line['WRIST'] = float(line['WRIST'])
            
            dataset.append(list(line.values()))
            
    dataset = np.array(dataset)
    return dataset


def print_stats(dataset, col):
    """
    TODO: implement this function.

    INPUT: 
        dataset - the body fat n by m+1 array
        col     - the index of feature to summarize on. 
                  For example, 1 refers to density.

    RETURNS:
        None
    """
    #add all the data we need into this list
    needed_data = []
    for row in dataset:
        needed_data.append(row[col])
    # find out how many items we have
    num = len(needed_data)
    print( num )
    #calculate the sum within that list
    mean = sum(needed_data)/num
    #round to 2 decimals place
    print( f'{mean:.2f}' )
    #calculate the standard devation
    total_diff = 0 
    for one in needed_data:
        #the difference between the value and mean
        diff = one - mean
        #square it
        diff = diff*diff
        #add it to total
        total_diff += diff
    final = math.sqrt(total_diff / (num -1))
    #round to 2 decimals place
    print( f'{final:.2f}' )
    
    
    
    
   


def regression(dataset, cols, betas):
    """
    TODO: implement this function.

    INPUT: 
        dataset - the body fat n by m+1 array
        cols    - a list of feature indices to learn.
                  For example, [1,8] refers to density and abdomen.
        betas   - a list of elements chosen from [beta0, beta1, ..., betam]

    RETURNS:
        mse of the regression model
    """
    mse = 0
    #go thought the dataset
    for row in dataset:
        row_total = 0
        #find the sum of betas*row
        for pos in range(len(cols)):
          
           row_total += row[cols[pos]]*betas[pos+1]
        #add the beta_0
        row_total += betas[0]
        #minize the y
        row_total -= row[0]
        #saure the total 
        mse += row_total*row_total
    
    mse = mse/len(dataset)
    
    return float('{:.2f}'.format(mse))


def gradient_descent(dataset, cols, betas):
    """
    TODO: implement this function.

    INPUT: 
        dataset - the body fat n by m+1 array
        cols    - a list of feature indices to learn.
                  For example, [1,8] refers to density and abdomen.
        betas   - a list of elements chosen from [beta0, beta1, ..., betam]

    RETURNS:
        An 1D array of gradients
    """
    grads = []
    #first item will mutiply with 1
    row = -1
    for n in range(len(betas)):
        total = 0
        for line in dataset:
            #set the total within this row as 0
            row_total = 0
            for pos in cols:
              
               row_total += line[cols[pos]]*betas[pos+1]
            #find the x1
            x1 = line[cols[row]]
            #first x1 should be 1
            if row == -1:
                x1 = 1
            #add betas_0 and minize y
            row_total += betas[0]
            row_total -= line[0]
          
            #add to total
            total += row_total*x1
        #increment row by 1
        row += 1
        
        final = (2 / len(dataset))* total
        grads.append(round(final,2))
    
    #transfer to array
    grads = np.array(grads)
    return grads


def iterate_gradient(dataset, cols, betas, T, eta):
    """
    TODO: implement this function.

    INPUT: 
        dataset - the body fat n by m+1 array
        cols    - a list of feature indices to learn.
                  For example, [1,8] refers to density and abdomen.
        betas   - a list of elements chosen from [beta0, beta1, ..., betam]
        T       - # iterations to run
        eta     - learning rate

    RETURNS:
        None
    """
   #go through the loop T times
    for lit in range(T):
        #find the grad for each time
        grads = gradient_descent(dataset, cols, betas)
        g_list = list(grads)
        # go through the grads
        for n in range(len(g_list)):
            # get the minize num we want
            minize = g_list[n]*eta
            #do the calculation 
            value = round( betas[n] - minize, 2)
            
            betas.append(value)
        
        cut = len(betas)/2
        betas = betas[int(cut):]
        #get the current MSE
        MSE = regression(dataset, cols, betas)

        print(lit+1, MSE, ' '.join( map(str, betas) ) )

def compute_betas(dataset, cols):
    """
    TODO: implement this function.

    INPUT: 
        dataset - the body fat n by m+1 array
        cols    - a list of feature indices to learn.
                  For example, [1,8] refers to density and abdomen.

    RETURNS:
        A tuple containing corresponding mse and several learned betas
    """
    betas = []
    
    #list of x
    x = []
    #list of y
    y = []
    #we want the line be the 1d list within the 
    #x list
    for row in dataset:
        #inital it as 1 since first item is always be 1
        line = [1]
        for pos in cols:
           #add all x value in same row into line
           line.append(row[pos])
        x.append(line)
        
        y.append(row[0])

        
    x = np.array(x)
    y = np.array(y)
    mid = np.dot(np.transpose(x), x)
    #inverse the mid
    midinv =  np.linalg.inv(mid)
    #calculate the betas
    betas = np.dot( np.dot( midinv, np.transpose(x) ) , y)
 
    mse = regression(dataset, cols, betas)

    
    return  (mse, *betas)
    
    
    
    



def predict(dataset, cols, features):
    """
    TODO: implement this function.

    INPUT: 
        dataset - the body fat n by m+1 array
        cols    - a list of feature indices to learn.
                  For example, [1,8] refers to density and abdomen.
        features- a list of observed values

    RETURNS:
        The predicted body fat percentage value
    """
    
    collect = compute_betas(dataset, cols)
    #extract the betas
    betas = collect[1:]
    #set the result as 0
    result = 0
    for x in range(len(features)):
        result += features[x]*betas[x+1]
    # use formular to get days
    result += betas[0]
    return result


def synthetic_datasets(betas, alphas, X, sigma):
    """
    TODO: implement this function.

    Input:
        betas  - parameters of the linear model
        alphas - parameters of the quadratic model
        X      - the input array (shape is guaranteed to be (n,1))
        sigma  - standard deviation of noise

    RETURNS:
        Two datasets of shape (n,2) - linear one first, followed by quadratic.
    """
    # it contains the item of linear
    new_x = []
    # it contains the item of quadratic
    new_x2 = []
   
    betas = list(betas)
    for pos in range(len(X)):
        #set the z

       
        z = np.random.normal(0, sigma)
 
        #item of linear
        new = betas[0] + betas[1]*X[pos][0] + z
        #we want a different z for quadratic
        z = np.random.normal(0, sigma)
        #item of quadratic
        new2 = alphas[0] + alphas[1]*X[pos][0]*X[pos][0] + z

        new_x.append(new)
        new_x2.append(new2)
    #the final answer of linear
    first = []

    for n in range(len(X)):
        row = []
        #add each item of MSE
        #print(*new_x[n])
        

        row.append(new_x[n])
        #copy of x
        row.append(X[n])
    
        
        first.append(row)
    

    first = np.array(first, dtype='float')
    #final answer of quadratic
    second = []
    for n in range(len(X)):
        row = [] 
        #add each item of MSE
        row.append(new_x2[n])
        #copy of x
        row.append(X[n])
     
        
        second.append(row)
    second = np.array(second, dtype='float')
    
    return first, second


def plot_mse():
    from sys import argv
    
        
    if len(argv) == 2 and argv[1] == 'csl':
        import matplotlib
        matplotlib.use('Agg')

    # TODO: Generate datasets and plot an MSE-sigma graph
    X = []
    for num in range(1000):
        x = random.randint(-100, 100)
        X.append([x])
    #convert it to array
    X=np.array(X)
    #create betas
    betas = []
    for num in range(2):
        x = random.randint(1, 100)
        betas.append(x)
    #create alphas
    alphas = []
    for num in range(2):
        x = random.randint(1, 100)
        alphas.append(x)
    #the line represent linear
    y1 = []
    #the line represent quadratic
    y2 = []
    #x axis
    s_list = []
    for p in range(-3,6):
        sigma = pow(10,p)
        s_list.append(p)
        first, second = synthetic_datasets(betas, alphas, X, sigma)
        #get the tuple of output
        tuple1 = compute_betas(first, cols = [1])
        tuple2 = compute_betas(second, cols = [1])
        #convert to decimal to prevent math domin error
        MSE1 = Decimal(tuple1[0])
        MSE2 = Decimal(tuple2[0])
        y1.append(MSE1.ln())
        y2.append(MSE2.ln())


    fig = plt.figure()
    plt.xlabel('sigma')
    plt.ylabel('MSEs')
    plt.plot(s_list, y1, marker='o', label = "Linear MSEs")
    plt.plot(s_list, y2, marker='o', label = "Quadratic MSEs")
    plt.legend() 
    fig.savefig("mse.pdf")
    
    
if __name__ == '__main__':    ### DO NOT CHANGE THIS SECTION ##
    plot_mse()
