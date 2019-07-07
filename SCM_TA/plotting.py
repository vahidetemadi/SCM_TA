import matplotlib.pyplot as plt
import pandas as pd
import os
from os.path import dirname, abspath
from pathlib import Path
import sys

'''
d=dirname(dirname(abspath('ploting.py')))
d=d+'/paretoFront'
'''

#os.chdir('C:/Users/DistLab3/git/SCM_TA/SCM_TA/archives/1/JDT/JDTMilestone3.1.1/SD')
JDT_fileNames=['JDTMilestone3.1.0', 'JDTMilestone3.1.1', 'JDTMilestone3.1.2', 'JDTMilestoneM1','JDTMilestoneM2', 'JDTMilestoneM3'
				,'JDTMilestoneM4', 'JDTMilestoneM5', 'JDTMilestoneM6']

for filename in os.listdir(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+'\\'+sys.argv[4]):
	if(filename.endswith('.csv')):
		print(filename)
		df=pd.read_csv(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+'\\'+sys.argv[4]+'\\'+filename)
		print(df)
		df.plot(kind='scatter', x='Time', y='Cost', title=os.path.splitext(filename)[0])
		plt.savefig(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+'\\'+sys.argv[4]+'\\'+os.path.splitext(filename)[0]+'.png')
	