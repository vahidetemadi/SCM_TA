#import settings
from shutil import copyfile
import os
import mmap
from pathlib import Path

dictOfOwnedNode={}

def fillDictOfOwnedNode():
    for fileNameP in os.listdir(os.getcwd()):
        if os.path.isdir(os.path.join(os.getcwd(),fileNameP)):
            for fileName in os.listdir(os.path.join(os.getcwd(),fileNameP)):
                if fileName.endswith('.yaml'):
                    name=os.path.splitext(filename)[0].split('_')
                    if name[1] in dictOfOwnedNode:
                        dictOfOwnedNode[name[1]].append(name[2])
                    else:
                        dictOfOwnedNode[name[1]]=[name[2]]

def pruneParetoFrontFile():
    for fileName in os.listdir(os.join(Path(Path(os.getcwd()).parent).parent, 'paretoFronts')):
        name=os.path.splitext(filename)[0].split('_')
        if name[1] in dictOfOwnedNode:
            if name[2] not in dictOfOwnedNode[name[1]]:
                os.rmdir(os.join(Path(Path(os.getcwd()).parent).parent, 'paretoFronts',fileName))
        else:
            os.rmdir(os.join(Path(Path(os.getcwd()).parent).parent, 'paretoFronts',fileName))
'''
def pruneFinalResultsFiles:
     for fileNameP in os.listdir(os.join(os.getcwd(), 'AnalyzerResults')):
        name=os.path.splitext(filename)[0].split('_')
        if name[1] 
'''

if __name__=="__main__":
    fillDictOfOwnedNode()
    print(dictOfOwnedNode)
    

