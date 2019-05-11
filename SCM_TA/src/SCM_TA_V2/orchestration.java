package SCM_TA_V2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import smile.sequence.HMM;
import SCM_TA_V1.*;

public class orchestration {
	
	static training training_instance=new training();
	public static void main(String[] args) throws NoSuchElementException, IOException, URISyntaxException{
	
		/*//generate environment
		environment environment_scenario1=new environment_s1();
		//create and intialize a sequence of observations
		observation[] observationSequence=new observation[4];//specify the number of observation sequence??!!!
		for(observation o:observationSequence ){
			environment_scenario1.generae_observation(o);
		}
		
		//get the trained Markov model
		HMM<observation> hmm=training_instance.getHMM();
		//find the sequence of predicted states 
		state[] predicted_statesSequence=training_instance.get_statesSequence(hmm, observationSequence);
		//apply the policy function and get the right action
		ArrayList<String> objectiveSet=operations.policyFunction(predicted_statesSequence[predicted_statesSequence.length-1]);
		//lunch the assignment process with the provided set of objectives--call the runExpriment of Test2
		Test2.run(objectiveSet, "JDT");
		Test2.run(objectiveSet, "Platform");
		data representation for the reigning round==>call the dataRepresentation to make the data ready
		analyzing and visualization	
		
		//represent the result==> HyperVolume, Contribution and Generational Distance
		Test2.changeRepresentation("JDT");
		Test2.changeRepresentation("Platform");
		
		apply the feedback to the developer profiles, the environmental parameters
		 --pick a solution -- update the dev profile by the assigned tasks-- 
		//pick one of the solution===> the once that has highest level of knowledge diffusion
		
		
		
		//update the dev profile based on the picked solution 
*/		
		
		
		//create the environment_s1
		environment_s1.insantiateObjects();
		
		//pull in the developer  profile
		Test2.devInitialization();
		
		//cut off the low experienced developers---add ready for attachment developers
		//starting with half of the developers
		environment_s1.rankDevs();
		GA_Problem_Parameter.setDevelopersIDForRandom();
		//Initialize the devNetwork
		environment_s1.initializeDevNetwork();
		environment_s1.initializeR(0.4);
		
		//running the experiment--->>> feedbacks afterwards apply on developers profile 
		Test2.run(null, "JDT");
		Test2.run(null, "Platform");
		
		//team change process---determine the team change rate
		
		
		
		
		
	}

}
