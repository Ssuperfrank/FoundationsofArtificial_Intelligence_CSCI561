import numpy as np
import pandas as pd
from numpy import dot
import sys

def sigmoid(X):
    X[X>=0] = 1/(1+np.exp(-X[X>=0]))
    X[X<0] = np.exp(X[X<0])/(1+np.exp(X[X<0]))
    return X
    
def softmax(Z):
    Z = np.exp(Z - Z.max(axis = 0))
    Z = Z/Z.sum(axis = 0)
    return Z

def netTraining(train_image, train_label, neuronNum, outputNum, sampleBatch, alpha, epochs):
    res = None
    Wih = np.random.normal(0,np.sqrt(2/(train_image.shape[0]+neuronNum)),(neuronNum,train_image.shape[0]))     #   2/input * hidden      hidden * input 
    Who = np.random.normal(0,np.sqrt(2/(neuronNum + outputNum)),(outputNum,neuronNum))                       #   2/hidden * output     output  *  hidden
    b1 = np.zeros((neuronNum,1))                                 # 300*1    
    b2 = np.zeros((outputNum,1))
    parameters = {'Wih':Wih,'Who':Who,'b1':b1,'b2':b2}
    sampleTimes = train_image.shape[1]//sampleBatch
    epochsCount = 0
    index = list(range(train_image.shape[1]))
    while(epochsCount < epochs):
        np.random.shuffle(index)
        count = 0
        while(count < sampleTimes):
            k = index[count*sampleBatch:(count+1)*sampleBatch]
            batchImage = train_image[:,k]
            batchLabel = train_label[:,k]
            next_data = feedForward(batchImage,parameters)        
            grad = backProp(batchImage,batchLabel,next_data,parameters)
            parameters['Wih'] = parameters['Wih'] - alpha*grad['Wih']
            parameters['Who'] = parameters['Who'] - alpha*grad['Who']
            parameters['b1'] = parameters['b1'] - alpha*grad['b1']
            parameters['b2'] = parameters['b2'] - alpha*grad['b2']
            count = count + 1
        epochsCount = epochsCount + 1
        result = Discriminate(train_image, parameters)
        result_bool = (train_label == result)
        correctRate = result_bool.sum()/train_label.shape[1]
        # print('Epoch:'+ str(epochsCount)+'/'+str(epochs)+' correctRate:'+str(correctRate) + '\n')
    # return Discriminate(testImage, parameters)
    return parameters


def feedForward(X,p):
    Wih = p['Wih']
    Who = p['Who']
    b1 = p['b1']
    b2 = p['b2']
    a = sigmoid(dot(Wih,X)+b1)
    Z = dot(Who,a)+b2
    res = softmax(np.vstack((Z,np.ones((1,Z.shape[1])))))
    return {'a':a,'res':res}
    

def backProp(X,Y,Data,parameters):
    a = Data['a']
    Who = parameters['Who']
    sampleNum = a.shape[1]
    delta2 = Data['res']
    delta2[Y,np.arange(sampleNum)] = delta2[Y,np.arange(sampleNum)] - 1
    delta2 = np.delete(delta2,delta2.shape[0]-1,axis = 0)
    delta1 = dot(Who.T,delta2)*(a*(1-a))
    NW2 = dot(delta2,a.T)/sampleNum
    NW1 = dot(delta1,X.T)/sampleNum
    Nb2 = np.sum(delta2,axis = 1)/sampleNum
    Nb1 = np.sum(delta1,axis = 1)/sampleNum
    Nb2 = Nb2.reshape((Nb2.shape[0],1))
    Nb1 = Nb1.reshape((Nb1.shape[0],1))
    return {'Wih':NW1,'Who':NW2,'b1':Nb1,'b2':Nb2}


def Discriminate(X,parameters):
    next_data = feedForward(X,parameters)
    res = next_data['res']
    result = res.argmax(axis = 0)
    return result
    

if __name__ == "__main__":
    trainImage = None
    trainLabel = None
    testImage = None
    if(len(sys.argv) > 1):
        trainImage = np.array(pd.read_csv(sys.argv[1],header = None)).T           
        trainLabel = np.array(pd.read_csv(sys.argv[2],header = None)).T 
        testImage = np.array(pd.read_csv(sys.argv[3],header = None)).T
    else:
        trainImage = np.array(pd.read_csv('./dataset/train_image.csv',header = None)).T           
        trainLabel = np.array(pd.read_csv('./dataset/train_label.csv',header = None)).T 
        testImage = np.array(pd.read_csv('./dataset/test_image.csv',header = None)).T
       


    


    images = np.array_split(trainImage, 6, axis=1)
    labels = np.array_split(trainLabel, 6, axis=1)

    trainList = []
    labelList = []
    
    for x in range(6):
        
        validation_image = None
        validation_label = None
        arrays_image = []
        arrays_label = []
        for y in range(6):
            if y == x: 
                validation_image = images[y]
                validation_label = labels[y]
            else:
                arrays_image.append(images[y])
                arrays_label.append(labels[y])

        train_image = np.concatenate(arrays_image, axis = 1)
        train_label = np.concatenate(arrays_label, axis = 1)

     
        trainList.append({'train_image': train_image, 'validation_image': validation_image})
        labelList.append({'train_label': train_label, 'validation_label': validation_label})
    


    result = []
    for x in range(6):
        train_image = trainList[0]['train_image']
        train_label = labelList[0]['train_label']

        validation_image = trainList[0]['validation_image']
        validation_label = labelList[0]['validation_label']

        parameter = netTraining(train_image, train_label, 300, 10, 1000, 0.12, 60)
        predict = Discriminate(validation_image, parameter)

        result_bool = (validation_label == predict)
        correctRate = result_bool.sum()/validation_image.shape[1]  
        result.append({'parameter': parameter, 'correctRate': correctRate})
        print( ' correctRate: '+str(correctRate) + '\n\n')
    print( result )



    # result = Discriminate(testImage , netTraining(trainImage, trainLabel, 300, 10, 1000, 0.12, 60))
    # np.savetxt("test_predictions.csv", result,  fmt='%d', delimiter=",")

    # testLabel = np.array(pd.read_csv('./dataset/test_label.csv',header = None)).T      
    # result_bool = (testLabel == result)
    # correctRate = result_bool.sum()/testImage.shape[1]  
    # print( ' ccorrectRate: '+str(correctRate) + '\n\n')
       
