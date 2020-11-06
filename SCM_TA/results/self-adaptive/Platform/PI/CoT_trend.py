import sys
import argparse

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

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(1)
    return parser.parse_args()



if __name__ == '__main__':
    exec(open('../../../../imports.py').read())
    args = parse_args()
    args = vars(args)
    print(args)
    temp_df = pd.read_csv('W' + args['window'] + '_' + 'D' + args['developer'] + '_'
    + 'B' + args['batch'] + '_' + 'P' + args['population'] + '_' + 'Cr' + args['crossover'] + '_' + 'M' +  args['mutation'] + '_'
     + 'A' + args['alpha'] + '_' + 'B' + args['beta'] + '_1' + ".csv")
    s = temp_df['CoT_static'][0]
    a = temp_df['CoT_adaptive'][0]
    s = ast.literal_eval(s)
    a = ast.literal_eval(a)

    plt.plot(s)
    plt.plot(a)
    plt.legend(['S', 'A'])
    plt.show()

