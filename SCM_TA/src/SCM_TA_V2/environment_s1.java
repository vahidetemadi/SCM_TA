package SCM_TA_V2;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.geometry.spherical.twod.Vertex;

import SCM_TA_V1.*;

public class environment_s1 extends environment {
	public static double deletionRate=0;
	public static double attachmentRate=0;
	public static ArrayList<Integer> deletedNodes=new ArrayList<Integer>();
	public static ArrayList<Integer> readyForAttachment=new ArrayList<Integer>();
	static Random random;
	{
		SLA=new NormalDistribution(0.8,0.1);
		TCR=new PoissonDistribution(5);
		devNetwork=new DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge>(DefaultEdge.class);
		random=new Random();
	}
	

	public static  DefaultDirectedWeightedGraph<Entry<Integer, Developer>, DefaultEdge> getDevNetwork(){
		return devNetwork;
	}
	//prepare the input data set for training
	public void generae_observation(observation o){
		
		//assign the SLA violation rate--- using the Normal Distribution
		//o.setSLA_violation(SLA.sample());	
		
		
		//input the mean of desired Poisson distribution and then assign teamChangeRate
		double t=TCR.sample();
		
		/*assign the team utilization_mean to the current observation --how to get the right parameters for assignment
		get the 20 % upper percentile devs as the selected ones-- compute the utilization rate based on the formula (# of changes
		/ total # of lines changed for a changes)-- find the avg and var of the utilization rate-- map those to high or low
		 category-- they're gonna be the observations
		*/ 
		
		//o.setTeamUtilizationRate_avg(operations.getAVG(developrs_UR));
		//assign the utilization_var to the current observation-- need to compute the variance for the assignment
		
		//NormalDistribution TUR_V=new NormalDistribution(0.8,0.05);
		//o.setTeamUtilizationRate_var(operations.getVAR(developrs_UR));
		
	}
	
	public static void initializeDevNetwork(){	
		//set devs node and assign weights to the developers
		int size=GA_Problem_Parameter.developers.entrySet().size();
		int sumOfWeights=0;
		for(Map.Entry<Integer, Developer> dev:GA_Problem_Parameter.developers.entrySet()){
			dev.getValue().weight=size;
			sumOfWeights+=size;
			size--;
			devNetwork.addVertex(dev);
		}
		//add the edges
		Random r=new Random();
		int numOfEdges=15;
		ArrayList<Map.Entry<Integer, Developer>> edgeTails=new ArrayList<Map.Entry<Integer,Developer>>();
		for(int i=0;i<numOfEdges;i++){
			setRandomEdge(devNetwork, edgeTails, sumOfWeights, r);
		}
		
		//set the weights to the edges
		setEdgesWeight();
		
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
	
	public static void setEdges_newNodes(){
		int numOfEdges=5;
		for(Integer i:deletedNodes){
			devNetwork.addEdge(getSelectedVertexByFitness(), getDevNetworkVertex(i));
			devNetwork.addEdge(getDevNetworkVertex(i), getSelectedVertexByFitness());
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

	//the nodes are added to the 
	public static void nodeDeletion(){
		
		//is done with the a rate of "r"
		double p=0;
		int numOfNodes=0;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			p=random.nextDouble();
			if(p<deletionRate && numOfNodes>0){
				deletedNodes.add(node.getKey());
			}
		}
		
		for(Integer i:deletedNodes){
			devNetwork.removeVertex(getVertex(i));
		}
		
	}
	
	public static void nodeAttachment(){
		//add the node with the ratio of "1-r"
		ArrayList<Integer> shouldBeDeleted=new ArrayList<Integer>();
		double p;
		for(Integer i:readyForAttachment){
			p=random.nextDouble();
			if(p<attachmentRate){
				shouldBeDeleted.add(i);
				devNetwork.addVertex(GA_Problem_Parameter.getDev(i));
			}
		}	
		//remove nodes from readyForAttachment after added to the devNetwork
		for(Integer i:shouldBeDeleted){
			readyForAttachment.remove(i);
		}
		
		//establish the links for the newly added nodes
		setEdges_newNodes();
		
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
		environment_s1.deletionRate=probability;
		environment_s1.attachmentRate=1-deletionRate;
		
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
	 
	public static Map.Entry<Integer, Developer> getSelectedVertexByFitness(){
		
		Map.Entry<Integer, Developer> selected=null;
		int totalWight=0;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			int weight=node.getValue().weight;
			double r=random.nextInt(totalWight+weight);
			if(r>=totalWight)
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
	
	public static void rankDevs(environment_s1 en_1_sample){
		ArrayList<Ranking<Developer, Double>> Devs=new ArrayList<Ranking<Developer,Double>>();
		for(Developer d:GA_Problem_Parameter.developers.values()){
			Devs.add(DevMetrics.computeMetric(d));
		}
		DevMetrics.sortByMetric(Devs);
		for(Ranking<Developer, Double> r:Devs){
			System.out.println(r.getEntity()+"---"+r.getMetric());
		}
		
		//cut off the low experienced developers---add ready for attachment developers
		GA_Problem_Parameter.pruneDevList(GA_Problem_Parameter.developers,Devs,50, en_1_sample);
	}
}
