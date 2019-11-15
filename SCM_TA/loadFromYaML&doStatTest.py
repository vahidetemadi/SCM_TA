import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon
from sklearn import preprocessing
import matplotlib.pyplot as plt
from pylab import boxplot
from matplotlib.ticker import FormatStrFormatter
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


    @staticmethod
    def normalize(df):
        return (df-df.min())/(df.max()-df.min())

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

    @staticmethod
    def fillWinTieLose(statTestDataFrame, dataFrameWinTieLose, typeOf):
        print(dataFrameWinTieLose)
        if typeOf=='Win':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[ 
                    (statTestDataFrame['effectSize']>=0.7) 
                    & (statTestDataFrame['Ax']==algortihmName)])
        if typeOf=='Tie':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[
                    (statTestDataFrame['effectSize']<0.7) & (statTestDataFrame['effectSize']>0.4)
                    & (statTestDataFrame['Ax']==algortihmName)])
        if typeOf=='Lose':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[
                    (statTestDataFrame['effectSize']<=0.4)
                    & (statTestDataFrame['Ax']==algortihmName)])

def plotAndSave_stackedChart(dataFrameWinTieLose):
    dataFrameWinTieLose=dataFrameWinTieLose.rename(index=settings.index)
    print(dataFrameWinTieLose)
    ax=dataFrameWinTieLose.plot.bar(stacked=True, width=.20, colors=['lime', 'cornflowerblue', 'red'])
    plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.2),fancybox=False, shadow=False, ncol=3)
    labels = []
    for j in dataFrameWinTieLose.columns:
        for i in dataFrameWinTieLose.index:
            label=str(dataFrameWinTieLose.loc[i][j])
            labels.append(label)
    patches=ax.patches
    for label, rect in zip(labels, patches):
        width = rect.get_width()
        if width > 0 and int(label)>0:
            x = rect.get_x()
            y = rect.get_y()
            height = rect.get_height()
            ax.text(x + width/2., y + height/2., label, ha='center', va='center', size='x-small')
    ax.grid(b=True, linestyle='dotted', axis='y')
    plt.tight_layout()
    plt.savefig(os.path.join(os.getcwd(),'results',sys.argv[1]+'_stackChartPlot',sys.argv[1]+'_winTieLose.pdf'))

def plotAndSave_boxPlotCharts(dictOfDataFrames):
    colors = ['black', 'red']
    columns=['SD','KRRGZ']
    for qi in settings.QIList:
        dfOfPairs=pd.DataFrame()
        for key , value in settings.getListOfFiles(sys.argv[1]).items():
            #dfOfPairs.iloc[0:0]
            # dictOfPairs.update({'SD':dictOfDataFrames.get(key)[sys.argv[2]+'_'+qi]})
            # dictOfPairs.update({'KRRGZ':dictOfDataFrames.get(key)[sys.argv[2]+'_'+qi]})
            #print(dictOfPairs)
            for item in settings.algorithmListUnderCom:
                #dfOfPairs.join(dictOfDataFrames.get(key)[item+'_'+qi])
                dfOfPairs=pd.concat([dfOfPairs,dictOfDataFrames.get(key)[item+'_'+qi]], axis=1)
                dfOfPairs.rename(columns={item+'_'+qi: settings.mapToRightName.get(item)})
            #bp=boxplot(dictOfPairs, positions=[position, position+1])
            # bp=boxplot(dfOfPairs, positions=[position+1, position+2])
            # plt.set_xticklabels(['SD','KRRGZ'])
        
        # box=dfOfPairs.boxplot(patch_artist = True,  return_type='both',)
        # for row_key, (ax,row) in box.iteritems():
        #     print(row_key)
        fig = plt.figure()
        ax = plt.subplot(111)
        j=1;
        #set for access to name of dataset
        lbs_bottom=[]
        x_ticks_top_pos=[]
        x_ticks_bottom_pos=[]
        for i in range(len(settings.getListOfFiles(sys.argv[1]).items())*2):
            if i%2==0:
                lbs_bottom.append('SD')
            else:
                lbs_bottom.append('KRRGZ')
            bp=ax.boxplot(dfOfPairs.ix[:,i].values, positions = [j], widths=.7, showfliers=False, patch_artist=True)
            x_ticks_bottom_pos.append(j)
            if j%3==1:
                x_ticks_top_pos.append((j+(j+1))/2)
            if i%2==1:
                bp['boxes'][0].set(color='cornflowerblue')
                j+=2
            else:
                bp['boxes'][0].set(color='lime')
                j+=1
            bp['medians'][0].set(color='red')
        #ax_bottom = ax.twiny()
        ax.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
        ax.tick_params(labelsize=4, labelbottom=True,labeltop=False,top=False, bottom=True)
        ax.set_xticks(x_ticks_bottom_pos)
        ax.set_xticklabels(lbs_bottom)
        ax.grid(b=True, linestyle='dotted')
        print(x_ticks_bottom_pos)
        print(x_ticks_top_pos)
        ax_top = ax.twiny()
        ax_top.tick_params(labelsize=4, labelbottom=False,labeltop=True,top=True, bottom=False)
        ax_top.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
        ax_top.minorticks_off()
        ax_top.set_xlim(ax.get_xlim())
        ax_top.set_xticks(x_ticks_top_pos)
        ax_top.set_xticklabels(settings.getListOfFiles_byID(sys.argv[1]).values())
        #ax_top.grid(b=True, linestyle='dotted')
        #plt.grid(True)
        #plt.grid(linestyle='dotted')
        #plt.yaxis.grid(True)
        plt.savefig(os.path.join(os.getcwd(),'results',sys.argv[1]+'_boxPlotsForQIs',sys.argv[1]+'_'+qi+'_boxplot.pdf'))
    




