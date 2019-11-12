import csv
import pandas as pd
import os
import matplotlib.pyplot as plt
import sys

df1=pd.read_csv(os.path.join(os.getcwd(),"paretoFronts","KRRGZ_"+sys.argv[1]+"_7.csv"), usecols=[0])
df2=pd.read_csv(os.path.join(os.getcwd(),"paretoFronts","SD_"+sys.argv[1]+"_7.csv"), usecols=[0])
df3=pd.read_csv(os.path.join(os.getcwd(),"paretoFronts","RS_"+sys.argv[1]+"_7.csv"), usecols=[0])

#df=pd.DataFrame(columns=['KRRGZ', 'SD', 'RS'])

df=pd.concat([df1,df2,df3], axis=1)
df.columns=['KRRGZ', 'SD', 'RS']
print(df)
boxplot = df.boxplot(column=['KRRGZ', 'SD', 'RS'])
boxplot.set_ylabel("Time")
plt.grid(linestyle='dotted')
plt.savefig(os.path.join(os.getcwd(),'paretoPlots',sys.argv[1]+'.pdf'))
plt.show()

#print(boxplot)
#plt.savefig(os.path.join(os.getcwd(),'3-1-1.pdf'))