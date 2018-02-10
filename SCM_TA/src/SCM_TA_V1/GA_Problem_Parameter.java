package SCM_TA_V1;

import java.rmi.dgc.DGC;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.moeaframework.algorithm.DBEA;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.Iterator;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;


public class GA_Problem_Parameter {
	static int Num_of_variables;
	static int Num_of_functions_Single=1;
	static int Num_of_functions_Multi=2;
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
	private static DAGEdge EClass=new DAGEdge();
	public static double currentTimePeriodStartTime=0;
	public static ArrayList<Integer> DevList=new ArrayList<Integer>();
	public static ArrayList<Integer> DevList_forAssignment=new ArrayList<Integer>();
	public static ArrayList<TopologicalOrderIterator<Bug, DefaultEdge>> candidateSchedulings=null;
	public static HashMap<Integer,TopologicalOrderIterator<Bug, DefaultEdge>> selectedSchedules=new HashMap<Integer, TopologicalOrderIterator<Bug,DefaultEdge>>();
	
	public static int setNum_of_Variables(Bug[] bugs){
		Num_of_variables=0;
		for(int i=0;i<bugs.length;i++){
			Num_of_variables+=bugs[i].BZone_Coefficient.size();
		}
		return Num_of_variables;
	}
	
	
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
		//all valid schedules(without any loop)
		ArrayList<ArrayList<DefaultEdge>> validSchedulings=new ArrayList<ArrayList<DefaultEdge>>();
		DefaultDirectedGraph<Bug, DefaultEdge> DDG=new DefaultDirectedGraph<Bug, DefaultEdge>(DefaultEdge.class);
		DDG=convertToDirectedGraph(DAG, DDG);
		ArrayList<DefaultEdge> potentilEdges=new ArrayList<DefaultEdge>();
		ConnectivityInspector<Bug,DefaultEdge> CI=new ConnectivityInspector<Bug, DefaultEdge>(DAG);
		//generate all valid schedules 
		for(Bug b1:DDG.vertexSet()){
			for(Bug b2:DDG.vertexSet()){
				if(b1.ID!=b2.ID && !CI.pathExists(b1, b2)){
					DDG.addEdge(b1, b2);
					//DDG.addEdge(b2,b1);
					potentilEdges.add(DDG.getEdge(b1, b2));
				}
			}
		}
		System.out.println(DDG.edgeSet().size() +"..."+potentilEdges.size());
		//find all permutation of potentialEdges list
		ICombinatoricsVector<DefaultEdge> IV=Factory.createVector(potentilEdges);
		Generator<DefaultEdge> potentialPerm=Factory.createPermutationGenerator(IV);
		for(ICombinatoricsVector<DefaultEdge> perm:potentialPerm){
			DefaultDirectedGraph<Bug, DefaultEdge> DDG2=new DefaultDirectedGraph<Bug, DefaultEdge>(DefaultEdge.class);
			DDG2=convertToDirectedGraph(DAG, DDG2);
			//DDG2=(DefaultDirectedGraph<Bug, DefaultEdge>).clone();
			ArrayList<DefaultEdge> verifiedEadges=new ArrayList<DefaultEdge>();
			DefaultEdge e=new DefaultEdge();
			Iterator<DefaultEdge> iterator_1=perm.iterator();
			Iterator<DefaultEdge> iterator_2=perm.iterator();
			ArrayList<DefaultEdge> remindEdges=new ArrayList<DefaultEdge>();
			remindEdges.clear();
			while(iterator_1.hasNext())
				remindEdges.add(iterator_1.next());
			while(iterator_2.hasNext()){
				e=iterator_2.next();
				iterator_2.remove();
				if(remindEdges.contains(e)){
					DDG2.addEdge(DDG.getEdgeSource(e), DDG.getEdgeTarget(e));
					verifiedEadges.add(e);
					update(remindEdges,e,DDG,DDG2);
				}
			}			
			//System.out.println(remindEdges.size());
			validSchedulings.add(verifiedEadges);
			for(DefaultEdge d:verifiedEadges)
				System.out.print(d+"-----");
			System.out.println();
		}
		return validSchedulings;
	}
	
	
	
	public static void update(ArrayList<DefaultEdge> edges, DefaultEdge e, DefaultDirectedGraph<Bug, DefaultEdge> DDG ,DefaultDirectedGraph<Bug, DefaultEdge> DDG_2){
		ArrayList<DefaultEdge> edges_2=(ArrayList<DefaultEdge>)edges.clone();
		try {
			DefaultEdge e_reverse=DDG.getEdge(DDG.getEdgeTarget(e), DDG.getEdgeSource(e));
			edges.remove(e_reverse);
			edges_2.remove(e_reverse);
			//DDG_2.removeEdge(DDG_2.getEdgeTarget(e), DDG_2.getEdgeSource(e));
		}
		catch (Exception e2) {
			//DDG_2.addEdge(, targetVertex)
			e2.printStackTrace();
		}
		ConnectivityInspector<Bug, DefaultEdge> CI=new ConnectivityInspector<Bug, DefaultEdge>(DDG_2);
		for(DefaultEdge ed: edges_2){
			DDG_2.addEdge(DDG.getEdgeSource(ed), DDG.getEdgeTarget(ed));
			//if(DDG_2.getEdgeSource(ed).ID!=DDG_2.getEdgeSource(e).ID && DDG_2.getEdgeTarget(ed).ID!=DDG_2.getEdgeTarget(e).ID)
			//{
			try {
				if(CI.pathExists(DDG_2.getEdgeSource(ed), DDG_2.getEdgeTarget(ed)) && CI.pathExists(DDG_2.getEdgeTarget(ed), DDG_2.getEdgeSource(ed))){
					DDG_2.removeEdge(DDG.getEdgeSource(ed), DDG.getEdgeTarget(ed));
					edges.remove(DDG_2.getEdge(DDG_2.getEdgeTarget(ed), DDG_2.getEdgeSource(ed)));
				}
			} catch (Exception e2) {
				System.out.println("error occured");
				e2.printStackTrace();
			}
			//}
		}
	}
	
	public static DirectedAcyclicGraph<Bug, DefaultEdge> getDAGModel(Bug[] bugs){
		DirectedAcyclicGraph<Bug, DefaultEdge> dag=new DirectedAcyclicGraph<Bug, DefaultEdge>(DefaultEdge.class);
		for(int k=0; k<bugs.length;k++){
			dag.addVertex(bugs[k]);
		}
		for(int i=0;i<bugs.length;i++){
			if(bugs[i].DB.size()>0){
				for(Bug b:bugs[i].DB){
						if(dag.edgeSet().size()<1){
							dag.addEdge(b,bugs[i]);
							System.out.println(dag.edgeSet());
						}
						else if(!dag.containsEdge(bugs[i],b)){
							dag.addEdge(b,bugs[i]);	
							System.out.println(dag.edgeSet());
						}
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
			@SuppressWarnings("unchecked")
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
	
	public static DefaultDirectedGraph<Bug, DefaultEdge> convertToDirectedGraph(DirectedAcyclicGraph<Bug, DefaultEdge> DAG,
			DefaultDirectedGraph<Bug, DefaultEdge> DDG){
		for(DefaultEdge d:DDG.edgeSet()){
			DDG.removeEdge(d);
		}
		for(Bug b:DDG.vertexSet()){
			DDG.removeVertex(b);
		}
		
		
		System.out.println("size of ddg"+DDG.edgeSet().size());
		for(Bug b:DAG.vertexSet()){
			DDG.addVertex(b);
		}
		for(DefaultEdge d:DAG.edgeSet()){
			DDG.addEdge(DAG.getEdgeSource(d), DAG.getEdgeTarget(d));
		}
		return DDG;
	}
	
}
