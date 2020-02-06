package main.java.mainPipeline;

import java.util.ArrayList;
import java.util.HashMap;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import main.java.featureTuning.FeatureSetV1;


/**
 * The InitializationFeatureProble implements the problem of examining of all possible clss
 * @author DistLab3
 *
 */
public class InitializedFeaturesProblem extends AbstractProblem {

	AdaptiveAssignmentPipline adaptive=null;
	FeatureSetV1 featureSetV1=null;
	HashMap<String, Double> totals=new HashMap<String, Double>();
	HashMap<String, ArrayList<Double>> tredOverTim=new HashMap<String, ArrayList<Double>>(); 
	@SuppressWarnings("static-access")
	public InitializedFeaturesProblem(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
		// TODO Auto-generated constructor stub
		adaptive=AdaptiveAssignmentPipline.getInstance();
		featureSetV1=FeatureSetV1.getInstance();
		
		//add <key, value>s to the trendOverTime list
		/*
		 * tredOverTim.put("CoT_static", new ArrayList<Double>());
		 * tredOverTim.put("IDoT_static", new ArrayList<Double>());
		 * tredOverTim.put("CoT_adaptive", new ArrayList<Double>());
		 * tredOverTim.put("IDoT_adaptive", new ArrayList<Double>());
		 */
	}

	/**
	 * Methos just clear the Arraylist of trends
	 */
	private void makeListClear() {
		tredOverTim.get("CoT_static").clear();
		tredOverTim.get("IDoT_static").clear();
		tredOverTim.get("CoT_adaptive").clear();
		tredOverTim.get("IDoT_adaptive").clear();
	}
	
	// MODIFY get a solution and compute its objective and  
	// EFFECT the cost associated to a solution is computed
	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		try {
			//makeListClear();
			adaptive.run(solution, totals, tredOverTim);
			solution.setObjective(0, totals.get("TCT_static"));
			solution.setAttribute("TCT_adaptive", totals.get("TCT_adaptive"));
			solution.setAttribute("TID_adaptive", totals.get("TID_adaptive"));
			solution.setAttribute("CoT_static", tredOverTim.get("CoT_static"));
			solution.setAttribute("CoT_adaptive", tredOverTim.get("CoT_adaptive"));
			solution.setAttribute("IDoT_adaptive", tredOverTim.get("IDoT_adaptive"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	//JsonObject jObject=Json.createObjectBuilder().build();
	
	@Override
	public Solution newSolution() {
		// TODO Auto-generated method stub
		Solution solution=new  Solution(numberOfVariables, numberOfObjectives);
		featureSetV1.setFeatureVector(solution);
		return solution;
	}

}
