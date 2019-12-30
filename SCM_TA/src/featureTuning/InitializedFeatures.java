package featureTuning;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.*;
import javax.json.*;

public class InitializedFeatures extends AbstractProblem {

	public InitializedFeatures(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
		// TODO Auto-generated constructor stub
	}

	// MODIFY 
	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		
	}

	JsonObject jObject=Json.createObjectBuilder().build();
	
	@Override
	public Solution newSolution() {
		// TODO Auto-generated method stub
		return null;
	}

}
