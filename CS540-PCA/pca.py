from scipy.linalg import eigh
import numpy as np
import matplotlib.pyplot as plt

# load the dataset from a provided  file,
# re-center it around the origin and
# return it as a NumPy array of floats
# para:  filename 
# return: array of provided file
def load_and_center_dataset(filename):
    #load the file
    x = np.load(filename)
    #center
    x_cent = x - np.mean(x, axis = 0)
    
    return x_cent
        

#  eigendecomposite the dataset
# return it as a Covariance Matrix
# para: dataset
# return: array of provided file
def get_covariance(dataset):
    n = len(dataset)
    # eigendecomposition 
    return np.dot(np.transpose(dataset), dataset)/(n-1)
        

#  eigendecomposite the dataset
# return it as a Covariance Matrix
# para: dataset, Covariance Matrix
# return: largest m eigvalue and correasponding eigvectors
def get_eig(S, m):
    # [0,0] at the largest position
    #but what for the egievector
    n = len(S)
    #find the last m elements
    eigvalue, eigvector = eigh(S, subset_by_index=[n-m,n-1])
    #build a new matrix with all zeros
    eigv_matrix = np.zeros( ( len(eigvalue), len(eigvalue) ), float )
    for n in eigvalue:
        print(n.as_integer_ratio())
    #fill the eigvalue into a diagonal matrix
    np.fill_diagonal(eigv_matrix,np.flip(eigvalue))
    
    
    return eigv_matrix, np.flip(eigvector, axis= 1)


#  find all eigvalues greater than m perc variance
# para: S,Covariance Matrix
# return:  eigvalue and correasponding eigvectors
def get_eig_perc(S, perc):
    # TODO: add your code here

    eigvalue = eigh(S, eigvals_only=True)
    #get the total number of eigvalue
    total = sum(eigvalue)
    #calculate the threshold
    threshold = total * perc
    #find the eigvalue and eigvectors
    eigvalue, eigvector = eigh(S, subset_by_value=(threshold, np.inf))
    
     #build a new matrix with all zeros
    eigv_matrix = np.zeros( ( len(eigvalue), len(eigvalue) ), float )
    #fill the eigvalue into a diagonal matrix
    np.fill_diagonal(eigv_matrix,np.flip(eigvalue))
    
    return eigv_matrix, np.flip(eigvector, axis= 1)
    
# project a image
# para: image, eigvectos
# return:  now  image
def project_image(img, U):
    #egivalue, eg_vec = get_eig(img, U)
   
    return np.dot(U, np.dot(np.transpose(img), U))


# display the new image
# para: origional image and projection image
def display_image(orig, proj):
    
    
    orig_re = np.reshape(orig, [32, 32])  # reshape image
    proj_re = np.reshape(proj, [32, 32])  # reshape image
    
    #transpose these matrix
    orig_trans = np.transpose(orig_re)
    proj_trans = np.transpose(proj_re)
    
    #build subplots and axis
    f, (ax1, ax2)= plt.subplots(1,2)
    #set their titles
    ax1.set_title('Original')
    ax2.set_title('Projection')
    
    #set the images into the places
    origion =  ax1.imshow(orig_trans,aspect  = 'equal')
    project =  ax2.imshow(proj_trans,aspect  = 'equal')
    #display the colorbar
    f.colorbar(origion, ax = ax1)
    f.colorbar(project, ax = ax2)
    plt.show()
    
x = x = [[1,0,0],[0,6,0],[0,0,3],[1,6,3]]
x = np.array(x)
S = get_covariance(x)

#print(Lambda)
#print(U)     
    
    
#x = load_and_center_dataset('YaleB_32x32.npy')
#S = get_covariance(x)
Lambda, U = get_eig(S, 2)
print(Lambda)
print(U)
# = project_image(x[0], U)
#print(projection)
#display_image(x[0], projection)