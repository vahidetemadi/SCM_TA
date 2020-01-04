package featureTuning;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import java.util.HashMap;
import java.util.Map;


public class FeatureSetV1 implements FeatureGeneration {
	
	static int numberOfVariable;
	static final HashMap<Integer, String> featureVectorIndex=new HashMap<Integer, String>(){
		{
			put(0, "numOfDevs");
			put(1, "numOfDevs");
			put(2, "TCR");
			put(3, "EM");
			put(4, "TM");
		}	
		};
		
		
	@Override
	/*
	 * Takes a solution and initialize that with the variable
	 * @ 
	 * 
	 * @see featureTuning.FeatureGeneration#getTheFeatureVector(org.moeaframework.core.Solution)
	 */
	
	public Solution getTheFeatureVector(Solution solution) {
		for(Map.Entry<Integer, String> varaible:featureVectorIndex.entrySet())
			switch(varaible.getValue()){
				case "numOfDevs":
					int numOfDevs=this.getNumOfDevs();
					solution.setVariable(varaible.getKey(), EncodingUtils.newInt(numOfDevs, numOfDevs));
					break;
				case "numOfBugs":
					int numOfBugs=this.getNumOfBugs();
					solution.setVariable(varaible.getKey(), EncodingUtils.newInt(numOfBugs, numOfBugs));
					break;
				case "TCR":
					double TCR=this.getTCR();
					solution.setVariable(varaible.getKey(), EncodingUtils.newReal(TCR, TCR));
					break;
				case "TCR":
					double TCR=this.getTCR();
					solution.setVariable(varaible.getKey(), EncodingUtils.newReal(TCR, TCR));
					break;
				case "EM":
					double EM=this.getTCR();
					solution.setVariable(varaible.getKey(), EncodingUtils.newReal(TCR, TCR));
					break;
			}
			
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
