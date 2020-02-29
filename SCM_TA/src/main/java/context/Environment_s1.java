package main.java.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import main.java.SCM_TA_V1.DevMetrics;
import main.java.SCM_TA_V1.Developer;
import main.java.SCM_TA_V1.GA_Problem_Parameter;
import main.java.SCM_TA_V1.Ranking;
import main.java.SCM_TA_V1.Zone;
//import org.apache.commons.math3.distribution.*;
import smile.stat.distribution.PoissonDistribution;

public class Environment_s1 extends Environment {
	public static double deletionRate=0;
	public static double attachmentRate=0;
	public static double TCR_ratio=0;
	public static int totalChanged=0;
	public static ArrayList<Integer> deletedNodes=new ArrayList<Integer>();
	public static ArrayList<Integer> readyForAttachment=new ArrayList<Integer>();
	static Random random;
	static int numOfNodes;
	public static int numberOfFiles=0;
	public static ArrayList<State> stateSequence=new ArrayList<State>();
	public static ArrayList<Observation> observationSequence=new ArrayList<Observation>();
	static HashMap<Integer, Observation> listOfObservation=new HashMap<Integer, Observation>();
	public static HashMap<Integer, State> listOfState=new HashMap<Integer, State>();
	static ArrayList<Integer> shouldBeDeleted=new ArrayList<Integer>();
	static int busFactor=2;
	static ArrayList<Integer> addedRecently=new ArrayList<Integer>();
	
	public static void insantiateObjects(int lambda){
		devNetwork=new DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge>(DefaultEdge.class);
		random=new Random();
		TCR=new PoissonDistribution(lambda);
	}
	

	public static  DefaultDirectedWeightedGraph<Entry<Integer, Developer>, DefaultEdge> getDevNetwork(){
		return devNetwork;
	}
	//prepare the input data set for training
	public static void generaetListOfObservation(){
		//clear former sequence
		listOfObservation.clear();
		
		//create new observation
		Observation o1=new Observation(0);
		Observation o2=new Observation(2);
		listOfObservation.put(0, o1);
		listOfObservation.put(1,o2);
	}
	
	public int getNumOfBugs(){
		return GA_Problem_Parameter.tasks.size();
	}
	
	
	public static void generaetListOfState(){
		//clear list of states
		listOfState.clear();
		
		//introduce the states
		final State steady_state=new State("steady",0);
		steady_state.setAction("diffusion");
		final State dynamic_state=new State("dynamic",1);
		dynamic_state.setAction("cost");
		
		listOfState.put(0, steady_state);
		listOfState.put(1,dynamic_state);
	}
	
	public static void initializeDevNetwork(){	
		//set devs node and assign weights to the developers
		int size=GA_Problem_Parameter.devListId.size();
		int sumOfWeights=0;
		for(Map.Entry<Integer, Developer> entry:GA_Problem_Parameter.developers_all.entrySet()){
			if(GA_Problem_Parameter.devListId.contains(entry.getKey())) {
				entry.getValue().weight=size;
				sumOfWeights+=size;
				size--;
				devNetwork.addVertex(entry);
			}
		}
		/*
		 * //add the edges Random r=new Random(); int numOfEdges=(size*(size-1))/2;
		 * ArrayList<Map.Entry<Integer, Developer>> edgeTails=new
		 * ArrayList<Map.Entry<Integer,Developer>>(); for(int i=0;i<numOfEdges;i++){
		 * setRandomEdge(devNetwork, edgeTails, sumOfWeights, r); }
		 */

		//set fully connected graph
		makeNetworkFullyConnectd(devNetwork);
		
		//set the weights of the edges
		setEdgesWeight();
		
	}
	
	
	
	public static void updateDevNetwork() {
		makeNetworkFullyConnectd(devNetwork);
		setEdgesWeight();
	}
	
	/**
	 * The method crates edges among the nodes in the network
	 * @param devNetwork
	 * @param edgeTails
	 * @param sumOfWeights
	 * @param r
	 */
	
	public static void makeNetworkFullyConnectd(DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge> devNetwork) {
		for(Map.Entry<Integer, Developer> nodeS:devNetwork.vertexSet()) {
			for(Map.Entry<Integer, Developer> nodeE:devNetwork.vertexSet()) {
				if(nodeS.getKey()!=nodeE.getKey()) {
					if(!devNetwork.containsEdge(nodeS, nodeE))
						devNetwork.addEdge(nodeS, nodeE);
				}	
			}
		}
			
	}
	
