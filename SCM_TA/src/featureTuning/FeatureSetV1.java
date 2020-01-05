package featureTuning;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import java.util.HashMap;
import java.util.Map;


public class FeatureSetV1 implements FeatureGeneration {
	
	FeatureInitializationV1 featureInitialization;
	static final HashMap<Integer, String> featureVectorIndex=new HashMap<Integer, String>(){
		{
			put(0, "numOfDevs");
			put(1, "numOfDevs");
			put(2, "TCR");
			put(3, "EM");
			put(4, "TM");
		}	
		};
		
	public FeatureSetV1() {
		// TODO Auto-generated constructor stub
		featureInitialization=FeatureInitializationV1.getInstance();
	}	
	@Override
	/*
	 * Takes a solution and initialize that with the all the required variable
	 * @param solution a candidate solution needs to be initialized with the verified variables
	 * 
	 * @return solution the initialized variable
	 * @see featureTuning.FeatureGeneration#getTheFeatureVector(org.moeaframework.core.Solution)
	 */
	//MODIFY
	//EFFECT 
	
	public Solution getTheFeatureVector(Solution solution) {
		
		setNumOfDevs(solution);
		setNumOfBugs(solution);
		setTCR(solution);
		setEM(solution);
		setTM(solution);
		
		return solution;
	}
	@Override
	public void setTCR(Solution solution) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setNumOfDevs(Solution solution) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setNumOfBugs(Solution solution) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setEM(Solution solution) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setTM(Solution solution) {
		// TODO Auto-generated method stub
		
	}


	

	
}
