package main.java.mainPipeline;

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
	@SuppressWarnings("static-access")
	public InitializedFeaturesProblem(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
		// TODO Auto-generated constructor stub
		adaptive=AdaptiveAssignmentPipline.getInstance();
		featureSetV1=FeatureSetV1.getInstance();
	}

	// MODIFY get a solution and compute its objective and  
	// EFFECT the cost associated to a solution is computed
	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		try {
			adaptive.run(solution, totals);
			solution.setObjective(0, totals.get("TCT_static"));
			solution.setAttribute("TCT_adaptive", totals.get("TCT_adaptive"));
			solution.setAttribute("TID_adaptive", totals.get("TID_adaptive"));
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