	public static void setRandomEdge(DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge> devNetwork, 
			 ArrayList<Map.Entry<Integer, Developer>> edgeTails, int sumOfWeights, Random r){
		int randNum;
		for(Map.Entry<Integer, Developer> dev:GA_Problem_Parameter.developers.entrySet()){
			randNum=r.nextInt(sumOfWeights);
			randNum-=dev.getValue().weight;
			if(randNum<0){
				edgeTails.add(dev);
			}
			if(edgeTails.size()==2){
				devNetwork.addEdge(edgeTails.get(0), edgeTails.get(1));
				devNetwork.addEdge(edgeTails.get(1), edgeTails.get(0));
				edgeTails.clear();
				continue;
			}
				
		}
	}
	
	public static void setEdges_newNodes(ArrayList<Integer> shouldBeDeleted){
		int numOfEdges=5;
		Map.Entry<Integer, Developer> dev=null;
		for(Integer i:shouldBeDeleted){
			//the selected node should not be as those in the 
			dev=getSelectedVertexByFitness(i);
			devNetwork.addEdge(dev, getDevNetworkVertex(i));
			devNetwork.addEdge(getDevNetworkVertex(i), dev);
		}
		
	}
	//set the label for all the edges in the devNetwork
	public static void setEdgesWeight(){
		//assign flow rate to each edge in developer network
		for(DefaultEdge e:devNetwork.edgeSet()){
			//compute the flow rate--start to end
			double flowRate=0;
			for(Map.Entry<Zone, Double> z:devNetwork.getEdgeSource(e).getValue().DZone_Coefficient.entrySet()){
				HashMap<Zone, Double> z_target=devNetwork.getEdgeTarget(e).getValue().DZone_Coefficient;
				if(!z_target.containsKey(z.getKey()))
					flowRate+=z.getValue();
				else{
					double difference=z.getValue()-z_target.get(z.getKey());
					flowRate+=(difference>0)?difference:0;
				}
					
			}
			//assign the flow rate
			devNetwork.setEdgeWeight(e,flowRate);
		}
		
	}

