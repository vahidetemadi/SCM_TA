import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon
from sklearn import preprocessing
import matplotlib.pyplot as plt
from pylab import boxplot
from matplotlib.ticker import FormatStrFormatter
import matplotlib
from sklearn import preprocessing
import settings
import a12
import saveIntoLatex_AllDevs
from  matplotlib.patches import Patch
import subprocess


# input arguments example: python loadFromYaML&doStatTest.py JDT NSGAIIITAGLS AllDevs(CoreDevs)
class readResults:
    @staticmethod
    def loadDataIntoDataFrames(args, typeOfFExp):
        print(typeOfFExp)
        row = []
        dictOfDataFrames = {}
        for fileName in os.listdir(os.path.join(os.getcwd(),"results"+typeOfFExp,args[1])):
            if(fileName.endswith('.yaml')):
                data=yaml.load(open(os.path.join(os.getcwd(),"results"+typeOfFExp,args[1],fileName)))
                name=os.path.splitext(fileName)[0].split('_')
                milestoneName=name[1]
                if milestoneName not in dictOfDataFrames:
                    name[1]=pd.DataFrame(columns=loadResults.getColumnList())
                    #name[1].columns=getColumnList()
                    name[1].name=milestoneName
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
    def storeDataIntoCSV(dictOfDataFrames, typeOfFExp):
        for key, value in dictOfDataFrames.items():
            value.to_csv(os.path.join(os.getcwd(),"results"+typeOfFExp,sys.argv[1], key+'.csv'))


    @staticmethod
    def normalize(df):
        return (df-df.min())/(df.max()-df.min())

