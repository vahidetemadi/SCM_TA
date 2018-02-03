package SCM_TA_V1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.moeaframework.algorithm.DBEA;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.alg.ConnectivityInspector;
import java.util.Iterator;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;


public class GA_Problem_Parameter {
	static int Num_of_variables;
	static int Num_of_functions_Single=1;
	static int Num_of_functions_Multi;
	static int Num_of_Active_Developers;
	static int Num_of_Bugs;
	static int Num_of_Zones;
	//set GA parameters
	static int population;
	static double sbx_rate;
	static double sbx_distribution_index;
	static double pm_rate;
	static double pm_distribution_index;
	static double delayPenaltyCostRate=0.33;
	//
	static Bug[] bugs;
	static HashMap<Integer,Developer> developers;
	public static final int startDevId=1;
	public static final int endDevId=20;
	private static final Class<? extends DefaultEdge> EClass = null;
	public static double currentTimePeriodStartTime=0;
	public static ArrayList<Integer> DevList=new ArrayList<Integer>();
	public static ArrayList<Integer> DevList_forAssignment=new ArrayList<Integer>();
	public static ArrayList<TopologicalOrderIterator<Bug, DefaultEdge>> candidateSchedulings=null;
	public static HashMap<Integer,TopologicalOrderIterator<Bug, DefaultEdge>> selectedSchedules=new HashMap<Integer, TopologicalOrderIterator<Bug,DefaultEdge>>();
	/*public void setNum_of_Variables(){
		Num_of_variables=Num_of_Bugs*Num_of_Zones;
	}*/
	
	
	public static void initializeDeveloperPool(){
		for(int i=0;i<3;i++){
			for(Integer dev:DevList)
				DevList_forAssignment.add(dev);
		}
	}
	
	public static int getRandomDevId(){
		Random rg=new Random();
		int index=rg.nextInt(DevList.size());
		return DevList.get(index);
	}
	
	
	public static int getDevId(){
		if(DevList_forAssignment.size()>0){
			Random rg=new Random();
			int index=rg.nextInt(DevList_forAssignment.size());
			int devId=DevList_forAssignment.get(index);
			DevList_forAssignment.remove(index);
			return devId;
		}
		else{
			return -1;
		}	
	}
	
	
	public static ArrayList<ArrayList<DefaultEdge>> getValidSchedulings(DirectedAcyclicGraph<Bug, DefaultEdge> DAG){
		//all valid schedulings(without any loop)
		ArrayList<ArrayList<DefaultEdge>> validSchedulings=new ArrayList<ArrayList<DefaultEdge>>(null);
		
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DAG_2=(DirectedAcyclicGraph<Bug, DefaultEdge>) DAG.clone();
		ArrayList<DefaultEdge> potentilEdges=new ArrayList<DefaultEdge>();
		ConnectivityInspector<Bug,DefaultEdge> CI=new ConnectivityInspector<Bug, DefaultEdge>(DAG);
		for(Bug b1:DAG_2.vertexSet()){
			for(Bug b2:DAG_2.vertexSet()){
				if(b1!=b2 && !CI.pathExists(b1, b2) && CI.pathExists(b2, b1)){
					potentilEdges.add(DAG_2.addEdge(b1,b2));
					potentilEdges.add(DAG_2.addEdge(b2,b1));
				}
			}
		}
		
		//find all permutation of potentialEdges list
		ICombinatoricsVector<DefaultEdge> IV=Factory.createVector(potentilEdges);
		Generator<DefaultEdge> potentialPerm=Factory.createPermutationGenerator(IV);
		for(ICombinatoricsVector<DefaultEdge> perm:potentialPerm){
			DAG_2=(DirectedAcyclicGraph<Bug, DefaultEdge>) DAG.clone();
			ArrayList<DefaultEdge> verifiedEadges=new ArrayList<DefaultEdge>();
			DefaultEdge e=new DefaultEdge();
			Iterator<DefaultEdge> iterator=perm.iterator();
			ArrayList<DefaultEdge> remindEdges=(ArrayList<DefaultEdge>)perm;
			while(iterator.hasNext()){
				e=(DefaultEdge) iterator.next().clone();
				iterator.remove();
				if(remindEdges.contains(e)){
					verifiedEadges.add(e);
					update(remindEdges,e,DAG_2);
				}
			}
			
			validSchedulings.add(verifiedEadges);
		}
		
		
		return validSchedulings;
	}
	
	
	
