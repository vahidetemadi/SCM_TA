#import settings
from shutil import copyfile
import os
import mmap
from pathlib import Path
import pprint
import settings

dictOfOwnedNode={}

def fillDictOfOwnedNode():
    for folderName in settings.projectList:
        for fileName in os.listdir(os.path.join(os.getcwd(),'results',folderName)):
            if not os.path.isdir(os.path.join(os.getcwd(),'results',folderName,fileName)):
                if fileName.endswith('.yaml'):
                    name=os.path.splitext(fileName)[0].split('_')
                    if name[1] in dictOfOwnedNode:
                        dictOfOwnedNode[name[1]].append(name[2])
                    else:
                        dictOfOwnedNode[name[1]]=[name[2]]


def pruneParetoFrontFile():
    listOfFiles=[]
    for fileName in os.listdir(os.path.join(os.getcwd(), 'paretoFronts')):
        name=os.path.splitext(fileName)[0].split('_')
        if name[1] in dictOfOwnedNode:
            #print(dictOfOwnedNode[name[1]])
            if str(name[2]) not in dictOfOwnedNode[name[1]]:
                #listOfFiles.append(fileName)
                os.remove(os.path.join(os.getcwd(), 'paretoFronts',fileName))
                #print(os.path.join(str(Path(Path(os.getcwd()).parent).parent), 'paretoFronts',fileName))
        else:
            os.remove(os.path.join(os.getcwd(), 'paretoFronts',fileName))
            #print(os.path.join(str(Path(Path(os.getcwd()).parent).parent), 'paretoFronts',fileName))
    #print(listOfFiles)
'''
def pruneFinalResultsFiles:
     for fileNameP in os.listdir(os.join(os.getcwd(), 'AnalyzerResults')):
        name=os.path.splitext(filename)[0].split('_')
        if name[1] 
'''

if __name__=="__main__":
    fillDictOfOwnedNode()
    #print(dictOfOwnedNode)
    #pprint.pprint(dictOfOwnedNode)
    pruneParetoFrontFile()
    

