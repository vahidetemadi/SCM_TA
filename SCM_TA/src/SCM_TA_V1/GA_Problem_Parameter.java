package SCM_TA_V1;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import org.moeaframework.core.Algorithm;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.traverse.*;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.GabowStrongConnectivityInspector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import java.util.Iterator;


public class GA_Problem_Parameter {
	static int Num_of_variables;
	static int Num_of_functions_Single=1;
	static int Num_of_functions_Multi=2;
	static int Num_of_Active_Developers;
	static int Num_of_Bugs;
	static int Num_of_Zones;
	//set GA parameters
	static int population;
	static int evaluation;
	static double sbx_rate=0.0;
	static double sbx_distribution_index;
	static double pm_rate=0.50;
	static double pm_distribution_index;
	static double delayPenaltyCostRate=0.2;
	public static int upperDevId;
	public static int lowerDevId=1;
	//
	static Bug[] bugs;
	static HashMap<Integer,Developer> developers;
	public static final int startDevId=1;
	public static final int endDevId=20;
	private static DAGEdge EClass=new DAGEdge();
	public static double currentTimePeriodStartTime=0;
	public static ArrayList<Integer> DevList=new ArrayList<Integer>();
	public static ArrayList<Integer> DevList_forAssignment=new ArrayList<Integer>();
	//public static ArrayList<TopologicalOrderIterator<Bug, DefaultEdge>> candidateSchedulings=null;
	public static ArrayList<ArrayList<Bug>> candidateSchedulings=null;
	public static HashMap<Integer, ArrayList<Bug>> selectedSchedules=new HashMap<Integer, ArrayList<Bug>>();
	
	//generate DAG for arrival Bugs
	public static DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	public static TopologicalOrderIterator<Bug,DefaultEdge> tso_competenceMulti2;
	public static TopologicalOrderIterator<Bug,DefaultEdge> tso_ID;
	public static TopologicalOrderIterator<Bug,DefaultEdge> tso_RS;
	public static ArrayList<Bug> tasks=new ArrayList<Bug>();
	public static ArrayList<Bug> shuffledTasks;
	public static ArrayList<DefaultEdge> pEdges;
	public static ArrayList<DefaultEdge> shuffledPEdges;
	public static DefaultDirectedGraph<Bug, DefaultEdge> DDG;
	public static DefaultDirectedGraph<Bug, DefaultEdge> DDG_1;
	public static HashMap<String, Double> priorities=new HashMap<String, Double>();
	public static int globalIndex=0;
	public static int numOfEvaluationLocalSearch;
	public static ArrayList<ArrayList<Integer>> encodedSolutions= new ArrayList<ArrayList<Integer>>();
	public static int thresoldForPopulationGeneration=0;
	public static String pName=null;
	public static int numOfDevs=0;
	public static int fileNum=1;
	public static int runNum=1;
	public static int runNumUpTo=30;
	static ArrayList<Integer> assignment=new ArrayList<Integer>();	
	static ArrayList<Integer> schedules=new ArrayList<Integer>();
	static Set<DefaultEdge> edgeSet;
	static Set<Bug> nodeSet;
	static DirectedAcyclicGraph<Bug, DefaultEdge> DEP_scheduling;
	static int flag=0;
	public static Algorithm algorithm=null;
	//paramter for new solution in ID approach
	//ArrayList<DefaultEdge> pEdges=new ArrayList<DefaultEdge>();
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
	
	
	public static int getMaxIdofDeveloper(){
		int maxID=Collections.max(DevList, null);
		return maxID;
	}
	
