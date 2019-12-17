#Save into the latex file
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


def create_simpleDataFrame_multiIndex():
    index_param1=pd.MultiIndex.from_product([['JDT']
        , settings.getListOfFiles_byID('JDT').values()
        , settings.devCategoryList])
    index_param2=pd.MultiIndex.from_product([['Platform']
        , settings.getListOfFiles_byID('Platform').values()
        , settings.devCategoryList])
    column_param=pd.MultiIndex.from_product([settings.QIList
                                            , settings.listOfApproaches])
    simpleStateDataFrame1=pd.DataFrame('-',
    index=index_param1,
    columns=column_param)
    simpleStateDataFrame2=pd.DataFrame('-',
    index=index_param2,
    columns=column_param)
    simpleStateDataFrame=pd.concat([simpleStateDataFrame1, simpleStateDataFrame2], axis=0)
    #print(dataFrameParam)t
    #return simpleStateDataFrame
    return simpleStateDataFrame


def saveAsLatex():
    simpleStateDataFrame=pd.read_pickle(os.path.join(os.getcwd(),"simpleStateDataFrame.pkl"))
    with open(os.path.join(os.getcwd(),'Table.txt'), 'w+') as file:
        file.write(simpleStateDataFrame.to_latex(multirow=True, multicolumn=True, bold_rows=True, multicolumn_format='c' ))