class statitisticalTest:
    @staticmethod
    def getWilcoxonTest(dataframe, algortihmName1, algortihmName2, indicator):
        try:
            if indicator=='GenerationalDistance' or indicator=='Spacing':
                if indicator=='Spacing':
                    return wilcoxon([ 100000000 if x==0 and algortihmName2=='RS' else x for x in dataframe[algortihmName2+'_'+indicator].tolist()]
                                    , [ 100000000 if x==0 and algortihmName1=='RS' else x for x in dataframe[algortihmName1+'_'+indicator].tolist()], alternative='greater', zero_method= "wilcox")
                else:
                    return wilcoxon(dataframe[algortihmName2+'_'+indicator].tolist(), dataframe[algortihmName1+'_'+indicator].tolist(), alternative='greater', zero_method= "wilcox")
            else:
                return wilcoxon(dataframe[algortihmName1+'_'+indicator].tolist(), dataframe[algortihmName2+'_'+indicator].tolist(),alternative='greater', zero_method= "wilcox")

        except ValueError:
            return [0.0, 0.5]

    @staticmethod
    def getEffectSize(dataframe,algortihmName1, algortihmName2, indicator):
        if indicator=='GenerationalDistance' or indicator=='Spacing':
            if indicator=='Spacing':
                return a12.VD_A([100000000 if x==0 and algortihmName2=='RS' else x for x in dataframe[algortihmName2+'_'+indicator].tolist()] 
                                , [100000000 if x==0 and algortihmName1=='RS' else x for x in dataframe[algortihmName1+'_'+indicator].tolist()])
            else:
                return a12.VD_A(dataframe[algortihmName2+'_'+indicator].tolist(), dataframe[algortihmName1+'_'+indicator].tolist())
        else:
            return a12.VD_A(dataframe[algortihmName1+'_'+indicator].tolist(), dataframe[algortihmName2+'_'+indicator].tolist())

    @staticmethod
    def saveWilcoxonResultIntoFile(dictOfStatTestDataFrame, typeOfFExp):
        for key, value in dictOfStatTestDataFrame.items():
            value.to_csv(os.path.join(os.getcwd(),"results"+typeOfFExp,sys.argv[1], key+'stat.csv'))

    @staticmethod
    def saveStatTestIntoFile(statTestDataFrame, typeOfExp):
        statTestDataFrame.to_csv(os.path.join(os.getcwd(),"results"+typeOfExp,sys.argv[1], 'statTestResults.csv'))

    @staticmethod
    def fillWinTieLose_onlyWilc(statTestDataFrame, dataFrameWinTieLose, typeOf):
        if typeOf=='Win':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[ 
                    (statTestDataFrame['wilcoxonTest']>=150) 
                    & (statTestDataFrame['Ax']==algortihmName)
                    & (statTestDataFrame['p-value']<=0.05)])
        if typeOf=='Tie':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[
                    (   (statTestDataFrame['wilcoxonTest']<150) 
                        & (statTestDataFrame['wilcoxonTest']>100)
                        & (statTestDataFrame['p-value']>0.05)
                    )
                    | 
                    (   (statTestDataFrame['wilcoxonTest']>=150) 
                        & (statTestDataFrame['p-value']>0.05)
                    )
                    & (statTestDataFrame['Ax']==algortihmName)])
        if typeOf=='Lose':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[
                    (statTestDataFrame['wilcoxonTest']<=100)
                    & (statTestDataFrame['Ax']==algortihmName)
                    & (statTestDataFrame['p-value']>0.05)])

    def fillWinTieLose_onlyWilc_enhanced(statTestDataFrame, dataFrameWinTieLose):
        dataFrameWinTieLose.fillna(0,inplace=True)
        print(dataFrameWinTieLose)
        statTestDataFrame_temp=statTestDataFrame[(statTestDataFrame.Ax=='NSGAIIITAGLS') |
                                                 ((statTestDataFrame.Ax=='KRRGZ') & (statTestDataFrame.Ay=='RS'))]

        for index,row in statTestDataFrame_temp.iterrows():
            algortihmName1=statTestDataFrame_temp.loc[index]['Ax']
            algortihmName2=statTestDataFrame_temp.loc[index]['Ay']
            res= statitisticalTest.makeComparison_wilcoxon(algortihmName1, algortihmName2, index, statTestDataFrame_temp)
            if res==1:
                dataFrameWinTieLose.loc[algortihmName1, 'Win']+=1;
                dataFrameWinTieLose.loc[algortihmName2, 'Lose']+=1;
            elif res==-1:
                dataFrameWinTieLose.loc[algortihmName1, 'Lose']+=1;
                dataFrameWinTieLose.loc[algortihmName2, 'Win']+=1;
            elif res==0:
                dataFrameWinTieLose.loc[algortihmName1, 'Tie']+=1;
                dataFrameWinTieLose.loc[algortihmName2, 'Tie']+=1;
        #return dataFrameWinTieLose

    
    def makeComparison_wilcoxon(algortihmName1, algortihmName2, rowOfStatTestDataFrame, statTestDataFrame_temp):
        if (    (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ax']==algortihmName1) 
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ay']==algortihmName2)        
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['wilcoxonTest']>=200)
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['p-value']<=0.05)
            ):
            return 1;
        elif( (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ax']==algortihmName2) 
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ay']==algortihmName1)        
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['wilcoxonTest']>=200)
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['p-value']<=0.05)
            ):
            return -1;
        else:
            return 0;
 

    def fillWinTieLose_withA12(statTestDataFrame, dataFrameWinTieLose, typeOf):
        if typeOf=='Win':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[ 
                    (statTestDataFrame['effectSize']>=0.8) 
                    & (statTestDataFrame['Ax']==algortihmName)])
        if typeOf=='Tie':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[
                    (statTestDataFrame['effectSize']<0.8) & (statTestDataFrame['effectSize']>0.7)
                    & (statTestDataFrame['Ax']==algortihmName)])
        if typeOf=='Lose':
            for algortihmName in settings.algorithmList:
                dataFrameWinTieLose.loc[algortihmName, typeOf]=len(statTestDataFrame[
                    (statTestDataFrame['effectSize']<=0.7)
                    & (statTestDataFrame['Ax']==algortihmName)])



    def fillWinTieLose_withA12_enhanced(statTestDataFrame, dataFrameWinTieLose):
        for col in dataFrameWinTieLose.columns:
            dataFrameWinTieLose[col].values[:] = 0

        print(dataFrameWinTieLose)
        statTestDataFrame_temp=statTestDataFrame[(statTestDataFrame.Ax=='NSGAIIITAGLS') |
                                                 ((statTestDataFrame.Ax=='KRRGZ') & (statTestDataFrame.Ay=='RS'))]

        for index,row in statTestDataFrame_temp.iterrows():
            algortihmName1=statTestDataFrame_temp.loc[index]['Ax']
            algortihmName2=statTestDataFrame_temp.loc[index]['Ay']
            res= statitisticalTest.makeComparison_effectSize(algortihmName1, algortihmName2, index, statTestDataFrame_temp)
            if res==1:
                dataFrameWinTieLose.loc[algortihmName1, 'Win']+=1;
                dataFrameWinTieLose.loc[algortihmName2, 'Lose']+=1;
            elif res==-1:
                dataFrameWinTieLose.loc[algortihmName1, 'Lose']+=1;
                dataFrameWinTieLose.loc[algortihmName2, 'Win']+=1;
            elif res==0:
                dataFrameWinTieLose.loc[algortihmName1, 'Tie']+=1;
                dataFrameWinTieLose.loc[algortihmName2, 'Tie']+=1;


    def makeComparison_effectSize(algortihmName1, algortihmName2, rowOfStatTestDataFrame, statTestDataFrame_temp):
        if (    (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ax']==algortihmName1) 
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ay']==algortihmName2)        
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['effectSize']>=0.8)
            ):
            return 1;
        elif( (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ax']==algortihmName2) 
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['Ay']==algortihmName1)        
                and (statTestDataFrame_temp.loc[rowOfStatTestDataFrame]['effectSize']>=0.8)
            ):
            return -1;
        else:
            return 0;



