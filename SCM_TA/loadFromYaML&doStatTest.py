import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon
import settings
import a12

# input arguments example: python loadFromYaML&doStatTest.py JDT NSGAIIITA
class readResults:
    row=[]
	@staticmethod
	def loadDataIntoDataFrames(args):
		dictOfDataFrames={}
		for filename in os.listdir(os.path.join(os.getcwd(),"results",args[1])):
			if(filename.endswith('.yaml')):
				data=yaml.load(open(os.path.join(os.getcwd(),"results",args[1],filename)))
				name=os.path.splitext(filename)[0].split('_')
				milestoneName=name[1]
				if milestoneName not in dictOfDataFrames:
					name[1]=pd.DataFrame(pd.np.empty(0, len(settings.algorithmList)*len(settings.QIList)))
					name[1].columns=getColumnList()
					dictOfDataFrames.update({milestoneName:name[1]})
            row.clear()
            for algortihmName in settings.algorithmList:
                for qi in settings.QIList:
                    row.append(data[algortihmName][qi])
			dictOfDataFrames.get(milestoneName).loc[dictOfDataFrames.get(milestoneName).shape[0]+1]=row

		return dictOfDataFrames	

    @staticmethod
    def getColumnList():
        columnList=[]
        for algortihmName in settings.algorithmList:
            for qi in settings.QIList:
                columnList.append(algortihmName+'_'+qi)

	@staticmethod
	def storeDataIntoCSV(dictOfDataFrames):		
		for key, value in dictOfDataFrames.items():
			value.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], key+'.csv'))


class statitisticalTest:
	@staticmethod
	def getWilcoxonTest(dataframe, algortihmName,indicator):
		if indicator=='GenerationalDistance' or indicator=='Spacing':
			return wilcoxon(dataframe[algortihmName+'_'+indicator].tolist(), dataframe[args[2]+'_'+indicator].tolist(), alternative='greater')
		else:
			return wilcoxon(dataframe[args[2]+'_'+indicator].tolist(), dataframe[algortihmName+'_'+indicator].tolist(),alternative='greater')


    @staticmethod
    def getEffectSize(dataframe,algortihmName, indicator):
        if indicator=='GenerationalDistance' or indicator=='Spacing':
            return a12.VD_A(dataframe[algortihmName+'_'+indicator].tolist(), dataframe[args[2]+'_'+indicator].tolist())
        else:
            return a12.VD_A(dataframe[args[2]+'_'+indicator].tolist(), dataframe[algortihmName+'_'+indicator].tolist())

	@staticmethod
	def saveWilcoxonResultIntoFile(dictOfStatTestDataFrame):
		for key, value in dictOfStatTestDataFrame.items():
			value.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], key+'stat.csv'))

    @staticmethod
    def saveStatTestIntoFile(statTestDataFrame):
        statTestDataFrame.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], 'statTestResults.csv'))


loadResults=readResults()
dictOfDataFrames=loadResults.loadDataIntoDataFrames(sys.argv)
loadResults.storeDataIntoCSV(dictOfDataFrames)
for key, value in dictOfDataFrames:
    dictOfDataFrames.get(key).


statTest=statitisticalTest()
statTestResults=pd.dataframe()
statTestdataframe=pd.DataFrame(columns=["projectName", "fileName", "Axy", "QI","wilcoxonTest", "p-value", "effectSize"])
resultRow=[]
for key, value in dictOfDataFrames.items():
	print(key)
    for algortihmName in settings.algorithmList:
        if algortihmName!=args[2]:
            resultRow.clear()
        	for qi in settings.QIList:
                result.extend([args[1],key],"A_"+args[1]+"_"algortihmName, qi)
        		result.extend(statitisticalTest.getWilcoxonTest(value,qi))
                result.extend(statitisticalTest.getEffectSize(value,algortihmName,qi))
        		statTestdataframe.loc[i]=result
        		print(result)
	
statTest.saveStatTestIntoFile(statTestdataframe)

#supposed to work properly