	public static int getMinIdofDeveloper(){
		int minID=Collections.min(DevList, null);
		return minID;
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
	
	/**
	 * This method is used to make a copy of a initialized graph
	 * @param DAG
	 * @return a list of validated schedules
	 */
	
	public static ArrayList<ArrayList<Bug>> getValidSchedulings(DirectedAcyclicGraph<Bug, DefaultEdge> DAG){
		//all valid schedules(without any loop)
		//ArrayList<ArrayList<DefaultEdge>> validSchedulings=new ArrayList<ArrayList<DefaultEdge>>();
		DDG=new DefaultDirectedGraph<Bug, DefaultEdge>(DefaultEdge.class);
		DDG=convertToDirectedGraph(DAG, DDG);
		ArrayList<DefaultEdge> potentilEdges=new ArrayList<DefaultEdge>();
		
		System.out.println();
		//generate all possible nodes 
		for(Bug b1:DAG.vertexSet()){
			for(Bug b2:DAG.vertexSet()){
				//System.out.print(b1.ID+">>>>"+b2.ID+"....."+CI.pathExists(b1, b2)+",,,");
				if(b1.ID!=b2.ID && !(DAG.containsEdge(b1, b2) || DAG.containsEdge(b2, b1))){
					DDG.addEdge(b1, b2);
					//DDG.addEdge(b2,b1);
					potentilEdges.add(DDG.getEdge(b1, b2));
				}
			}
		}
		DDG_1=(DefaultDirectedGraph<Bug, DefaultEdge>)DDG.clone();
		System.out.println();
		pEdges=potentilEdges;

		
		ArrayList<ArrayList<Bug>> validSchedulings=new ArrayList<ArrayList<Bug>>();
		for(int k=0;k<500;k++){
			ArrayList<Bug> va=new ArrayList<Bug>();
			ArrayList<Bug> travesredNodes=new ArrayList<Bug>();
			Random randomGenerator=new Random();
			int rIndex=0;
			for(Bug b:DAG.vertexSet()){
				if(DAG.inDegreeOf(b)==0){
					travesredNodes.add(b);
				}
			}
			while(!travesredNodes.isEmpty()){
				rIndex=randomGenerator.nextInt(travesredNodes.size());
				va.add(travesredNodes.get(rIndex));
				Set<DefaultEdge> edges=DAG.outgoingEdgesOf(travesredNodes.get(rIndex));
				travesredNodes.remove(travesredNodes.get(rIndex));
				for(DefaultEdge d:edges){
					travesredNodes.add(DAG.getEdgeTarget(d));
				}
			}
			validSchedulings.add(va);	
		}
		
		
		/*System.out.println("comp: "+components.size());
		for(int i=0;i<10;i++){
			Collections.shuffle(subgraphs);
			for(AsSubgraph<Bug, DefaultEdge> g:subgraphs){
				TopologicalOrderIterator<Bug, DefaultEdge> TO=new TopologicalOrderIterator(g);
				while(TO.hasNext()){
					va.add(TO.next());
				}
			}
			
		}*/
		
		
		/*for(ArrayList<Bug> ab:validSchedulings){
			for(Bug b:ab){
				System.out.print(b.ID+"---");
			}
			System.out.println();
		}*/
		
		return validSchedulings;
	}
	
	
	
	public static void update(ArrayList<DefaultEdge> edges, DefaultEdge e, DefaultDirectedGraph<Bug, DefaultEdge> DDG ,DefaultDirectedGraph<Bug, DefaultEdge> DDG_2
			, ArrayList<DefaultEdge> verifiedEdges){
		CycleDetector<Bug,DefaultEdge> CD=new CycleDetector<Bug, DefaultEdge>(DDG_2);
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
		for(DefaultEdge ed: edges_2){	
			DDG_2.addEdge(DDG.getEdgeSource(ed), DDG.getEdgeTarget(ed));
			//verifiedEdges.add(ed);
			//if(DDG_2.getEdgeSource(ed).ID!=DDG_2.getEdgeSource(e).ID && DDG_2.getEdgeTarget(ed).ID!=DDG_2.getEdgeTarget(e).ID)
			//{
			try {
				//if(CI.pathExists(DDG_2.getEdgeSource(ed), DDG_2.getEdgeTarget(ed)) && CI.pathExists(DDG_2.getEdgeTarget(ed), DDG_2.getEdgeSource(ed))){
				if(CD.detectCycles()){
					//System.out.println(CD.detectCycles());
					edges.remove(DDG_2.getEdge(DDG_2.getEdgeTarget(ed), DDG_2.getEdgeSource(ed)));
					DDG_2.removeEdge(DDG.getEdgeSource(ed), DDG.getEdgeTarget(ed));
					//verifiedEdges.remove(ed);
				}
			} catch (Exception e2) {
				System.out.println("error occured");
				e2.printStackTrace();
			}


			//}
		}
		//System.out.println(edges.size());
	}
	
	public static DirectedAcyclicGraph<Bug, DefaultEdge> getDAGModel(Bug[] bugs){
		
		DirectedAcyclicGraph<Bug, DefaultEdge> dag=new DirectedAcyclicGraph<Bug, DefaultEdge>(DefaultEdge.class);
		
		for(int k=0; k<bugs.length;k++){
			dag.addVertex(bugs[k]);
		}
		for(int i=0;i<bugs.length;i++){
			if(bugs[i].DB.size()>0){
				for(Bug b:bugs[i].DB){
						if(dag.edgeSet().size()<1 && dag.containsVertex(bugs[i])){
							try{
								dag.addEdge(b,bugs[i]);
								}
							catch(Exception ex){
								if(b==null)
									System.out.println("f");
								else if (bugs[i]==null)
									System.out.println("f-f");
								ex.printStackTrace();
							}
							//System.out.println(dag.edgeSet());
						}
						else if(!dag.containsEdge(bugs[i],b)){
							if(b!=null && bugs[i]!=null)
								dag.addEdge(b,bugs[i]);	
							//System.out.println(dag.edgeSet());
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
		
		TopologicalOrderIterator<Bug, DefaultEdge> tso=new TopologicalOrderIterator<Bug, DefaultEdge>(dag);
		
		return tso;
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
	
	public static void resetParameters(DirectedAcyclicGraph<Bug, DefaultEdge> DEP,Solution s, HashMap<Integer, Developer> developers){
		for(Bug b:DEP.vertexSet()){
			b.startTime_evaluate=0.0;
			b.endTime_evaluate=0.0;
			for(Zone z:b.Zone_DEP.vertexSet()){
				z.zoneStartTime_evaluate=0.0;
				z.zoneEndTime_evaluate=0.0;
			}	
		}
		for(Entry<Integer, Developer> d:developers.entrySet()){
			d.getValue().developerNextAvailableHour=0.0;
		}
	}
	
	public static void resetParameters_ZoneAndDevelopers(Bug b,Solution s, HashMap<Integer, Developer> developers){
		for(Zone z:b.Zone_DEP.vertexSet()){
			z.zoneStartTime_evaluate=0.0;
			z.zoneEndTime_evaluate=0.0;
		}
		/*for(Entry<Integer, Developer> d:developers.entrySet()){
			d.getValue().developerNextAvailableHour=0.0;
		}*/
	}
	public static void assignZoneDev(ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee,ArrayList<Bug> tasks,Solution s){
		int index=0;
		for(Bug b:tasks){
			for(Zone zone:b.Zone_DEP){
				zoneAssignee.add(new Triplet<Bug, Zone, Integer>(b, zone, EncodingUtils.getInt(s.getVariable(index))));
				index++;
			}
		}
		
		/*
		while(EncodingUtils.getInt(s.getVariable(index))!=-100){
			Bug b=tasks.get(tIndex);
			for(Zone zone:b.Zone_DEP){
				zoneAssignee.add(new Triplet<Bug, Zone, Integer>(b, zone, EncodingUtils.getInt(s.getVariable(index))));
				index++;
			}
			tIndex++;
		}
		globalIndex=index;*/
	}
	
	public static void setCandidateSchedulings(ArrayList<ArrayList<Bug>> validSchedulings ){
		candidateSchedulings=validSchedulings;
		/*for(DirectedAcyclicGraph<Bug, DefaultEdge> schedule:validSchedulings){
			candidateSchedulings.add(getTopologicalSorted(schedule));
		}*/
		
	}
	
	public static DefaultDirectedGraph<Bug, DefaultEdge> convertToDirectedGraph(DirectedAcyclicGraph<Bug, DefaultEdge> DAG,
			DefaultDirectedGraph<Bug, DefaultEdge> DDG){
		
		if(!DDG.vertexSet().isEmpty()){
			for(DefaultEdge d:DDG.edgeSet()){
				DDG.removeEdge(d);
			}
			for(Bug b:DDG.vertexSet()){
				DDG.removeVertex(b);
			}
		}
		
		//System.out.println("size of ddg"+DDG.edgeSet().size());
		for(Bug b:DAG.vertexSet()){
			DDG.addVertex(b);
		}
		for(DefaultEdge d:DAG.edgeSet()){
			DDG.addEdge(DAG.getEdgeSource(d), DAG.getEdgeTarget(d));
		}
		return DDG;
	}
	
	public static void generateModelofBugs(){
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso_competenceMulti2=GA_Problem_Parameter.getTopologicalSorted(DEP);
		tso_ID=GA_Problem_Parameter.getTopologicalSorted(DEP);
		tso_RS=GA_Problem_Parameter.getTopologicalSorted(DEP);
	}
	
	
	public static void candidateSolutonGeneration(){
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP=GA_Problem_Parameter.getDAGModel(GA_Problem_Parameter.bugs);
		//generate all the candidate schedules
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation_scheduling=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		ArrayList<ArrayList<Bug>> validSchedulings = GA_Problem_Parameter.getValidSchedulings(DEP_evaluation_scheduling);
		GA_Problem_Parameter.setCandidateSchedulings(validSchedulings);
	}
	
	/** 
	 update the schedules for each generated offspring produced by crossover operator   
	**/
	public static Solution setValidSchdule(Solution solution, HashMap<Integer, Bug> varToBug){
		assignment.clear();
		schedules.clear();
		/*edgeSet=DEP_scheduling.edgeSet();
		DEP_scheduling.removeAllEdges(edgeSet);*/
		DEP_scheduling=(DirectedAcyclicGraph<Bug, DefaultEdge>) GA_Problem_Parameter.DEP.clone();	
		int[] solu=EncodingUtils.getInt(solution);
		for(int i=0;i<solu.length;i++){
			if(solu[i]!=-100){
				assignment.add(solu[i]);
			}
			else {
				break;
			}
		}
		
		for(int i=assignment.size()+1;i<solu.length-1;i++){
			schedules.add(solu[i]);
		}
		
		int sizeTest=GA_Problem_Parameter.pEdges.size();
		int scheculeSize=schedules.size();
		int m,n,p,q;
		int[] indexes=new int[2];
		AllDirectedPaths<Bug, DefaultEdge> paths;
		int vertexSetSize=GA_Problem_Parameter.tasks.size()-1;
		/*if(vertexSetSize>40)
			vertexSetSize/=3;*/
		for(int i=0;i<vertexSetSize;i++){
			indexes=getIndex(i);
			m=indexes[0];
			n=indexes[1];
			for(int j=i+1;j<GA_Problem_Parameter.tasks.size();j++){
				indexes=getIndex(j);
				p=indexes[0];
				q=indexes[1];
				if(compareSubtasksAssignee(m,n,p,q,assignment)){
					try{
						paths=new AllDirectedPaths<Bug, DefaultEdge>(DEP_scheduling);
						if(paths.getAllPaths(varToBug.get(i), varToBug.get(j), true, 10000).isEmpty() && paths.getAllPaths(varToBug.get(j), varToBug.get(i), true, 10000).isEmpty()){
							int t=-1;
							try{
								t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(i), varToBug.get(j)));
								if(t<0){
									t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(j), varToBug.get(i)));
								}
								DEP_scheduling.addEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
										GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
							}
							catch(Exception ex)
							{
								
							} 
							if(!new CycleDetector<Bug, DefaultEdge>(DEP_scheduling).detectCycles())
								schedules.set(t, 1);
							else
								DEP_scheduling.removeEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
										GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
						}
					}
					catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		}
		int j=0;
		int tttt=solution.getNumberOfVariables();
		int ttttt=assignment.size();
		
		for(int i=assignment.size()+1;i<solution.getNumberOfVariables();i++){
			solution.setVariable(i, EncodingUtils.newInt(schedules.get(j),schedules.get(j)));
			j++;
			if(j>71)
				break;
		}
		
		//should be called as a part of solution preparation
		for (int k = 0; k < solution.getNumberOfVariables(); k++) {
			solution.getVariable(k).randomize();
		}
		DEP_scheduling=null;
		paths=null;
		return solution;
	}
	
	/**get index of the subtasks' assignees need to be compared**/
	public static int[] getIndex(int index){
		int[] indexes=new int[2];
		int sIndex=0;
		for(int i=0;i<index;i++){
			sIndex+=GA_Problem_Parameter.tasks.get(i).BZone_Coefficient.size();
		}
		indexes[0]=sIndex;
		indexes[1]=sIndex+GA_Problem_Parameter.tasks.get(index).BZone_Coefficient.size();
		return indexes;
	}
	
	/** compare subtasks to find the potential links**/
	public static Boolean compareSubtasksAssignee(int i, int j,int p, int k, ArrayList<Integer> assignment){
		Boolean b=false;
		outerloop:
		for(int r=i;r<j;r++){
			for(int s=p;s<k;s++)
				if(assignment.get(r)==assignment.get(s)){
					b=true;
					break outerloop;
				}
		}
		
		return b;
	}
	
	/** assign the each bug an index of the associated variable in the encoded solution**/
	public static HashMap<Integer, Bug> getVarToBug(){
		int index=0;
		HashMap<Integer, Bug> varToBug=new HashMap<Integer, Bug>();
		for(Bug b:GA_Problem_Parameter.tasks){
			varToBug.put(index, b);
			index++;
		} 
		return varToBug;
	}
	
	/**set arrival task**/
	public static void setArrivalTasks(){
		GA_Problem_Parameter.tasks.clear();
		while(tso_ID.hasNext()){
			Bug b=tso_ID.next();
			GA_Problem_Parameter.tasks.add(b);
		}
		tso_ID=GA_Problem_Parameter.getTopologicalSorted(DEP);
	}
	public static void createPriorityTable(){
		priorities.put("P1", 0.9);
		priorities.put("P2", 0.6);
		priorities.put("P3", 0.3);
	}
	
	public static void pruneList(HashMap<Integer, Bug> tasks_prune){
		if(tasks_prune.size()>20){
			int _size=(tasks_prune.size()*3)/4;
			System.out.println(tasks_prune.size()+"***");
			ArrayList<Integer> bugsID=new ArrayList<Integer>();
			for(Integer ID:tasks_prune.keySet()){
				bugsID.add(ID);
			}
			for(int i=0;i<_size;i++){
				int ran=new Random().nextInt(bugsID.size());
				tasks_prune.remove(bugsID.get(ran));
				bugsID.remove(ran);
			}

			System.out.println(tasks_prune.size()+"///");
		}
	}
	
	public static void pruneDevList(HashMap<Integer, Developer> devs_prune){
		int _size=devs_prune.size()/2;
		System.out.println(devs_prune.size()+"***devs");
		ArrayList<Integer> devsID=new ArrayList<Integer>();
		for(Integer ID:devs_prune.keySet()){
			devsID.add(ID);
		}
		for(int i=0;i<_size;i++){
			int ran=new Random().nextInt(devsID.size());
			devs_prune.remove(devsID.get(ran));
			devsID.remove(ran);
		}

		System.out.println(devs_prune.size()+"///devs");
	}
	
	public static void pruneDevList(HashMap<Integer, Developer> devs_prune, ArrayList<Ranking<Developer, Double>> devs, int portion){
			int _size=devs_prune.size()-(int)(devs_prune.size()*portion)/100;
			System.out.println(devs_prune.size()+"***devs");
			int i=1;
			for(Ranking<Developer, Double> r:devs){
				if(i>_size){
					break;
				}
				devs_prune.remove(r.getEntity().getID());
				i++;
			}

			System.out.println(devs_prune.size()+"///devs");
	}
	
	
}