def plotAndSave_stackedChart(dataFrameWinTieLose, typeOfComparison, pathPart):
    dataFrameWinTieLose=dataFrameWinTieLose.rename(index=settings.index)
    print(dataFrameWinTieLose)
    hatches=['\\', '\\', '\\','//', '//', '//','.', '.', '.']
    ax=dataFrameWinTieLose.plot.bar(stacked=True, width=.20, colors=['lime', 'cornflowerblue', 'lightcoral'])
    win = Patch(facecolor='lime', label='Win', hatch='\\')
    tie = Patch(facecolor='cornflowerblue', label='Tie', hatch='//')
    lose = Patch(facecolor='lightcoral', label='Lose', hatch='.')
    plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.2),fancybox=False, shadow=False, ncol=3, handles=[win, tie, lose])
    labels = []
    for j in dataFrameWinTieLose.columns:
        for i in dataFrameWinTieLose.index:
            label=str(dataFrameWinTieLose.loc[i][j])
            labels.append(label)
    patches=ax.patches
    for label, rect, hatch in zip(labels, patches, hatches):
        width = rect.get_width()
        rect.set_hatch(hatch)
        if width > 0 and int(label)>0:
            x = rect.get_x()
            y = rect.get_y()
            height = rect.get_height()
            ax.text(x + width/2., y + height/2., label, ha='center', va='center', size='x-small')
            ax
    ax.grid(b=True, linestyle='dotted', axis='y')   
    plt.tight_layout()
    print(pathPart)
    matplotlib.rc('pdf', fonttype=42)
    #plt.savefig(os.path.join(os.getcwd(),"results"+settings.devCategory.get(pathPart),sys.argv[1]+'_stackChartPlot',sys.argv[1]+'_winTieLose_'+typeOfComparison+'.pdf'))
    pathOfFig=str(os.path.join(os.getcwd(),"results"+settings.devCategory.get(pathPart),sys.argv[1]+'_stackChartPlot'))
    nameOfFig=str(sys.argv[1]+'_winTieLose_'+typeOfComparison)
    savePdfViaSvg(plt, nameOfFig, pathOfFig)

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
        matplotlib.rc('pdf', fonttype=42)
        plt.savefig(os.path.join(os.getcwd(),"results",sys.argv[1]+'_boxPlotsForQIs',sys.argv[1]+'_'+qi+'_boxplot.pdf'))


def fillTableOfSimpleStat(devKey):
    simpleStateDataFrame=pd.read_pickle(os.path.join(os.getcwd(),"simpleStateDataFrame.pkl"))
    #print(simpleStateDataFrame)
    for MSFile in settings.getDict(devKey).values():#need to iterate over two lists same time
        for qi in settings.QIList:
            for approach in settings.listOfApproaches:
                entryValue = str(round(MSFile[settings.index_reverse.get(approach)+'_'+qi].mean(),2))+';'+ str(round(MSFile[settings.index_reverse.get(approach)+'_'+qi].std(),2))
                if devKey=='CoreDevs' and approach=='RS' and qi=='Spacing':
                    entryValue='-'
                simpleStateDataFrame.loc[(sys.argv[1], settings.getListOfFiles_byID(sys.argv[1]).get(settings.getListOfFiles(sys.argv[1]).get(MSFile.name))) ,(qi, approach)]= entryValue
    simpleStateDataFrame.fillna(0, inplace=True)
    print(simpleStateDataFrame)
    simpleStateDataFrame.to_pickle(os.path.join(os.getcwd(),"simpleStateDataFrame.pkl"))  


def savePdfViaSvg(fig,nameOfFig, pathOfFig, **kwargs):
    fig.savefig(os.path.join(pathOfFig, nameOfFig+".svg"), format="svg", **kwargs)
    incmd = ["inkscape", os.path.join(pathOfFig, nameOfFig+".svg"), "--export-pdf="+os.path.join(pathOfFig, nameOfFig+".pdf"),
             "--export-pdf-version=1.5"] #"--export-ignore-filters",
    subprocess.call(incmd, shell=True)
    os.remove(os.path.join(pathOfFig, nameOfFig+".svg"))

