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

NEF=['50000','100000', '150000', '200000', '250000']

for filename in os.listdir(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+'\\'+sys.argv[4]):
	if(filename.endswith('.csv')):
		print(filename)
		df=pd.read_csv(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+'\\'+sys.argv[4]+'\\'+filename)
		print(df)
		name=os.path.splitext(filename)[0].split('_')
		if(sys.argv[4]=='KRRGZ'):
			df2=pd.read_csv(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+
				'\\SD\\'+name[0]+'_'+name[1]+'_SD_'+name[3]+os.path.splitext(filename)[1])
		else:
			df2=pd.read_csv(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+
				'\\KRRGZ\\'+name[0]+'_'+name[1]+'_KRRGZ_'+name[3]+os.path.splitext(filename)[1])

		ax=df.plot(kind='scatter', x='Time', y='Cost', title=name[0]+'_'+name[1]+'_'+name[3])
		df2.plot(kind='scatter', x='Time',c='DarkRed' ,y='Cost', title=name[0]+'_'+name[1]+'_'+name[3], ax=ax)	
		plt.savefig(os.getcwd()+'\\archives\\'+sys.argv[1]+'\\'+sys.argv[2]+'\\'+sys.argv[3]+'\\'+name[0]+'_'+name[1]+'_'+name[3]+'.png')
	