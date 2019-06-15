package SCM_TA_V1;

import java.sql.Time;
import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;

import javax.swing.JViewport;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.ext.IntegerComponentNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import com.google.common.collect.ArrayListMultimap;

import java.text.SimpleDateFormat;
import java.util.Date;
public class SchedulingDriven extends AbstractProblem{
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	ArrayList<ArrayList<Integer>> schedules=new ArrayList<ArrayList<Integer>>();
	HashMap<Integer,Bug> varToBug; //supposed to be used for mapping the item in chromosome to a particular bug
	ArrayList<ArrayList<ArrayList<Integer>>> variables=new ArrayList<ArrayList<ArrayList<Integer>>>();
	//ArrayList<ArrayList<Integer>> variable=new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> encodedSolution= new ArrayList<Integer>();
	ArrayList<ArrayList<Integer>> encodedSolutions= new ArrayList<ArrayList<Integer>>();
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
	
	
	public void init(){
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso_ID;
		
		varToBug=new HashMap<Integer, Bug>();
		//variables holds the list of choromosome and schedule
		variables.clear();
		assignment.clear();
		encodedSolutions.clear();
		
		/* copy all the arrival bugs to the tasks */

		
		//sort the subtasks for a bug-- initialize the assignment choromosome-- set the varToBug dictionary
		int index=0;
		for(Bug b:GA_Problem_Parameter.tasks){
			b.setZoneDEP();
			tso_zones=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_zones.hasNext()){
				genes.add(tso_zones.next());
				assignment.add(GA_Problem_Parameter.getRandomDevId());
			}
			varToBug.put(index, b);
			index++;
		}
		int numOfSchedules=1;
		
		for(int i=0;i<numOfSchedules;i++){
			schedules.add(generateSchedule());
		}
		
		for(ArrayList<Integer> schedule:schedules){
			ArrayList<ArrayList<Integer>> variable=new ArrayList<ArrayList<Integer>>();
			variable.add(assignment);
			variable.add(schedule);
			variables.add(variable);
		}
		
		//create final list
		for(ArrayList<ArrayList<Integer>> var:variables){
			ArrayList<Integer> encodedSolution=new ArrayList<Integer>();
			for(ArrayList<Integer> list:var){
				for(Integer i:list){
					encodedSolution.add(i);
				}
				encodedSolution.add(-100);
			}
			encodedSolutions.add(encodedSolution);	
		}
	}
	
	public static Boolean compareSubtasksAssignee(int i, int j,int p, int k, ArrayList<Integer> assignment){
		Boolean b=false;
		for(int r=i;r<j;r++){
			for(int s=p;s<k;s++)
				if(assignment.get(r)==assignment.get(s))
					b=true;
		}
		
		return b;
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
		init();
		//changed NUM of variables for the solution
		Solution solution=new Solution(encodedSolutions.get(0).size(),GA_Problem_Parameter.Num_of_functions_Multi);
		for(int i=0;i<encodedSolutions.get(0).size();i++){
				solution.setVariable(i,EncodingUtils.newInt(encodedSolutions.get(0).get(i), encodedSolutions.get(0).get(i)));
				
		}
		return solution;
	}
		
	@Override 	
	public void evaluate(Solution solution){
		ArrayList<Integer> temp=new ArrayList<Integer>();
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		//reset all the associate time for the bugs and their zones
		GA_Problem_Parameter.resetParameters(DEP_evaluation,solution, developers);
		//assign Devs to zone
		zoneAssignee.clear();
		GA_Problem_Parameter.assignZoneDev(zoneAssignee,GA_Problem_Parameter.tasks, solution );
		//evaluate and examine for all the candidate schedules and then, pick the minimum one 
		ArrayList<Integer> sche=new ArrayList<Integer>();
		int[] solu=EncodingUtils.getInt(solution);
		for(int i=assignment.size()+1;i<solu.length-1;i++){
			sche.add(solu[i]);
		}
		
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
				temp.add(EncodingUtils.getInt(solution.getVariable(index)));
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
		
		solution.setObjective(0, totalTime);
		solution.setObjective(1, totalCost);
	}
	

	public ArrayList<Integer> generateSchedule(){
		//schedule has the same size of pEdges (potential edges)
		ArrayList<Integer> schedule=new ArrayList<Integer>();
		//initialization
		for(int i=0;i<GA_Problem_Parameter.pEdges.size();i++)
			schedule.add(0);
		
		/* generate valid schedules*/
		int m,n,p,q;
		int[] indexes=new int[2];
		paths=new AllDirectedPaths<Bug, DefaultEdge>(DEP);
		DefaultDirectedGraph<Bug, DefaultEdge> DEP_scheduling=new DefaultDirectedGraph<Bug, DefaultEdge>(DefaultEdge.class);
		DEP_scheduling=GA_Problem_Parameter.convertToDirectedGraph(GA_Problem_Parameter.DEP, DEP_scheduling);
		ArrayList<Integer> indices=new ArrayList<Integer>();
		
		//shuffling the bug list to provide randomness in the scheduling
		GA_Problem_Parameter.shuffledTasks=(ArrayList<Bug>) GA_Problem_Parameter.tasks.clone();
		Collections.shuffle(GA_Problem_Parameter.shuffledTasks);
		for(int i=0;i<GA_Problem_Parameter.shuffledTasks.size()-1;i++){
			indexes=getIndex(i);
			m=indexes[0];
			n=indexes[1];
			for(int j=i+1;j<GA_Problem_Parameter.shuffledTasks.size();j++){
				indexes=getIndex(j);
				p=indexes[0];
				q=indexes[1];
				if(compareSubtasksAssignee(m,n,p,q,assignment)){
					try{
						if(paths.getAllPaths(varToBug.get(i),varToBug.get(j), true, 1000).isEmpty() && paths.getAllPaths(varToBug.get(j), varToBug.get(i), true, 1000).isEmpty()){
							int t=-1;
							indices.clear();
							indices.add(i);
							indices.add(j);
							try{
								//get the information for potential link-- index t determine if the edge already exists there
								t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(i), varToBug.get(j)));
								if(t<0){
									t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(j), varToBug.get(i)));
								}
								boolean b=new CycleDetector<Bug, DefaultEdge>(DEP_scheduling).detectCycles();
								// in case a link exits we add that link to DEP_scheduling (the graph of a specific schedule)
								DEP_scheduling.addEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
										GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
							}
							catch(Exception ex)
							{
								
							} 
							//check if added link does not result in a cycle otherwise the added link gets removed
							boolean b=new CycleDetector<Bug, DefaultEdge>(DEP_scheduling).detectCycles();
							if(!b)
								schedule.set(t, 1);
							else
								DEP_scheduling.removeEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
										GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
						}
					}
					catch (IllegalArgumentException e) {
						// TODO: handle exception
					}
				}
			}
		}
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
	
	public void generateDAG(){
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
	}
}