#the script starts at this line
if __name__=="__main__":
    simpleStateDataFrame=pd.DataFrame()
    #create(if not exist) and save table of results---using a multi-index strucutre
    if not os.path.exists(os.path.join(os.getcwd(),"simpleStateDataFrame.pkl")):
        #simpleStateDataFrame=getEmptyDataframe_simpleDataFrame_multiIndex()
        simpleStateDataFrame=saveIntoLatex_AllDevs.create_simpleDataFrame_multiIndex()
        simpleStateDataFrame.to_pickle(os.path.join(os.getcwd(),"simpleStateDataFrame.pkl"))

        
    for keyDev,valueDev in settings.devCategory.items():
        print('/////////---> '+keyDev)
        loadResults=readResults
        dictOfDataFrames=loadResults.loadDataIntoDataFrames(sys.argv, valueDev)
        features=[]





        features=[]
        for qi in settings.QIList_needToBeNormalized:
            feature=[]
            for an in settings.algorithmList:
                feature.append(an+'_'+qi)
            features.append(feature)    

        for key,value in dictOfDataFrames.items():
            for feature in features:
                dictOfDataFrames[key][feature]=dictOfDataFrames[key][feature].apply(lambda row:(row[feature]-row[feature].min())/(row[feature].max()-row[feature].min()), axis=1)
                dictOfDataFrames[key].fillna(0, inplace=True)
            if key=='PlatformMilestoneM8':
                    print(dictOfDataFrames[key][['NSGAIIITAGLS_GenerationalDistance', 'KRRGZ_GenerationalDistance','RS_GenerationalDistance',
                                                    'NSGAIIITAGLS_Spacing', 'KRRGZ_Spacing','RS_Spacing' ]])

        #print(dictOfDataFrames['PlatformMilestone3.0'].to_string())







        #make item normalize
        # for key, value in dictOfDataFrames.items():
        #     #dictOfDataFrames[key]=loadResults.normalize(value)
        #     print(dictOfDataFrames[key])

        loadResults.storeDataIntoCSV(dictOfDataFrames, valueDev)
        # for key, value in dictOfDataFrames:
        #     print(key)
        statTest=statitisticalTest
        #statTestResults=pd.DataFrame()
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

        # settings.setDict(dictOfDataFrames, keyDev)
        # settings.setDF(statTestDataFrame, keyDev) 

        if keyDev == 'AllDevs':
            settings.AllDevs = dictOfDataFrames
        elif keyDev == 'CoreDevs':
            settings.CoreDevs = dictOfDataFrames

        if keyDev == 'AllDevs':
            settings.All_Devs = statTestDataFrame
        elif keyDev == 'CoreDevs':
            settings.Core_Devs = statTestDataFrame
            print('/////////////////')
        #print(statTestDataFrame[(statTestDataFrame.wilcoxonTest>150) & (statTestDataFrame.Ay=='NSGAIIITAGLS')].round(2))
        print(statTestDataFrame)
        

        fillTableOfSimpleStat(keyDev)
        saveIntoLatex_AllDevs.saveAsLatex()
        #statTest.saveStatTestIntoFile(statTestDataFrame, value)

    for keyNameDev, statTestDF in settings.getStatTestDFs().items():
        dataFrameWinTieLose=pd.DataFrame(index=settings.algorithmList, columns=['Win', 'Tie', 'Lose']) 
        # print(statTestDataFrame[(statTestDataFrame['wilcoxonTest']>300) | (statTestDataFrame['effectSize']>0.8)].groupby('Axy').size())
        # print(statTestDataFrame[(statTestDataFrame['wilcoxonTest']<300) | (statTestDataFrame['effectSize']<0.8)].groupby('Axy').size())
        
        #fill and plot stacked charts using only wilcoxson
        #for item in settings.statTest:
        #   statitisticalTest.fillWinTieLose_onlyWilc(statTestDF, dataFrameWinTieLose, item)


        statitisticalTest.fillWinTieLose_onlyWilc_enhanced(statTestDF, dataFrameWinTieLose);
        
        print('senario[wilcoxson]--->'+ keyNameDev)
        plotAndSave_stackedChart(dataFrameWinTieLose, "wilcoxson",keyNameDev )

        #fill and plot stacked charts using effect size
        # for item in settings.statTest:
        #     statitisticalTest.fillWinTieLose_withA12(statTestDF, dataFrameWinTieLose, item)

        statitisticalTest.fillWinTieLose_withA12_enhanced(statTestDF, dataFrameWinTieLose);   

        print('senario[effectSize]--->'+ keyNameDev)
        plotAndSave_stackedChart(dataFrameWinTieLose, "A12", keyNameDev)

 


    #plot and save boxplot of datasets per QIs which relatively compare SD and KRRGZ
    if sys.argv[3]== 'AllDevs':
        plotAndSave_boxPlotCharts(dictOfDataFrames)

