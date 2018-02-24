package SCM_TA_V1;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

public class InformationDifussion extends AbstractProblem{
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	public InformationDifussion(){
		super(GA_Problem_Parameter.setNum_of_Variables(bugs),GA_Problem_Parameter.Num_of_functions_Multi);
		//this.bugs=bugs;
		//this.developers= new ArrayList<Developer>(Arrays.asList(developers));
	}
	
	
	@Override
	public Solution newSolution(){
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);
		int j=0;
		while(tso.hasNext()){
			Bug b=tso.next();
			b.setZoneDEP();
			TopologicalOrderIterator<Zone,DefaultEdge> tso_zones=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_zones.hasNext()){
				genes.add(tso_zones.next());
			}
		}
		//changed NUM of variables for the solution
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions_Multi);
		for(Zone z:genes){
			int randDevId=GA_Problem_Parameter.getRandomDevId();
			solution.setVariable(j,EncodingUtils.newInt(randDevId, randDevId));
		}

		//generate all the candidate schdeuling
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation_scheduling=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		System.out.println(DEP_evaluation_scheduling.hashCode());
		ArrayList<ArrayList<DefaultEdge>> validSchedulings = GA_Problem_Parameter.getValidSchedulings(DEP_evaluation_scheduling);
		GA_Problem_Parameter.setCandidateSchedulings(GA_Problem_Parameter.getReScheduledGraphs(DEP,validSchedulings));
		System.out.println("passed");
		return solution;
	}
		
	
	@Override 	
	public void evaluate(Solution solution){
		System.out.println("gives error");
		double f_totalTime = 0.0;
		double f_totalCost=0.0;
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		TopologicalOrderIterator<Bug, DefaultEdge> tso_evaluate=GA_Problem_Parameter.getTopologicalSorted(DEP_evaluation);
		//reset all the associate time for the bugs and their zones
		GA_Problem_Parameter.resetParameters(DEP_evaluation,solution);
		//assign Devs to zone
		GA_Problem_Parameter.assignZoneDev(tso_evaluate, solution);
		//evaluate and examine for all the candidate schdulings and then, pick the minimum one 
		for(TopologicalOrderIterator<Bug, DefaultEdge> tso_evaluate_scheduling:GA_Problem_Parameter.candidateSchedulings){
			double f_devCost=0.0;
			double f_delayCost=0.0;
			double f_Time=0.0;		
			int numOfVar=0; 
			Bug b;
			while(tso_evaluate_scheduling.hasNext()) {
			 b=tso_evaluate_scheduling.next();
				 for(Zone zone_bug:b.Zone_DEP){
						double delayTime=0.0;
						double compeletionTime=0.0;
						Entry<Zone, Double> zone=new AbstractMap.SimpleEntry<Zone, Double>(zone_bug,b.BZone_Coefficient.get(zone_bug));
						compeletionTime=fitnessCalc.compeletionTime(b,zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
						f_devCost+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).getDZone_Wage().get(zone.getKey());
						numOfVar++;
						delayTime=fitnessCalc.getDelayTime(b, zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
						f_delayCost+=delayTime*GA_Problem_Parameter.delayPenaltyCostRate;		
						f_Time+=compeletionTime+delayTime;
						//update developer nextAvailableHours
						developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).developerNextAvailableHour+=fitnessCalc.getDelayTime(b, zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
						//update bug endTime
						b.endTime=Math.max(b.endTime, delayTime+compeletionTime);		
				 }  
		 }
			 f_totalCost=f_delayCost+f_devCost;
			 f_totalTime=f_Time;
			 if(solution.getObjectives()[0]!=0){
				 solution.setObjective(0, Math.min(f_totalTime,solution.getObjectives()[0]));
				 //assigning the best schedule for the solution 
				 GA_Problem_Parameter.selectedSchedules.put(solution.getNumberOfVariables(),tso_evaluate_scheduling);
			 }
			 if(solution.getObjectives()[1]!=0){
				 solution.setObjective(0, Math.min(f_totalCost,solution.getObjectives()[1]));
				 //assigning the best schedule for the solution 
				 GA_Problem_Parameter.selectedSchedules.put(solution.getNumberOfVariables(),tso_evaluate_scheduling);
			 }
		}
		
		
		//compute the ID for candidate solution
		/*
		
		
		//compute the information diffusion
		numOfVar=0;
		 ArrayList<Developer> devs=new ArrayList<Developer>();
		 for (int i = 0; i < GA_Problem_Parameter.Num_of_Bugs; i++) {
			 for(Entry<Zone, Double>  zone:bugs[i].BZone_Coefficient.entrySet()){
				 //f2_2 +=fitnessCalc.getSimBug( developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))), bugs[i],zone.getKey());
				 devs.add(developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				 numOfVar++;
			 }
			 f2_1+=fitnessCalc.getDataFlow(bugs[i], devs);
		 }
		
		
		
		//compute team similarity
		 numOfVar=0;
		 devs.clear();
		 for (int i = 0; i < GA_Problem_Parameter.Num_of_Bugs; i++) {
			 for(Entry<Zone, Double>  zone:bugs[i].BZone_Coefficient.entrySet()){
				 //f2_2 +=fitnessCalc.getSimBug( developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))), bugs[i],zone.getKey());
				 devs.add(developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				 numOfVar++;
			 }
			 f2_2+=fitnessCalc.getTZoneSim(bugs[i].BZone_Coefficient, devs);
		 }
		 f2=f2_1+f2_2;
		 
		 
		 */
		
		//olution.setObjective(1, f2);
		
		 }

	public void generateDAG(){
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
	}
}
