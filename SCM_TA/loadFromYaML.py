import yaml
import os
import sys
import pandas as pd

listOfDataFrames=[]

for filename in os.listdir(os.path.join(os.getcwd(),"results",sys.argv[1])):
	if(filename.endswith('.yaml')):
		data=yaml.load(open(os.path.join(os.getcwd(),"results",sys.argv[1],filename)))
		name=os.path.splitext(filename)[0].split('_')
		if name[2]=='1' and name[3]=='1':
			name[1]=pd.DataFrame(pd.np.empty((0, 8)))
			name[1].columns=["NSGAIIITA_Hypervolume", "NSGAIIITA_GenerationalDistance", "NSGAIIITA_Spacing", "NSGAIIITA_Contribution",
							"KRRGZ_Hypervolume", "KRRGZ_GenerationalDistance","KRRGZ_Spacing", "KRRGZ_Contribution"]
			listOfDataFrames.append(name[1])

		index=listOfDataFrames.index(name[1])
		listOfDataFrames[index].loc[listOfDataFrames[index].shape[0]+1]=[data['NSGAIIITA']['Hypervolume'],data['NSGAIIITA']['GenerationalDistance'], data['NSGAIIITA']['Spacing'], data['NSGAIIITA']['Contribution']
																		 , data['KRRGZ']['Hypervolume'],data['KRRGZ']['GenerationalDistance'], data['KRRGZ']['Spacing'], data['KRRGZ']['Contribution'] ]

		print(listOfDataFrames.index(name[1]))