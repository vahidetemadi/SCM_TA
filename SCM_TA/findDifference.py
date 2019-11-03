from shutil import copyfile
import os
import mmap
from pathlib import Path
import pprint
import settings

allRequiredFiles_JDT=set()
allRequiredFiles_Platform=set()
alreadyExistingFiles_JDT=set()
alreadyExistingFiles_Platform=set()


def fillAllRequiredFiles():
    for milestoneName in settings.JDT_dataset_list.keys():
        for i in range(1,31):
            allRequiredFiles_JDT.add(milestoneName+'_'+str(i))
    for milestoneName in settings.Platform_dataset_list.keys():
        for i in range(1,31):
            allRequiredFiles_Platform.add(milestoneName+'_'+str(i))


def fillAlreadyExistingFiles():
    for folderName in settings.projectList:
        for fileName in os.listdir(os.path.join(os.getcwd(), 'results',folderName)):
            if not os.path.isdir(os.path.join(os.getcwd(),fileName)):
                if fileName.endswith('.yaml'):
                    name=os.path.splitext(fileName)[0].split('_')
                    if folderName=='JDT':
                        alreadyExistingFiles_JDT.add(name[1]+'_'+name[2])
                    elif folderName=='Platform':
                        alreadyExistingFiles_Platform.add(name[1]+'_'+name[2])


def printDiff():
    with open('JDTDiffs.txt', 'w') as jdt:
        print(len(allRequiredFiles_JDT))
        print(len(alreadyExistingFiles_JDT))
        diff=sorted(allRequiredFiles_JDT-alreadyExistingFiles_JDT)
        for item in diff:
            jdt.write(str(item)+'\n') 
    #(allRequiredFiles_JDT-alreadyExistingFiles_JDT).write(os.path.join(os.getcwd(),'JDTDiff'))
    with open('PlatformDiffs.txt', 'w') as platform:
        print(len(allRequiredFiles_Platform))
        print(len(alreadyExistingFiles_Platform))
        for item in sorted(allRequiredFiles_Platform-alreadyExistingFiles_Platform):
            platform.write(str(item)+'\n')
    #(allRequiredFiles_Platform-alreadyExistingFiles_Platform).write(os.path.join(os.getcwd(),'PlatformDiff'))
    '''
    print(allRequiredFiles_JDT-alreadyExistingFiles_JDT)
    print(allRequiredFiles_Platform-alreadyExistingFiles_Platform)
    '''

if __name__=="__main__":
    fillAllRequiredFiles()
    fillAlreadyExistingFiles()
    printDiff()
