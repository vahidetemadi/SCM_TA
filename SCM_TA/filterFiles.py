from shutil import copyfile
import os

def filterFiles():
    for fileName in os.listdir(os.path.join(os.getcwd(),'JDT')):
        if(filename.endswith('.yaml')):
            with open(fileName) as f:
                if f.find('NSGAIIITAGLS') != -1:
                    print(filename)
                    copyfile(os.path.join(os.getcwd(),'JDT',filename), os.path.join(os.getcwd(),'JDT2',filename))
                    continue