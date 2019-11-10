import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon
from sklearn import preprocessing
import settings
import a12

# input arguments example: python loadFromYaML&doStatTest.py JDT NSGAIIITA
class readResults:
    @staticmethod
    def loadDataIntoDataFrames(args):
        row = []
        dictOfDataFrames = {}
        for fileName in os.listdir(os.path.join(os.getcwd(),"results",args[1])):
            if(fileName.endswith('.yaml')):
                data=yaml.load(open(os.path.join(os.getcwd(),"results",args[1],fileName)))
                name=os.path.splitext(fileName)[0].split('_')
                milestoneName=name[1]
                if milestoneName not in dictOfDataFrames:
                    name[1]=pd.DataFrame(columns=loadResults.getColumnList())
                    #name[1].columns=getColumnList()
                    dictOfDataFrames.update({milestoneName:name[1]})
                row.clear()
                for algortihmName in settings.algorithmList:
                    for qi in settings.QIList:
                        row.append(float(data[algortihmName][qi]))
                dictOfDataFrames.get(milestoneName).loc[len(dictOfDataFrames.get(milestoneName))]=row
        return dictOfDataFrames

    @staticmethod
    def getColumnList():
        columnList=[]
        for algortihmName in settings.algorithmList:
            for qi in settings.QIList:
                columnList.append(algortihmName+'_'+qi)
        #print(columnList)
        return columnList

    @staticmethod
    def storeDataIntoCSV(dictOfDataFrames):
        for key, value in dictOfDataFrames.items():
            value.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], key+'.csv'))

class statitisticalTest:
    @staticmethod
    def getWilcoxonTest(dataframe, algortihmName1, algortihmName2, indicator):
        if indicator=='GenerationalDistance' or indicator=='Spacing':
            return wilcoxon(dataframe[algortihmName2+'_'+indicator].tolist(), dataframe[algortihmName1+'_'+indicator].tolist(), alternative='greater', zero_method= "zsplit")
        else:
            return wilcoxon(dataframe[algortihmName1+'_'+indicator].tolist(), dataframe[algortihmName2+'_'+indicator].tolist(),alternative='greater', zero_method= "zsplit")

    @staticmethod
    def getEffectSize(dataframe,algortihmName1, algortihmName2, indicator):
        if indicator=='GenerationalDistance' or indicator=='Spacing':
            return a12.VD_A(dataframe[algortihmName2+'_'+indicator].tolist(), dataframe[algortihmName1+'_'+indicator].tolist())
        else:
            return a12.VD_A(dataframe[algortihmName1+'_'+indicator].tolist(), dataframe[algortihmName2+'_'+indicator].tolist())

    @staticmethod
    def saveWilcoxonResultIntoFile(dictOfStatTestDataFrame):
        for key, value in dictOfStatTestDataFrame.items():
            value.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], key+'stat.csv'))

    @staticmethod
    def saveStatTestIntoFile(statTestDataFrame):
        statTestDataFrame.to_csv(os.path.join(os.getcwd(),"results",sys.argv[1], 'statTestResults.csv'))

if __name__=="__main__":
    loadResults=readResults
    dictOfDataFrames=loadResults.loadDataIntoDataFrames(sys.argv)
    loadResults.storeDataIntoCSV(dictOfDataFrames)
    # for key, value in dictOfDataFrames:
    #     print(key)
    statTest=statitisticalTest()
    statTestResults=pd.DataFrame()
    statTestDataFrame=pd.DataFrame(columns=["projectName", "fileName", "Axy", "QI","wilcoxonTest", "p-value", "effectSize"])
    resultRow=[]
    for key, value in dictOfDataFrames.items():
        for algortihmName1 in settings.algorithmList:
            for algortihmName2 in settings.algorithmList:
                if algortihmName1!=algortihmName2:
                    for qi in settings.QIList:
                        resultRow.extend([sys.argv[1],key,"A_"+algortihmName1+"_"+algortihmName2, qi])
                        resultRow.extend(statitisticalTest.getWilcoxonTest(value,algortihmName1, algortihmName2, qi))
                        resultRow.append(statitisticalTest.getEffectSize(value,algortihmName1, algortihmName2, qi))
                        #print(resultRow)
                        statTestDataFrame.loc[len(statTestDataFrame)]=resultRow
                        resultRow.clear()


    print(statTestDataFrame.groupby('Axy').size())
    statTest.saveStatTestIntoFile(statTestDataFrame)
    #supposed to work properly
