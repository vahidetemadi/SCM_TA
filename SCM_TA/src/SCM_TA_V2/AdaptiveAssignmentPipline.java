/* Copyright 2019- Vahid Etemadi
 * 
 */
package SCM_TA_V2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import org.jgrapht.alg.color.SaturationDegreeColoring;

import smile.sequence.HMM;
import SCM_TA_V1.*;

public class AdaptiveAssignmentPipline {

	static Training training_instance=new Training();
	static ArrayList<String> objectiveSet=new ArrayList<String>();
	static Random random=new Random();
	static HMM<Observation> HMM=null;
	static String datasetName=null;
	
	/*
	 * Gets invoked to run a new pipeline of task assignment in an adaptive way
	 *
	 * <p> 
	 * the method orchestrates the sequence of tasks prior to self-adaptive assignment
	 * the objective is to compute the overall cost for a particular optimization
	 * this method only initialize the inputs
	 * 
	 * EFFECT the overall cost is computed and is returned as the fitness of input solution 
	 */		
	public double run(Solution solution){
		//set the num of devs
		
		return null;
	}
	public static void main(String[] args) throws NoSuchElementException, IOException, URISyntaxException{
		//get the trained Markov model
		HMM=training_instance.getHMM();
		
		Environment_s1.generaetListOfState();
		Environment_s1.generaetListOfObservation(); 
		
		Environment_s1.insantiateObjects(); 
		
		System.out.println("Enter the dataset name:");
		Scanner sc=new Scanner(System.in);
		datasetName=sc.next();
		
		//pull in the developer  profile
		Test2.devInitialization(datasetName);
		
		//cut off the low experienced developers---add ready for attachment developers
		//starting with half of the developers
		Environment_s1.rankDevs();
		
		//Initialize the devNetwork
		Environment_s1.initializeDevNetwork();
		
		//supposed to change initialize the deletion and attachment rate
		Environment_s1.initializeR(0.3);
		Environment_s1.initializeParameters();
		
		for(Entry<Integer, Developer> i:Environment_s1.getDevNetwork().vertexSet()){
			System.out.print(i.getKey()+" , ");
		}
		
		System.out.println();
		
		for(Integer i:Environment_s1.readyForAttachment){
			System.out.print(i+" , ");
		}
		
		//set the number of files
		if(datasetName=="JDT")
			Environment_s1.numberOfFiles=9;
		else
			Environment_s1.numberOfFiles=10;
		
		//set the initial observation and 
		int roundNum=1;
		for(int i=1; i<=Environment_s1.numberOfFiles;i++){
			//call for run
			GA_Problem_Parameter.listOfSubBugs.clear();
			Test2.run(datasetName, i);
			int j=0;
			for(HashMap<Integer,Bug> bugList:GA_Problem_Parameter.listOfSubBugs){
				//set bug dependencies
				Test2.setBugDependencies(datasetName, bugList);
				
				//call the GA initialization--after party call
				Test2.initializeGAParameter(bugList);
				
				//generate the models for create the candidates
				GA_Problem_Parameter.generateModelofBugs();
				GA_Problem_Parameter.candidateSolutonGeneration();
				
				//find most probable state
				State state=getState(HMM);
				Environment_s1.addToSequenceOfStates(state);

				Test2.Assigning(state.actionSet.get(0), 1, roundNum, datasetName);
				
				//make the update onto devNetwork
				Environment_s1.nodeDeletion();
				Environment_s1.nodeAttachment();

				GA_Problem_Parameter.setDevelopersIDForRandom();
				System.out.println("number of developers---devNetwork: "+Environment_s1.devNetwork.vertexSet().size()
						+"*** total changed: "
						+Environment_s1.totalChanged);
				//add to the sequence of observation
				//the updates behind poisson process
				//update lambda
				Environment_s1.reinitializeParameters(random.nextInt(Environment_s1.getDevNetwork().vertexSet().size()),
						random.nextInt((Environment_s1.getDevNetwork().vertexSet().size()/2)));
				
				Environment_s1.addToSequenceOfObservation(Environment_s1.getObservation());

				j++;
				roundNum++;
			}
		}	
	}
	
	public static State getState(HMM<Observation> HMM){
		HashMap<State, Double> stateProbability=new HashMap<State, Double>();
		
		int[] observation=Environment_s1.getObsercationSequence();
		int[] states=null;
		 
		int i=Environment_s1.observationSequence.size();
		for(Map.Entry<Integer, State> s:Environment_s1.listOfState.entrySet()){
			if(states!=null || i>1)
				Environment_s1.addToSequenceOfStates(s.getValue());
			states=Environment_s1.getStateSequence();
			stateProbability.put(s.getValue(), HMM.p(observation,states));
			Environment_s1.stateSequence.remove(Environment_s1.stateSequence.size()-1);
			i++;
		}
		
		double totalProb=0;
		for(Map.Entry<State, Double> stateProb:stateProbability.entrySet()){
			totalProb+=stateProb.getValue();
		}
		for(Map.Entry<State, Double> stateProb:stateProbability.entrySet()){
			stateProbability.put(stateProb.getKey(), stateProb.getValue()/totalProb);
		}
		
		double r=random.nextDouble();
		Map.Entry<State, Double> selectedState=null;
		double lowerBound=0;
		for(Map.Entry<State, Double> stateProb:stateProbability.entrySet()){
			if ((lowerBound < r) && (r < (lowerBound+stateProb.getValue())))
				selectedState=stateProb;
			else
				lowerBound+=stateProb.getValue();
		}
		
		
		/*for(Map.Entry<state, Double> stateProb:stateProbability.entrySet()){
			if (selectedState==null)
				selectedState=stateProb;
			else
				if(stateProb.getValue()>selectedState.getValue())
					selectedState=stateProb;
		}*/
		
		//environment_s1.addToSequenceOfStates(selectedState.getKey());
		return selectedState.getKey();
	}


}
