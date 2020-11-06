import sys
import argparse
import os

USAGE_MSG = \
    """TBD..."""
USAGE_DEC = \
    """TBD... """
PATH = ""

def parse_args():
    """parse the input parameters"""
    parser = argparse.ArgumentParser(usage=USAGE_MSG,
                                     description=USAGE_DEC,
                                     epilog="",
                                     formatter_class=argparse.RawDescriptionHelpFormatter,
                                     add_help=False)
    parser.add_argument("-ds", "--dataset", help="the name of the dataset")
    parser.add_argument("-w", "--window", help="the name of the dataset")
    parser.add_argument("-d", "--developer", help="the name of the approach")
    parser.add_argument("-ba", "--batch", help="run number")
    parser.add_argument("-p", "--population", help="solution number")
    parser.add_argument("-nfe", "--nfe", help="solution number")
    parser.add_argument("-c", "--crossover",  help="solution number")
    parser.add_argument("-m", "--mutation", help="solution number")
    parser.add_argument("-a", "--alpha", help="solution number")
    parser.add_argument("-b", "--beta", help="solution number")
    #parser.add_argument("-prob", "--prob", h)

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(1)
    return parser.parse_args()

def create_df():
    df = pd.DataFrame(columns = ['W', 'D', 'B', 'P', 'C', 'M', 'A', 'B', 'result', 'prob'])
    return df

def get_all_dfs(args, df):
    i = 0
    '''trends are appended to main dataframe df'''
    for filename in os.listdir(os.path.join(args['dataset'] , "Context")):
        if filename.endswith('.csv') and not ('probOverTime') in filename:
            splitedFileName = filename.split('_')
            splitedFileName = splitedFileName[ : len(splitedFileName) - 1]
            #print(splitedFileName)
            for name in splitedFileName:
                if 'Cr' in name:
                    names = list(name)
                    df.loc[str(i), names[0]] = name[2:]
                else:
                    names = list(name)
                    df.loc[str(i), names[0]] = name[1:]

            temp_df = pd.read_csv(os.path.join(args['dataset'] , "Context", filename))
            temp_df_prob = pd.read_csv(os.path.join(args['dataset'] , "Context", filename.replace('_1', '_probOverTime')))
            df._set_value(str(i), 'result', temp_df)
            df._set_value(str(i), 'prob', temp_df_prob)
            i = i + 1
    return df

def plot_dfs(args, df):
    plots = ['CoT', 'IDoT']
    temp_df = df.groupby('W')
    for name, group_df in temp_df:
        print(group_df)
        for index, row in group_df.iterrows():
            '''plotting the cost and id over time'''
            for p in plots:
                df_result = row['result']
                s = df_result[p + '_static'][0]
                a = df_result[p + '_adaptive'][0]
                s = ast.literal_eval(s)
                a = ast.literal_eval(a)
                s = [abs(i) for i in s]
                a = [abs(i) for i in a]
                plt.clf()
                plt.plot(s)
                plt.plot(a)
                plt.legend(['S', 'A'])
                plt.grid(linestyle='dotted')
                plt.savefig(os.path.join(args['dataset'], 'Context', 'plots', p, 
                        'W' + row['W'] + '_' + 'D' + row['D'] + '.jpg'))
            '''plotting the prob over rounds'''
            plt.clf()
            df_prob = row['prob']
            df_prob.columns = ['C_prob', 'ID_prob']
            plt.plot(df_prob)
            plt.legend(df_prob.columns)
            plt.grid(linestyle='dotted')
            plt.savefig(os.path.join(args['dataset'], 'Context', 'plots', 'probs', 
                        'W' + row['W'] + '_' + 'D' + row['D'] + '.jpg'))


def get_not_param(param):
    variables = ['W', 'D']
    return 'D' if param == 'W' else 'W' 


if __name__ == '__main__':
    exec(open('../../imports.py').read())
    args = parse_args()
    args = vars(args)
    print(args)
    df = create_df()
    df = get_all_dfs(args, df)
    plot_dfs(args, df)
