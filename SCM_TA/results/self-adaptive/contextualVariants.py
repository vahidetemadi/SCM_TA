import os
import sys
import argparse
import pandas as pd
import ast

#the command to run--> 

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
    parser.add_argument("-param", "--parameter", help="the parameter need to be tuned-- \
    should be a value of 'P', 'C' or 'M'")

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(1)
    return parser.parse_args()

def create_df():
    df = pd.DataFrame(columns = ['W', 'D', 'B', 'P', 'C', 'M', 'A', 'B', 'result'])
    return df

def get_all_dfs(args, df):
    i = 0
    for filename in os.listdir(os.path.join(args['dataset'] , "PI")):
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

            temp_df = pd.read_csv(os.path.join(args['dataset'] , "PI", filename))
            df._set_value(str(i), 'result', temp_df)
            i = i + 1

def create_table(df, args):
    print(df[args['parameter'] + '_x'])
    l = df[args['parameter'] + '_x']
    l = set(l)
    param = args['parameter']
    df_2 = pd.DataFrame(index=l, columns=l)
    for x in l:
        for y in l:
            if x != y:
                temp_df = df.loc[(df[param + '_x'] == x) & (df[param + '_y'] == y)]
                l1 = temp_df['result_x']
                l1 = l1.iloc[0]['CoT_adaptive']
                l1 = l1[0]
                l1 = float(l1[1 : len(l1)-1])
                l2 = temp_df['result_y']
                l2 = l2.iloc[0]['CoT_adaptive']
                l2 = l2[0]
                l2 = float(l2[1 : len(l2)-1])
                df_2._set_value(x, y, round((l1 - l2) / max(l1, l2), 2))
    df_2 = df_2.fillna('-')
    print(df_2)
    with open(os.path.join(os.getcwd(), args['dataset'], 'PI','Table_' + args['parameter'] + '.tex'), 'w+') as file:
        file.write(df_2.to_latex())
                                    
if __name__ == '__main__':
    exec(open('../../imports.py').read())
    cols = ['P', 'C', 'M']
    args = parse_args()
    args = vars(args)
    cols = [item for item in cols if not args['parameter']]
    df = create_df()
    get_all_dfs(args, df)
    print(len(df.index))
    #new_df = df.join(df, lsuffix='_x', rsuffix='_y', on=cols)
    df['key'] = 0
    new_df = pd.merge(df, df, on='key')
    print(new_df)
    if args['parameter'] == 'M':
        new_df = new_df[(new_df.P_x == args['population']) & (new_df.P_y == args['population']) 
                    & (new_df.C_x == args['crossover']) & (new_df.C_y == args['crossover'])]
    elif args['parameter'] == 'C':
        new_df = new_df[(new_df.P_x == args['population']) & (new_df.P_y == args['population']) 
                    & (new_df.M_x == args['mutation']) & (new_df.M_y == args['mutation'])]
    elif args['parameter'] == 'P':
         new_df = new_df[(new_df.C_x == args['crossover']) & (new_df.C_y == args['crossover']) 
                    & (new_df.M_x == args['mutation']) & (new_df.M_y == args['mutation'])]
    new_df = new_df[['P_x', 'C_x', 'M_x', 'result_x', 'P_y', 'C_y', 'M_y', 'result_y']]
    print(new_df)
    create_table(new_df, args)
    
    #write_tables(df)
