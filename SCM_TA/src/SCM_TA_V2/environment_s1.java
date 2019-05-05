package SCM_TA_V2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.apache.commons.math3.distribution.*;

import SCM_TA_V1.*;

public class environment_s1 extends environment {
	
	{
		SLA=new NormalDistribution(0.8,0.1);
		TCR=new PoissonDistribution(5);
		devNetwork=new DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge>(DefaultEdge.class);
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

	
}
