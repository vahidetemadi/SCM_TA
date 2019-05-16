package SCM_TA_V2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import smile.sequence.HMM;
import SCM_TA_V1.*;

public class adaptiveAssignmentPipline {
	
	static training training_instance=new training();
	public static void main(String[] args) throws NoSuchElementException, IOException, URISyntaxException{
	
		//generate environment
		//environment environment_scenario1=new environment_s1();
		
		//get the trained Markov model
		HMM<observation> hmm=training_instance.	getHMM();
		
		//apply the policy function and get the right action
		//ArrayList<String> objectiveSet=operations.policyFunction(predicted_statesSequence[predicted_statesSequence.length-1]);
		//data representation for the reigning round==>call the dataRepresentation to make the data ready
		//analyzing and visualization	
		
		
		//required in case of 
		/*//represent the result==> HyperVolume, Contribution and Generational Distance
		Test2.changeRepresentation("JDT");
		Test2.changeRepresentation("Platform");*/ 

		
		
		environment_s1.insantiateObjects();
		
		//pull in the developer  profile
		Test2.devInitialization();
		
		//cut off the low experienced developers---add ready for attachment developers
		//starting with half of the developers
		environment_s1.rankDevs();
		
		//Initialize the devNetwork
		environment_s1.initializeDevNetwork();
		environment_s1.initializeR(0.4);

		for(Entry<Integer, Developer> i:environment_s1.getDevNetwork().vertexSet()){
			System.out.print(i.getKey()+" , ");
		}
		
		System.out.println();
		
		for(Integer i:environment_s1.readyForAttachment){
			System.out.print(i+" , ");
		}
		
		for(int i=0; i<environment_s1.numberOfFiles;i++){
			//get the observation
			environment_s1.addToSequenceOfObservation();
			
			//running the experiment--->>> feedbacks afterwards apply on developers profile 
			Test2.run(null, "JDT", i);
			Test2.run(null, "Platform", i);
			//team change process---determine the team change rate
			if(environment_s1.getTheLastState().name=="steady"){
				environment_s1.nodeDeletion();
				environment_s1.nodeAttachment();
			}

			GA_Problem_Parameter.setDevelopersIDForRandom();
			System.out.println("number of developers---devNetwork:"+environment_s1.getDevNetwork().vertexSet().size());
		}

	}

}
