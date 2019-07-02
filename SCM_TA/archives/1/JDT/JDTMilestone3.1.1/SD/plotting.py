import matplotlib.pyplot as plt
import pandas as pd
import os
from os.path import dirname, abspath
from pathlib import Path

'''
d=dirname(dirname(abspath('ploting.py')))
d=d+'/paretoFront'
'''

#os.chdir('C:/Users/DistLab3/git/SCM_TA/SCM_TA/archives/1/JDT/JDTMilestone3.1.1/SD')

for filename in os.listdir(os.getcwd()):
	if(filename.endswith('.csv')):
		print(filename)
		df=pd.read_csv(filename)
		print(df)
		df.plot(kind='scatter', x='Time', y='Cost', title=os.path.splitext(filename)[0])
		plt.show()
	