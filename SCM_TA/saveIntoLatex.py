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
    index_param=pd.MultiIndex.from_product([settings.projectList, [settings.getListOfFiles_byID('JDT'), settings.getListOfFiles_byID('Platform')], settings.devCategory])
    column_param=pd.MultiIndex.from_product([settings.QIList, settings.listOfApproaches])
    simpleStateDataFrame=pd.DataFrame('-',
    index=index_param,
    columns=columns_param)
    #print(dataFrameParam)
    #return simpleStateDataFrame


def saveAsLatex():
    simpleStateDataFrame=df.read_pickle(os.path.join(os.getcwd(),"simpleStateDataFrame.pkl"))
    with open(os.path.join(os.getcwd(), "results_"+sys.argv[3], sys.argv[1],param+'.txt'), 'w+') as file:
        file.write(simpleStateDataFrame.to_latex())


