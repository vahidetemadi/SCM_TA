import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon
import settings
import a12


#gets param from input, load as dataframe and perform effectsize test
def getEmptyDataframe(paramName):
	columns_param=pd.MultiIndex.from_product([settings.QIList, settings.paramName])

	dataFrameParam=pd.DataFrame(np.random.rand(len(settings.paramName), len(settings.paramName)
	* len(settings.QIList)), 
	index=[settings.paramName],
	columns=columns_param )
	
	return dataFrameParam

 def getColumnList(listOfParams,param):
        columnList=[]
        for paramValue in listOfParams:
            for qi in settings.QIList:
                columnList.append(param+'_'+paramValue+'_'+qi)

def fillDataframeTest(paramName, param):
	dataFrameTest=getEmptyDataframe(paramName)
	for param_index in settings.paramName:
	    for param_column in settings.paramName:
	        if param_index!=param_column:
	        	dataFrameTest=getDataframeOfParamValue([param_index,param_column], param)
	        	for qi in settings.QIList:
	        		if qi=='GenerationalDistance' or qi=='Spacing':
		        		dataFrameTest.insert(loc=param_index,column=[qi, crossover_column], 
		        			a12.VD_A(dataFrameTest[param+'_'+param_column+'_'+qi].tolist(), dataFrameTest[args[param+'_'+param_index+'_'+qi]+'_'+indicator].tolist()))
		        		dataFrameTest.insert(loc=param_column,column=[qi,param_index],
		        			a12.VD_A(a12.VD_A(dataFrameTest[param+'_'+param_index+'_'+qi].tolist(), dataFrameTest[args[param+'_'+param_column+'_'+qi]+'_'+indicator].tolist())))
		        	else:
		        		dataFrameTest.insert(loc=param_index,column=[qi, crossover_column], 
		        			a12.VD_A(dataFrameTest[param+'_'+param_index+'_'+qi].tolist(), dataFrameTest[args[param+'_'+param_column+'_'+qi]+'_'+indicator].tolist()))
		        		dataFrameTest.insert(loc=param_column,column=[qi,param_index],
		        			a12.VD_A(a12.VD_A(dataFrameTest[param+'_'+param_column+'_'+qi].tolist(), dataFrameTest[args[param+'_'+param_index+'_'+qi]+'_'+indicator].tolist())))

	#convert&save into latex table
	with open(os.path.join(os.getcwd(),"config",args[1],param+'.txt'),'w+') as file:
		file.write(dataFrameTest.to_latex()) 

def getDataframeOfParamValue(listOfParams, param):
	dataFramePaird=pd.dataframe(np.random.rand(0,2*len(settings.QIList)))
	dataFramePaird.columns=getColumnList(listOfParams, param)
	row=[]
	for fileName in os.listdir(os.path.join(os.getcwd(),"config",args[1],"results",param)):
		data=yaml.load(open(os.path.join(os.getcwd(),"config",args[1],"results",param,fileName)))
		if(filename.endswith('.yaml')):
			name=os.path.splitext(filename)[0].split('_')
			if name[settings.dictOfParamIndex[param][0]]==listOfParams[0] and name[settings.dictOfParamIndex[param][1]]==listOfParams[1]:
				row.clear()
				for paramValue in listOfParams:
					for qi in settings.QIList:
						row.append(data[param+'_'+paramValue][qi])
				dataFramePaird.append(row)
	return dataFramePaird



if __name__=="__main__":
	for key in settings.dictOfParamIndex
		fillDataframeTest(key+'Params', key)