	//EFFECT: the developer network gets updated with nodes removed from that
	public static void nodeDeletion(){	
		//is done with the a rate of "r"
		double p=0;
		totalChanged=0;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			p=random.nextDouble();
			
			//ignore those who added recently
			if(addedRecently.contains(node.getKey()))
				continue;
			if(p<TCR_ratio && devNetwork.vertexSet().size() > GA_Problem_Parameter.devListIdSize){
				devNetwork.removeVertex(getVertex(node.getKey()));
				GA_Problem_Parameter.developers.remove(node.getKey());
				GA_Problem_Parameter.devListId.remove(node.getKey());
				totalChanged++;
			}
		}
	}
	
	
	/**
	 * Attaches the nodes from a developer pool
	 * 
	 * EFFECT: a new developer network with updated nodes
 	 */
	public static void nodeAttachment(){
		shouldBeDeleted.clear(); //it's needed to then update ready for attachment list
		addedRecently.clear();
		double p;
		for(Integer i:readyForAttachment){
			p=random.nextDouble();
			if(p<TCR_ratio && numOfNodes>0){
				shouldBeDeleted.add(i);
				
				//check weather developer i exists
				if(GA_Problem_Parameter.getDev(i)!=null){
					Map.Entry<Integer, Developer> developer=GA_Problem_Parameter.getDev(i);
					devNetwork.addVertex(developer);
					//GA_Problem_Parameter.developers.put(i, GA_Problem_Parameter.developers_all.get(i));
					GA_Problem_Parameter.devListId.add(i);
					addedRecently.add(i);
					shouldBeDeleted.add(i);
					totalChanged++;
				}
			}
		}
		//remove nodes from readyForAttachment after added to the devNetwork
		for(Integer i:shouldBeDeleted){
			readyForAttachment.remove(i);
		}
		
		//establish the links for the newly added nodes
		setEdges_newNodes(shouldBeDeleted);
		
	}
	
	public static Map.Entry<Integer, Developer> getVertex(Integer i){
		Map.Entry<Integer, Developer> nodeForDeletion=null;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet())
			if(node.getKey()==i)
				nodeForDeletion=node;
			else
				nodeForDeletion=null;
		return nodeForDeletion;
	}
	
	public static void initializeR(double probability){
		Environment_s1.deletionRate=probability;
		Environment_s1.attachmentRate=1-deletionRate;
		
	}
	
	/*** after round update method ***/
	public static void recomputeNodeFitness(){
		double globalFitness=0;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			double individualFitness=0;
			for(Map.Entry<Zone, Double> zone:node.getValue().DZone_Coefficient.entrySet()){
			individualFitness+=zone.getValue();
			}
			individualFitness=individualFitness*devNetwork.degreeOf(node);
			node.getValue().fitness=individualFitness;
			globalFitness+=individualFitness;
		}
		
		for(Map.Entry<Integer, Developer> dev:devNetwork.vertexSet())
			dev.getValue().preferentialAttachment=dev.getValue().fitness/globalFitness;
		
	}
	 
	public static Map.Entry<Integer, Developer> getSelectedVertexByFitness(Integer selfEdge){
		
		Map.Entry<Integer, Developer> selected=null;
		int totalWight=0;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			int weight=node.getValue().weight;
			double r=random.nextInt(totalWight+weight);
			if(r>=totalWight && node.getKey()!=selfEdge)
				selected=node;
			totalWight+=weight;
		}
		return selected;
	}
	
	public static Map.Entry<Integer, Developer> getDevNetworkVertex(Integer i){
		Map.Entry<Integer, Developer> node=null;
		for(Map.Entry<Integer, Developer> vertex:devNetwork.vertexSet())
			if(vertex.getKey()==i)
				node=vertex;
		return node;
	}
	
	public static void rankDevs(){
		ArrayList<Ranking<Developer, Double>> Devs=new ArrayList<Ranking<Developer,Double>>();
		
		System.out.println("prelimenary dev list size: "+GA_Problem_Parameter.developers.size());
		
		for(Developer d:GA_Problem_Parameter.developers.values()){
			Devs.add(DevMetrics.computeMetric(d));
		}
		
		DevMetrics.sortByMetric(Devs);
		
		for(Ranking<Developer, Double> r:Devs){
			System.out.println(r.getEntity()+"---"+r.getMetric());
		}
		
		System.out.println("secondary dev list size: "+Devs.size());
		//cut off the low experienced developers---add ready for attachment developers
		
		GA_Problem_Parameter.pruneDevList(GA_Problem_Parameter.developers, Devs,75);
	}

	public static double getTCR_ratio(){
		/*double down=devNetwork.vertexSet().size();
		TCR_ratio=totalChanged/down;*/
		return TCR_ratio;
	}
	
	public static Observation getObservation(){
		if(numOfNodes>=busFactor)
			return listOfObservation.get(1);
		else 
			return listOfObservation.get(0);
	}
	
	public static void addToSequenceOfStates(State state){
		stateSequence.add(state);
	}
	
	public static void addToSequenceOfObservation(Observation observation){
		observationSequence.add(observation);
	}
	
	public static State getTheLastState(){
		return stateSequence.get(stateSequence.size()-1);
	}

	public static Observation[] getObservationSymbols(){
		Observation o1=new Observation(2);
		Observation o2=new Observation(0);
		
		return new Observation[]{o1,o2};
	}

	@SuppressWarnings("null")
	public static int[] getStateSequence(){
		int[] stateSeqId=new int[stateSequence.size()];
		for(int i=0;i<stateSequence.size();i++){
			stateSeqId[i]=stateSequence.get(i).id;
		}
		return stateSeqId;
	}
	
	public static int[] getObsercationSequence(){
		int[] obsercationSeqId=new int[observationSequence.size()];
		for(int i=0;i<observationSequence.size();i++){
			obsercationSeqId[i]=observationSequence.get(i).getTeamChangeRate();
		}
		return obsercationSeqId;
	}
	
	public static void initializeParameters(){
		//clear sequences
		stateSequence.clear();
		observationSequence.clear();
		
		Environment_s1.addToSequenceOfStates(listOfState.get(0));
		Environment_s1.addToSequenceOfObservation(Environment_s1.getObservation());	
	}

	public static void reinitializeParameters(){
		TCR_ratio=ThreadLocalRandom.current().nextDouble(0.0,0.2);
		numOfNodes=getNearestK(TCR_ratio);
	}
	
	/**
	 * The method intends to find the nearest number of developers for input churn ration
	 * @param tcr of type double and denotes output of poisson process
	 * @return k  of type integer as the number of poisson algorithm input
	 */
	public static int getNearestK(double tcr) {
		int k=0;
		double diff=0.0;
		for(int i=0; i<getDevNetwork().vertexSet().size();i++) {
			if(Math.abs((1-TCR.cdf(k)))>diff)
				k=i;
				diff=(Math.abs((1-TCR.cdf(k))));
		}
		return k;
	}
}
