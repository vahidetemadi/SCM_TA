# SCM_TA: How to run the implemented code
The only thing you should follow to run the experiment mainly referred to in in-progress paper is to clone the repository into a machine with the following requirements:
1. Provide a JVM through installing jre or jdk (preferably Vesrion 1.8.0 or above) on your underlying machine.
You could try to run the code from the terminal (CMD in windows) or using an IDE.
The program takes two arguments, windows size, devloper churn rate which must be provided when it's called (please refer to 
 to https://www.cs.colostate.edu/helpdocs/cmd.pdf to see how the arguments could be introduced)
1.a. Compile the provided code in src package
1.b. Run the GATaskAssignment.class which contains the main method together with introducing the input arguments

2. The final results are provided in separate folders including all type of outpus. These raw results are then go through several steps 
to be ready for publishing as the results. Several python scripts are used to analyze the results