#the script starts at this line
if __name__=="__main__":
    loadResults=readResults
    dictOfDataFrames=loadResults.loadDataIntoDataFrames(sys.argv)
    #make item normalize
    # for key, value in dictOfDataFrames.items():
    #     #dictOfDataFrames[key]=loadResults.normalize(value)
    #     print(dictOfDataFrames[key])

    loadResults.storeDataIntoCSV(dictOfDataFrames)
    # for key, value in dictOfDataFrames:
    #     print(key)
    statTest=statitisticalTest()
    statTestResults=pd.DataFrame()
    statTestDataFrame=pd.DataFrame(columns=["projectName", "fileName", "Ax", "Ay", "QI","wilcoxonTest", "p-value", "effectSize"])
    resultRow=[]
    for key, value in dictOfDataFrames.items():
        for algortihmName1 in settings.algorithmList:
            for algortihmName2 in settings.algorithmList:
                if algortihmName1!=algortihmName2:
                    for qi in settings.QIList:
                        resultRow.extend([sys.argv[1],key, algortihmName1, algortihmName2, qi])
                        resultRow.extend(statitisticalTest.getWilcoxonTest(value,algortihmName1, algortihmName2, qi))
                        resultRow.append(statitisticalTest.getEffectSize(value,algortihmName1, algortihmName2, qi))
                        #print(resultRow)
                        statTestDataFrame.loc[len(statTestDataFrame)]=resultRow
                        resultRow.clear()
    dataFrameWinTieLose=pd.DataFrame(index=settings.algorithmList, columns=['Win', 'Tie', 'Lose']) 
    # print(statTestDataFrame[(statTestDataFrame['wilcoxonTest']>300) | (statTestDataFrame['effectSize']>0.8)].groupby('Axy').size())
    # print(statTestDataFrame[(statTestDataFrame['wilcoxonTest']<300) | (statTestDataFrame['effectSize']<0.8)].groupby('Axy').size())
    for item in settings.statTest:
        statitisticalTest.fillWinTieLose(statTestDataFrame, dataFrameWinTieLose, item)


    # plotStacked charts
    plotAndSave_stackedChart(dataFrameWinTieLose)

    #plot and save table of results



    #plot and save boxplot of datasets per QIs which relatively compare SD and 
    plotAndSave_boxPlotCharts(dictOfDataFrames)


    statTest.saveStatTestIntoFile(statTestDataFrame)
    #supposed to work properly