	public static void update(ArrayList<DefaultEdge> edges, DefaultEdge e, DirectedAcyclicGraph<Bug,DefaultEdge> dag){
		DefaultEdge e_reverse=dag.getEdge(dag.getEdgeTarget(e), dag.getEdgeSource(e));
		edges.remove(e_reverse);
		ConnectivityInspector<Bug, DefaultEdge> CI=new ConnectivityInspector<Bug, DefaultEdge>(dag);
		for(DefaultEdge ed: edges){
			if(CI.pathExists(dag.getEdgeSource(ed), dag.getEdgeSource(ed))){
				edges.remove(dag.getEdge(dag.getEdgeTarget(ed), dag.getEdgeSource(ed)));
				edges.remove(ed);
			}
		}
	}
	
	public static DirectedAcyclicGraph<Bug, DefaultEdge> getDAGModel(Bug[] bugs){
		DirectedAcyclicGraph<Bug, DefaultEdge> dag=new DirectedAcyclicGraph<Bug, DefaultEdge>(EClass);
		for(int i=0;i<bugs.length;i++){
			if(bugs[i].DB.size()>0){
				for(Bug b:bugs[i].DB){
					if(!dag.containsEdge(bugs[i],b))
						dag.addEdge(b,bugs[i]);
				}
			}
			else{
				dag.addVertex(bugs[i]);
			}
		}
		return dag;
	}
	
	public static ArrayList<DefaultEdge> getEdges(ArrayList<Bug> tasks){
	
		return new ArrayList<DefaultEdge>();
	}
	

	public static TopologicalOrderIterator<Bug, DefaultEdge> getTopologicalSorted(DirectedAcyclicGraph<Bug, DefaultEdge> dag){
		return new TopologicalOrderIterator<Bug, DefaultEdge>(dag);
	}
	
	
	public static ArrayList<DirectedAcyclicGraph<Bug, DefaultEdge>> getReScheduledGraphs(DirectedAcyclicGraph<Bug, DefaultEdge> DAG 
			, ArrayList<ArrayList<DefaultEdge>> validSchedulings){
		ArrayList<DirectedAcyclicGraph<Bug, DefaultEdge>> schedulings=new ArrayList<DirectedAcyclicGraph<Bug,DefaultEdge>>();
		for(ArrayList<DefaultEdge> candidateSchedule:validSchedulings){
			DirectedAcyclicGraph<Bug, DefaultEdge> ReScheduledDAG=(DirectedAcyclicGraph<Bug, DefaultEdge>)DAG.clone();
			for(DefaultEdge edge:candidateSchedule){
			ReScheduledDAG.addEdge(DAG.getEdgeSource(edge), DAG.getEdgeTarget(edge));
		}
		schedulings.add(ReScheduledDAG);
		}
		
		return schedulings;
	}
	
	public static void resetParameters(DirectedAcyclicGraph<Bug, DefaultEdge> DEP,Solution s){
		for(Bug b:DEP.vertexSet()){
			b.startTime_evaluate=0.0;
			b.endTime_evaluate=0.0;
			for(Zone z:b.Zone_DEP.vertexSet()){
				z.zoneStartTime_evaluate=0.0;
				z.zoneEndTime_evaluate=0.0;
			}
				
		}
	}
	public static void assignZoneDev(TopologicalOrderIterator<Bug, DefaultEdge> TSO,Solution s){
		int numOfVar=0;
		while(TSO.hasNext()){
			Bug b=TSO.next();
			for(Zone z:b.Zone_DEP){
				z.assignedDevID=EncodingUtils.getInt(s.getVariable(numOfVar));
			}
		}
	}
	
	public static void setCandidateSchedulings(ArrayList<DirectedAcyclicGraph<Bug, DefaultEdge>> validSchedulings ){
		candidateSchedulings=new ArrayList<TopologicalOrderIterator<Bug,DefaultEdge>>();
		for(DirectedAcyclicGraph<Bug, DefaultEdge> schedule:validSchedulings){
			candidateSchedulings.add(getTopologicalSorted(schedule));
		}
		
	}
	
}
