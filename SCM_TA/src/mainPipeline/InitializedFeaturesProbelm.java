package mainPipeline;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.*;
import javax.json.*;

import featureTuning.FeatureSetV1;


/**
 * The InitializationFeatureProble implements the problem of examining of all possible clss
 * @author DistLab3
 *
 */
public class InitializedFeaturesProbelm extends AbstractProblem {

	AdaptiveAssignmentPipline adaptive=null;
	FeatureSetV1 featureSetV1=null;
	@SuppressWarnings("static-access")
	public InitializedFeaturesProbelm(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
		// TODO Auto-generated constructor stub
		adaptive=AdaptiveAssignmentPipline.getInstance();
		featureSetV1=FeatureSetV1.getInstance();
	}

	// MODIFY  
	// EFFECT the cost associated to a solution is computed
	@SuppressWarnings("static-access")
	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		try {
			Double[] doubles=adaptive.run(solution);
			solution.setObjective(0, doubles[0]);
			solution.setAttribute("ID", doubles[1]);
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
