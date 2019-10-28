import yaml
import os
import sys
import pandas as pd
from scipy.stats import wilcoxon
import settings
import a12


#gets param from input, load as dataframe and perform effectsize test
dataFrameTest={}

