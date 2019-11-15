JDT_dataset_list={
    'JDTMilestone3.1.1':'1',
    'JDTMilestone3.1.2':'2',
    'JDTMilestone3.1':'3',
    'JDTMilestoneM1':'4',
    'JDTMilestoneM2':'5',
    'JDTMilestoneM3':'6',
    'JDTMilestoneM4':'7',
    'JDTMilestoneM5':'8',
    'JDTMilestoneM6':'9',
}

JDT_dataset_list_byId={
    '1':'MS3.1.1',
    '2':'MS3.1.2',
    '3':'MS3.1',
    '4':'MSM1',
    '5':'MSM2',
    '6':'MSM3',
    '7':'MSM4',
    '8':'MSM5',
    '9':'MSM6',
}

Platform_dataset_list={
    'PlatformMilestone3.0':'1',
    'PlatformMilestone3.1':'2',
    'PlatformMilestoneM2':'3',
    'PlatformMilestoneM3':'4',
    'PlatformMilestoneM4':'5',
    'PlatformMilestoneM5':'6',
    'PlatformMilestoneM6':'7',
    'PlatformMilestoneM7':'8',
    'PlatformMilestoneM8':'9',
    'PlatformMilestoneM9':'10',
}

Platform_dataset_list_byId={
    '1':'MS3.0',
    '2':'MS3.1',
    '3':'MSM2',
    '4':'MSM3',
    '5':'MSM4',
    '6':'MSM5',
    '7':'MSM6',
    '8':'MSM7',
    '9':'MSM8',
    '10':'MSM9',
}

def getListOfFiles(keyName):
    if keyName=='JDT':
        return JDT_dataset_list
    if keyName=='Platform':
        return Platform_dataset_list
def getListOfFiles_byID(keyName):
    if keyName=='JDT':
        return JDT_dataset_list_byId
    if keyName=='Platform':
        return Platform_dataset_list_byId

projectList=['JDT','Platform']

algorithmList=['NSGAIIITAGLS', 'KRRGZ', 'RS']
algorithmListUnderCom=['NSGAIIITAGLS', 'KRRGZ']
mapToRightName={'NSGAIIITAGLS':'SD', 'KRRGZ':'KRRGZ'}

QIList=['Hypervolume', 'GenerationalDistance', 'Spacing', 'Contribution']

crossoverParams=[0.5, 0.6, 0.7, 0.8, 0.9]
mutationParams=[0.01, 0.05, 0.10, 0.20, 0.30]
populationParams=[100, 200, 300, 400, 500]

dictOfParamsList={'crossoverParams':[0.5, 0.6, 0.7, 0.8, 0.9],'mutationParams':[0.01, 0.05, 0.10, 0.20, 0.30], 'populationParams':[100, 200, 300, 400, 500] }

dictOfParamIndex={'crossover':[1,2], 'mutation':[3,4], 'population':[5,6]}

statTest=["Win", "Tie", "Lose"]

index={"NSGAIIITAGLS":"SD", "KRRGZ":"KRRGZ", "RS":"RS"}
