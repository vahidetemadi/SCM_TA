import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon
import settings
import a12
import numpy as np

import pdb
#pdb.set_trace()
#gets param from input, load as dataframe and perform effectsize test
def getEmptyDataframe(paramName):
	columns_param=pd.MultiIndex.from_product([settings.QIList, settings.dictOfParamsList.get(paramName)])
	dataFrameParam=pd.DataFrame('-',
	index=settings.dictOfParamsList.get(paramName),
	columns=columns_param )
	#print(dataFrameParam)
	return dataFrameParam

def getColumnList(listOfParams,param):
	columnList=[]
	for paramValue in listOfParams:
		for qi in settings.QIList:
			columnList.append(param+'_'+str(paramValue)+'_'+qi)
	return columnList

def fillDataframeTest(paramName:str, param:str):
	dataFrameTestCon=getEmptyDataframe(paramName)
	for param_index in settings.dictOfParamsList.get(paramName):
		for param_column in settings.dictOfParamsList.get(paramName):
			print(str(param_index)+'---->'+str(param_column))
			if param_index<param_column:
				print(dataFrameTestCon)
				dataFrameTest=getDataframeOfParamValue([param_index,param_column], param)
				#print(dataFrameTest)
				for qi in settings.QIList:
					print('its working')
					if qi is'GenerationalDistance' or qi is'Spacing':
						dataFrameTestCon.loc[param_index,(qi,param_column)]=round(a12.VD_A(dataFrameTest[param+'_'+str(param_column)+'_'+qi].tolist(), dataFrameTest[param+'_'+str(param_index)+'_'+qi].tolist()),2)
						dataFrameTestCon.loc[param_column,(qi,param_index)]=round(a12.VD_A(dataFrameTest[param+'_'+str(param_index)+'_'+qi].tolist(), dataFrameTest[param+'_'+str(param_column)+'_'+qi].tolist()),2)
					else:
						dataFrameTestCon.loc[param_index, (qi, param_column)]= round(a12.VD_A(dataFrameTest[param+'_'+str(param_index)+'_'+qi].tolist(), dataFrameTest[param+'_'+str(param_column)+'_'+qi].tolist()),2)
						dataFrameTestCon.loc[param_column,(qi,param_index)]=round(a12.VD_A(dataFrameTest[param+'_'+str(param_column)+'_'+qi].tolist(), dataFrameTest[param+'_'+str(param_index)+'_'+qi].tolist()),2)

					with open(os.path.join(os.getcwd(), "config", sys.argv[1],param+'.txt'), 'w+') as file:
						file.write(dataFrameTestCon.to_latex())
	#convert&save into latex table

def getDataframeOfParamValue(listOfParams, param):
	dataFramePaird=pd.DataFrame(np.random.rand(0,2*len(settings.QIList)))
	dataFramePaird.columns=getColumnList(listOfParams, param)
	row=[]
	for fileName in os.listdir(os.path.join(os.getcwd(),"config",sys.argv[1],"results",param)):
		data=yaml.load(open(os.path.join(os.getcwd(),"config",sys.argv[1],"results",param,fileName)))
		if(fileName.endswith('.yaml')):
			name=os.path.splitext(fileName)[0].split('_')
			if name[settings.dictOfParamIndex[param][0]]==str(listOfParams[0]) and name[settings.dictOfParamIndex[param][1]]==str(listOfParams[1]):
				row.clear()
				for paramValue in listOfParams:
					for qi in settings.QIList:
						row.append(data[param + '_' + str(paramValue)][qi])
				dataFramePaird.loc[len(dataFramePaird)] = row
	#print(dataFramePaird)
	return dataFramePaird



if __name__=="__main__":
	for key in settings.dictOfParamIndex:
		fillDataframeTest(key+'Params', key)
