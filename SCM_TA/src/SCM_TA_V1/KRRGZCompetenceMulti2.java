package SCM_TA_V1;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Iterator;

import javax.xml.stream.events.StartDocument;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.graph.AsSubgraph;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

public class KRRGZCompetenceMulti2 extends AbstractProblem {
	
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	List<Bug> bestSort = new ArrayList<Bug>();
	ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee=new ArrayList<Triplet<Bug,Zone,Integer>>();
	Random r = new Random();
	public KRRGZCompetenceMulti2(){
		super(GA_Problem_Parameter.setNum_of_Variables(GA_Problem_Parameter.bugs),GA_Problem_Parameter.Num_of_functions_Multi);
	}
	
	
	public void init() throws CloneNotSupportedException{
		bugs=GA_Problem_Parameter.bugs;
		DEP=GA_Problem_Parameter.DEP;
		GA_Problem_Parameter g=new GA_Problem_Parameter();
		tso= GA_Problem_Parameter.tso_competenceMulti2;
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
		tso=null;
	}
	
	@Override
	public Solution newSolution(){
		if(GA_Problem_Parameter.flag==1){
			try{
				init();
			}
			catch(CloneNotSupportedException e){
				e.printStackTrace();
			}
			GA_Problem_Parameter.flag=0;
		}
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions_Multi);
		int rand=r.nextInt(GA_Problem_Parameter.listOfdevs.length);
		int var=GA_Problem_Parameter.listOfdevs[rand];
		int j=0;
		for(Zone z:genes){
			//RealVariable r=new RealVariable(GA_Problem_Parameter.getMinIdofDeveloper(), GA_Problem_Parameter.getMaxIdofDeveloper());
			//r.randomize();
			solution.setVariable(j,EncodingUtils.newInt(var, var));
			j++;
		}
		return solution;
	}
		
	
	@Override 	
	public void evaluate(Solution solution){
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation = (DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		
		//belongs to the second list
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation_for_inCommon = (DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		ArrayList<Bug> list_for_incommon = new ArrayList<Bug>();
		HashMap<Integer, int[]> listOfBugAssignee=new HashMap<Integer, int[]>();   /* keep the list of assignees for a given bug*/
		int numOfDevs=GA_Problem_Parameter.numOfDevs;
		
		
		//List<AsSubgraph<Bug, DefaultEdge>> sub = DEP_evaluation.
		//reset all the associate time for the bugs and their zones
		GA_Problem_Parameter.resetParameters(DEP_evaluation, solution, developers);
		//assign associate Dev to zone
		zoneAssignee.clear();
		GA_Problem_Parameter.assignZoneDev(zoneAssignee, GA_Problem_Parameter.tasks, solution);
		
		//start checking dev in common
		for (Bug b_temp : DEP_evaluation_for_inCommon.vertexSet()) {
			int[] tempListToKeep=new int[b_temp.Zone_DEP.vertexSet().size()];
			int i=0;
			b_temp.flag_devInCommon = 0;
			for(Triplet<Bug, Zone, Integer> item : zoneAssignee){
				if(item.getFirst().ID == b_temp.ID){
					tempListToKeep[i] = item.getThird();
					i++;
				}
			}
			listOfBugAssignee.put(b_temp.ID, tempListToKeep );
		}
		
		
		for (Bug b_temp : DEP_evaluation_for_inCommon.vertexSet()) {
			if (DEP_evaluation_for_inCommon.incomingEdgesOf(b_temp).size() == 0) {
				list_for_incommon.add(b_temp);
			}
		}
		
		for (int m = 0; m < list_for_incommon.size() - 1; m++) {
			for (int n = m + 1; n < list_for_incommon.size(); n++) {
				int devInCommon = getNumOfDeveloperInCommon_enhanced(listOfBugAssignee.get(list_for_incommon.get(m).getID()),
						listOfBugAssignee.get(list_for_incommon.get(n).getID()), numOfDevs);
				if (devInCommon > 1) {
					list_for_incommon.get(m).flag_devInCommon = 1;
					list_for_incommon.get(n).flag_devInCommon = 1;
				}
			}
		}
		
		//end dev in common
		
		//given the number of vertices, N, iterate over N * (N - 1) permutations of the bugs
		double totalMinCost = Double.MAX_VALUE;
		double totalMinTime = Double.MAX_VALUE;
		int numOfTasks = DEP_evaluation.vertexSet().size();
		int numOfPermutations = GA_Problem_Parameter.numOfEvalNSGAIIGLS; 
		for (int c = 0 ; c < numOfPermutations ; c++) {
			GA_Problem_Parameter.resetParameters(DEP_evaluation, solution, developers);
			TopologicalOrderIterator<Bug, DefaultEdge> tso = new TopologicalOrderIterator<Bug, DefaultEdge>(DEP_evaluation, null, "Bug");
			double totalTime = 0.0;
			double totalCost = 0.0;
			double totalDevCost = 0.0;
			double totalDelayTime = 0.0;
			double totalDelayCost = 0.0;
			double totalStartTime = 0.0;
			double totalEndTime = 0.0;
			double totalExecutionTime = 0.0;
			int index = 0;
			bestSort.clear();
			while(tso.hasNext()){
				Bug b=tso.next();
				bestSort.add(b);
				//set Bug startTime
				b.startTime_evaluate = fitnessCalc.getMaxEndTimes(b, DEP_evaluation);
				//reset zones' stats
				GA_Problem_Parameter.resetParameters_ZoneAndDevelopers(b, solution, developers);
				
				TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone = new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
				//iterate by the order provided in y graph
				while(tso_Zone.hasNext()){
					Zone zone = tso_Zone.next();
					double compeletionTime = 0.0;
					Entry<Zone, Double> zone_bug = new AbstractMap.SimpleEntry<Zone, Double>(zone, b.BZone_Coefficient.get(zone));
					compeletionTime = fitnessCalc.compeletionTime(b, zone_bug, developers.get(EncodingUtils.getInt(solution.getVariable(index))));
					totalExecutionTime += compeletionTime;
					//need to be changed????///
					totalDevCost += compeletionTime * developers.get(EncodingUtils.getInt(solution.getVariable(index))).hourlyWage;
					zone.zoneStartTime_evaluate = b.startTime_evaluate + fitnessCalc.getZoneStartTime(developers.get(EncodingUtils.getInt(solution.getVariable(index))), zone.DZ);
					zone.zoneEndTime_evaluate = zone.zoneStartTime_evaluate + compeletionTime;
					/*developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour=Math.max(developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour,
					zone.zoneStartTime_evaluate)+compeletionTime;*/
					developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour = zone.zoneEndTime_evaluate;
					b.endTime_evaluate = Math.max(b.endTime_evaluate, zone.zoneEndTime_evaluate);
					index++;
				}
				totalStartTime = Math.min(totalStartTime, b.startTime_evaluate);
				totalEndTime = Math.max(totalEndTime, b.endTime_evaluate);
				//pay for those 
				totalDelayTime += b.endTime_evaluate - (2.5 * totalExecutionTime + totalExecutionTime);
				if(totalDelayTime > 0)
					totalDelayCost += totalDelayTime * GA_Problem_Parameter.priorities.get(b.priority);
			}
			//end=System.currentTimeMillis()-start;
			totalTime = totalEndTime - totalStartTime;
			totalCost = totalDevCost;//+totalDelayCost;
			
			//set soultion objective values
			if (totalTime < totalMinTime && totalCost < totalMinCost) {
				solution.setObjective(0, totalTime);
				solution.setObjective(1, totalCost);
				solution.setBestSort(bestSort);
			}
		}	
		if(GA_Problem_Parameter.algorithm.getNumberOfEvaluations() % 50000 == 0)
			System.out.println("KRRGZ: " + GA_Problem_Parameter.algorithm.getNumberOfEvaluations());
	}
	
	public int getNumOfDeveloperInCommon_enhanced(int[] b1, int[] b2, int numOfDevs){
		int numOfDevsInCommon = 0;
		int[] devs = new int[numOfDevs];
		
		for(int i = 0; i < b1.length; i++)
			devs[b1[i]-1]++;
		
		for(int i = 0; i < b2.length; i++)
			if(devs[b2[i]-1] > 0){
				numOfDevsInCommon++;
				devs[b2[i]-1]--;
			}
		
		return numOfDevsInCommon;
	}
		
	
}

