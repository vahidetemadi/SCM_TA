package SCM_TA_V1;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import java.text.SimpleDateFormat;
import java.util.Date;
public class SchedulingDriven extends AbstractProblem{
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	ArrayList<ArrayList<Integer>> schedules=new ArrayList<ArrayList<Integer>>();
	HashMap<Integer,Bug> varToBug=new HashMap<Integer, Bug>(); //supposed to be used for mapping the item in chromosome to a particular bug
	/*ArrayList<ArrayList<ArrayList<Integer>>> variables=new ArrayList<ArrayList<ArrayList<Integer>>>();
	//ArrayList<ArrayList<Integer>> variable=new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> encodedSolution= new ArrayList<Integer>();
	ArrayList<ArrayList<Integer>> encodedSolutions= new ArrayList<ArrayList<Integer>>();*/
	ArrayList<Integer> assignment=new ArrayList<Integer>();
	TopologicalOrderIterator<Zone,DefaultEdge> tso_zones;
	AllDirectedPaths<Bug, DefaultEdge> paths;
	DefaultDirectedGraph<Bug, DefaultEdge> DEP_scheduling;
	ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee=new ArrayList<Triplet<Bug,Zone,Integer>>();
	public SchedulingDriven(){
		super(GA_Problem_Parameter.setNum_of_Variables(bugs),GA_Problem_Parameter.Num_of_functions_Multi);
		//this.bugs=bugs;
		//this.developers= new ArrayList<Developer>(Arrays.asList(developers));
	}
	
	
	public ArrayList<ArrayList<Integer>> getSchedules(Solution solution){
		int indexTotal=0;
			//clear the list holds mapping solution element index to bugs
			varToBug.clear();
			assignment.clear();
			schedules.clear();
			for(Integer devID:EncodingUtils.getInt(solution)){
				assignment.add(devID);
			}
			
			int index=0;
			for(Bug b:GA_Problem_Parameter.tasks){
				b.setZoneDEP();
				varToBug.put(index, b);
				index++;
			}
			
			for(int i=0;i<GA_Problem_Parameter.numOfEvaluationLocalSearch;i++){
				schedules.add(generateSchedule());
			}
			
		return schedules;
	}
		
	
	
	
	public void preInit(){
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso_ID;
		/*
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);*/
		while(tso.hasNext()){
			Bug b=tso.next();
			b.setZoneDEP();
			TopologicalOrderIterator<Zone,DefaultEdge> tso_zones=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_zones.hasNext()){
				genes.add(tso_zones.next());
			}
		}
	}
	
	
	/*public static String arrayToString(ArrayList<Integer> list){
		String s="";
		for(Integer i:list){
			s+=i+",";
		}
		s=s.substring(0, s.length()-2);
		return s;
	}*/
	
	/*public static ArrayList<Integer> stringToList(String s){
		ArrayList<Integer> list=new ArrayList<Integer>();
		for(String st:s.split(",")){
			list.add(Integer.parseInt(st));
		}
		return list;
	}*/
	
	@Override
	public Solution newSolution(){
		preInit();
		//changed NUM of variables for the solution
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions_Multi);
		int j=0;
		for(Zone z:genes){
			int randDevId=GA_Problem_Parameter.getRandomDevId();
			solution.setVariable(j,EncodingUtils.newInt(randDevId, randDevId));
			j++;
		}
		return solution;
	}
		
	@Override 	
	public void evaluate(Solution solution){
		long start=System.currentTimeMillis();
		@SuppressWarnings("unchecked")
		int test=GA_Problem_Parameter.DEP.vertexSet().size();
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation;
	
		//reset all the associate time for the bugs and their zones
		
		
		//evaluate and examine for all the candidate schedules and then, pick the minimum one 
		solution.setSchedules(getSchedules(solution));
		int counter=0;
		int rrr=solution.getSchedules().size();
		for(ArrayList<Integer> sche:solution.getSchedules()){
			DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) GA_Problem_Parameter.DEP.clone();
			GA_Problem_Parameter.resetParameters(DEP_evaluation,solution, developers);
			//assign Devs to zone
			zoneAssignee.clear();
			GA_Problem_Parameter.assignZoneDev(zoneAssignee,GA_Problem_Parameter.tasks, solution);
			for(int i=0;i<GA_Problem_Parameter.pEdges.size();i++){
				if(sche.get(i)==1){
					DEP_evaluation.addEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(i)),
							GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(i)));
				}
			}
			TopologicalOrderIterator<Bug, DefaultEdge> tso=new TopologicalOrderIterator<Bug, DefaultEdge>(DEP_evaluation);
			double totalTime=0.0;
			double totalCost=0.0;
			double totalDevCost=0.0;
			double totalDelayTime=0.0;
			double totalDelayCost=0.0;
			double totalStartTime=0.0;
			double totalEndTime=0.0;
			double totalExecutionTime=0.0;
			int index=0;
			start=System.currentTimeMillis();
			while(tso.hasNext()){
				Bug b=tso.next();
				//set Bug startTime
				Date date=new Date();
				SimpleDateFormat d=new SimpleDateFormat();
				Date d1=null;
				Date d2=null;
				double x=fitnessCalc.getMaxEndTimes(b, DEP_evaluation);
				b.startTime_evaluate=x;
				TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
				while(tso_Zone.hasNext()){
					Zone zone=tso_Zone.next();
					double compeletionTime=0.0;
					Entry<Zone, Double> zone_bug=new AbstractMap.SimpleEntry<Zone, Double>(zone,b.BZone_Coefficient.get(zone));
					int devId=zoneAssignee.get(index).getThird();
					compeletionTime=fitnessCalc.compeletionTime(b,zone_bug, developers.get(devId));
					totalExecutionTime+=compeletionTime;
					totalDevCost+=compeletionTime*developers.get(zoneAssignee.get(index).getThird()).hourlyWage;
					zone.zoneStartTime_evaluate=b.startTime_evaluate+fitnessCalc.getZoneStartTime(developers.get(zoneAssignee.get(index).getThird()), zone.DZ);
					zone.zoneEndTime_evaluate=zone.zoneStartTime_evaluate+compeletionTime;
					developers.get(zoneAssignee.get(index).getThird()).developerNextAvailableHour=Math.max(developers.get(zoneAssignee.get(index).getThird()).developerNextAvailableHour,
							zone.zoneEndTime_evaluate);
					b.endTime_evaluate=Math.max(b.endTime_evaluate, zone.zoneEndTime_evaluate);
					index++;
				}
				totalStartTime=Math.min(totalStartTime, b.startTime_evaluate);
				totalEndTime=Math.max(totalEndTime, b.endTime_evaluate);
				totalDelayTime+=b.endTime_evaluate-(2.5*totalExecutionTime+totalExecutionTime);
				if(totalDelayTime>0)
					totalDelayCost+=totalDelayTime*GA_Problem_Parameter.priorities.get(b.priority);
			}
			totalTime=totalEndTime-totalStartTime;
			totalCost=totalDevCost+totalDelayCost;
			if(counter==0){
				solution.setObjective(0, totalTime);
				solution.setObjective(1, totalCost);
				solution.setSchedule(sche);
			}
			else if(compare(totalTime, totalCost, solution)==1){
				solution.setObjective(0, totalTime);
				solution.setObjective(1, totalCost);
				solution.setSchedule(sche);
			}
			
		}		
		long end=System.currentTimeMillis();
		long diff=(long) (end-start);
		int t=0;
	}
	

	public ArrayList<Integer> generateSchedule(){
		//schedule has the same size of pEdges (potential edges)
		ArrayList<Integer> schedule=new ArrayList<Integer>();
		
		
		//initialization
		for(int i=0;i<GA_Problem_Parameter.pEdges.size();i++)
			schedule.add(0);
		
		/* generate valid schedules*/
		//m: startIndex, n:endIndex, 
		int m,n,p,q;
		int[] indexes=new int[2];
		//create DEP_scheduling graph to add potential links used for a new schedule 
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_scheduling=(DirectedAcyclicGraph<Bug, DefaultEdge>) GA_Problem_Parameter.DEP.clone();
		//DEP_scheduling=GA_Problem_Parameter.convertToDirectedGraph(GA_Problem_Parameter.DEP, DEP_scheduling);
		ArrayList<Integer> indices=new ArrayList<Integer>();
		
		//shuffling the bug list to provide randomness in the scheduling
		GA_Problem_Parameter.shuffledTasks=(ArrayList<Bug>) GA_Problem_Parameter.tasks.clone();
		Collections.shuffle(GA_Problem_Parameter.shuffledTasks);
		int vertexSetSize=GA_Problem_Parameter.tasks.size()-1;
		/*if(vertexSetSize>40)
			vertexSetSize/=3;*/
		for(int i=0;i<vertexSetSize;i++){
			indexes=getIndex(i);
			m=indexes[0];
			n=indexes[1];
			for(int j=i+1;j<vertexSetSize+1;j++){
				indexes=getIndex(j);
				p=indexes[0];
				q=indexes[1];
				if(compareSubtasksAssignee(m,n,p,q,assignment)){
					paths=new AllDirectedPaths<Bug, DefaultEdge>(DEP_scheduling);
					try{
						if(paths.getAllPaths(varToBug.get(i),varToBug.get(j), true, 10000).isEmpty() && paths.getAllPaths(varToBug.get(j), varToBug.get(i), true, 10000).isEmpty()){
							int t=-1;
							//get the information for potential link-- index t determines if the edge already exists there
							//DDG_1 is the fully connected graph of all bugs 
							t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(i), varToBug.get(j)));
							if(t<0){
								t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(j), varToBug.get(i)));
							}
							// in case a link exits we add that link to DEP_scheduling (the graph of a specific schedule)
							DEP_scheduling.addEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
									GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
							//check if added link does not result in a cycle otherwise the added link gets removed
							boolean b=new CycleDetector<Bug, DefaultEdge>(DEP_scheduling).detectCycles();
							if(!b)
								schedule.set(t, 1);
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
		DEP_scheduling=null;
		paths=null;
		return schedule;
	}

	//get the start and end indices of each bug in the choromosome
	public int[] getIndex(int index){
		int[] indexes=new int[2];
		int sIndex=0;
		for(int i=0;i<index;i++){
			sIndex+=GA_Problem_Parameter.tasks.get(i).BZone_Coefficient.size();
		}
		indexes[0]=sIndex;
		indexes[1]=sIndex+GA_Problem_Parameter.tasks.get(index).BZone_Coefficient.size();
		return indexes;
	}
	
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
	
	public void generateDAG(){
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
	}

	public int compare(double totalTime, double totalCost, Solution solution){
		if(solution.getObjective(0)<totalTime && solution.getObjective(1)<totalCost)
			return -1;
		else if(solution.getObjective(0)<totalTime && solution.getObjective(1)==totalCost)
			return -1;
		else if(solution.getObjective(0)==totalTime && solution.getObjective(1)<totalCost)
			return -1;
		else if(solution.getObjective(0)==totalTime && solution.getObjective(1)==totalCost)
			return 0;
		else 
			return 1;
	}
}
