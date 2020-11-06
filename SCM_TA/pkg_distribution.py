import sys
import os

def creat_dict_pkgs(pname):
    df_temp = pd.read_csv(os.path.join('bin', 'main', 'resources', 'bug-data', pname, 'efforts', 
    pname + 'MilestoneM5.txt'), sep='\t')
    cols = (df_temp.iloc[:, 3:]).columns
    dic_pkgs = {}
    for col in cols:
        dic_pkgs[col] = 0
    return dic_pkgs

def fill_dict_pkgs(pname, dict_pkgs):
    all_dfs = pd.DataFrame()
    for filename in os.listdir(os.path.join('bin', 'main', 'resources', 'bug-data', pname, 'efforts')):
        df_temp = pd.read_csv(os.path.join('bin', 'main', 'resources', 'bug-data', pname, 'efforts',
         filename), sep='\t')
        if all_dfs.empty:
            all_dfs = df_temp
        else:
            all_dfs = pd.concat([all_dfs, df_temp])
        '''
        df_temp = df_temp.iloc[:, 3:]
        cols = df_temp.columns
        for index, row in df_temp.iterrows():
            for col in cols:
                dict_pkgs[col] = dict_pkgs[col] + 1 if row[col] > 0 else dict_pkgs[col]
        '''
    return all_dfs.iloc[:, 3:]

def slice_and_plot(df_all, pname, dict_pkgs):
    l = len(df_all)
    print(l)
    for i in range(int(l / 40)):
        dict_pkgs = dict_pkgs.fromkeys(dict_pkgs, 0)
        df_temp = df_all.iloc[(i*40):(i+1)*40]
        cols = df_temp.columns
        for index, row in df_temp.iterrows():
            for col in cols:
                dict_pkgs[col] = dict_pkgs[col] + 1 if row[col] > 0 else dict_pkgs[col]
        print(dict_pkgs)
        plot_dict_pkgs(dict_pkgs, i, pname)

def plot_dict_pkgs(dict_pkgs, s_num, pname):
    plt.clf()
    plt.bar(range(len(dict_pkgs)), list(dict_pkgs.values()), align='center')
    plt.xticks(range(len(dict_pkgs)), list(dict_pkgs.keys()), fontsize=9, rotation=90)
    plt.savefig(os.path.join('results','self-adaptive', pname, 'Context', 'plots',
     'trend', str(s_num) + '.jpg'))

if  __name__ == '__main__':
    exec(open('imports.py').read())
    dict_pkgs = creat_dict_pkgs(sys.argv[1])
    df_all = fill_dict_pkgs(sys.argv[1], dict_pkgs)
    slice_and_plot(df_all, sys.argv[1], dict_pkgs)
    #plot_dict_pkgs(dict_pkgs)