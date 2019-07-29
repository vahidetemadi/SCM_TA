import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon


class readResults:
	@staticmethod
	def loadDataIntoDataFrames(args):
		dictOfDataFrames={}
		for filename in os.listdir(os.path.join(os.getcwd(),"results",args[1])):
			if(filename.endswith('.yaml')):
				data=yaml.load(open(os.path.join(os.getcwd(),"results",args[1],filename)))
				name=os.path.splitext(filename)[0].split('_')
				milestoneName=name[1]
				if milestoneName not in dictOfDataFrames:
					name[1]=pd.DataFrame(pd.np.empty((0, 8)))
					name[1].columns=["NSGAIIITA_Hypervolume", "NSGAIIITA_GenerationalDistance", "NSGAIIITA_Spacing", "NSGAIIITA_Contribution",
									"KRRGZ_Hypervolume", "KRRGZ_GenerationalDistance","KRRGZ_Spacing", "KRRGZ_Contribution"]
					dictOfDataFrames.update({milestoneName:name[1]})

				dictOfDataFrames.get(milestoneName).loc[dictOfDataFrames.get(milestoneName).shape[0]+1]=[data['NSGAIIITA']['Hypervolume'],data['NSGAIIITA']['GenerationalDistance'], data['NSGAIIITA']['Spacing'], data['NSGAIIITA']['Contribution']
																				 , data['KRRGZ']['Hypervolume'],data['KRRGZ']['GenerationalDistance'], data['KRRGZ']['Spacing'], data['KRRGZ']['Contribution'] ]

				#print(dictOfDataFrames.get(milestoneName))
		return dictOfDataFrames	

	@staticmethod
	def storeDataIntoCSV(dictOfDataFrames):		
		for key, value in dictOfDataFrames.items():
			value.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], key+'.csv'))


class statitisticalTest:
	@staticmethod
	def getWilcoxonTest(dataframe, indicator):
		if indicator=='GenerationalDistance' or indicator=='Spacing':
			return wilcoxon(dataframe['KRRGZ_'+indicator].tolist(), dataframe['NSGAIIITA_'+indicator].tolist(), alternative='greater')
		else:
			return wilcoxon(dataframe['NSGAIIITA_'+indicator].tolist(), dataframe['KRRGZ_'+indicator].tolist(),alternative='greater')

	@staticmethod
	def saveWilcoxonResultIntoFile(dictOfStatTestDataFrame):
		for key, value in dictOfStatTestDataFrame.items():
			value.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], key+'stat.csv'))



loadResults=readResults()
dictOfDataFrames=loadResults.loadDataIntoDataFrames(sys.argv)
loadResults.storeDataIntoCSV(dictOfDataFrames)

statTest=statitisticalTest()
dictOfStatTestDataFrame={}
for key, value in dictOfDataFrames.items():
	dataframeName=str('wilcoxonDataframeResults_'+key)
	dataframeName=pd.DataFrame(pd.np.empty((4, 2)), index=["Hypervolume", "GenerationalDistance", "Spacing", "Contribution"],
								columns=["wilcoxonTestResult", "p-value"])

	print(key)
	for i, row in dataframeName.iterrows():
		result=statTest.getWilcoxonTest(value,i)
		dataframeName.loc[i]=result
		print(result)
	dictOfStatTestDataFrame.update({key:dataframeName})
	statTest.saveWilcoxonResultIntoFile(dictOfStatTestDataFrame)

#supposed to work properly
