package SCM_TA_V2;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;
import java.util.Dictionary;

import smile.sequence.HMM;


public class training {

	//introduce the states
	static final state steady_state=new state("Steady","1");
	static final state dynamic_state=new state("Dynamic","2");
	
	//parameters definition
	Random random_generator=new Random();
	double[] states=new double[]{0.5,0.5};
	double[][] transitions=new double[2][2];
	double[][] emissions=new double[2][16];

	public HMM<observation> getHMM() {
		initialize_params();
		HMM<observation> hmm=new HMM<observation>(states,transitions,emissions);
		return hmm;
	}
	
	
	//initialize the params 
	public void initialize_params(){
		
		//initialize transitions probabilities
		for(int i=0;i<transitions.length;i++){
			for(int j=0;j<transitions[i].length;j++)
				transitions[i][j]=random_generator.nextDouble();
		}
		
		//initialize emissions probabilities
		for(int i=0;i<emissions.length;i++){
			for(int j=0;j<emissions[i].length;j++)
				emissions[i][j]=random_generator.nextDouble();
		}
	}
	
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
	}

}
