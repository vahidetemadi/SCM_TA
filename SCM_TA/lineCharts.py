import pandas as pd
import numpy as np

rootPath="results\\self-adaptive\\"
fileName=sys.argv[1]

df=pd.read_csv(rootPath+fileName)

print(df)