package featureTuning;

import org.moeaframework.core.Solution;

import java.util.HashMap;
import java.util.Map;


public class FeatureSetV1 implements FeatureGeneration {
	
	static int numberOfVariable;
	static HashMap<Integer, String> featureVectorIndex=new HashMap<Integer, String>();
	{
		featureVectorIndex.put(1, "numOfDevs");
		
	}
	@Override
	/*
	 * Takes a solution and initialize that with the variable
	 * @ 
	 * 
	 * @see featureTuning.FeatureGeneration#getTheFeatureVector(org.moeaframework.core.Solution)
	 */
	public Solution getTheFeatureVector(Solution solution) {
		for(Map.Entry<Integer, String> varaible:featureVectorIndex)
			solution.setVariable(index, variable);
			
		return solution;
	}


	@Override
	public Double getTCR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getNumOfDevs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getNumOfBugs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[][] getmissionMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[][] getTransitionMatrix() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
