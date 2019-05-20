package SCM_TA_V2;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;
import java.util.Dictionary;

import smile.sequence.HMM;


public class training {

	
	//set the initial states
	double[] initialStates=new double[]{0.8,0.2};
	
	//parameters definition
	Random random_generator=new Random();
	double[] states=new double[]{0.5,0.5};
	double[][] transitions=new double[2][2];
	double[][] emissions=new double[2][2];

	//generate a new HMM
	public HMM<observation> getHMM() {
		initialize_params();
		HMM<observation> HMM=new HMM<observation>(initialStates,transitions,emissions, environment_s1.getObservationSymbols());
		
		return HMM;
	}

	//initialize the params 
	public void initialize_params(){
		
		//initialize transitions probabilities
		
		/*//assign random entries
		for(int i=0;i<transitions.length;i++){
			for(int j=0;j<transitions[i].length;j++)
				transitions[i][j]=random_generator.nextDouble();
		}*/
		
		//initialized by hand
		transitions[0][0]=0.6;
		transitions[0][1]=0.4;
		transitions[1][0]=0.5;
		transitions[1][1]=0.5;
		
		//initialize emissions probabilities
		/*//assign random entries
		for(int i=0;i<emissions.length;i++){
			for(int j=0;j<emissions[i].length;j++)
				emissions[i][j]=random_generator.nextDouble();
		}*/
		
		//initialized by hand
		emissions[0][0]=0.9;
		emissions[0][1]=0.1;
		emissions[1][0]=0.1;
		emissions[1][1]=0.9;
	}
	
	/*//get the sequence of states
	public state[] get_statesSequence(HMM<observation> hmm, observation[] o){
		int[] state_ids=hmm.predict(o);
		state[] predictedStates=new state[state_ids.length];
		for(int i=0;i<state_ids.length;i++)
			switch (state_ids[i]) {
			case 1:
				predictedStates[i]=steady_state;
			case 2: 
				predictedStates[i]=dynamic_state;
				break;
			}
		
		return predictedStates;
	}*/

}
