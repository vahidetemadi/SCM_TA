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
	
	
	public void init(){
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso;
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
	
	@Override
	public Solution newSolution(){
		init();
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
		double f_totalTime = 0.0;
		double f_totalCost=0.0;
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		TopologicalOrderIterator<Bug, DefaultEdge> tso_evaluate=GA_Problem_Parameter.getTopologicalSorted(DEP_evaluation);
		//reset all the associate time for the bugs and their zones
		//assign Devs to zone
		GA_Problem_Parameter.assignZoneDev(GA_Problem_Parameter.getTopologicalSorted(DEP_evaluation), solution);
		//evaluate and examine for all the candidate schdulings and then, pick the minimum one 
		for(ArrayList<Bug> valid_scheduling:GA_Problem_Parameter.candidateSchedulings){
			GA_Problem_Parameter.resetParameters(DEP_evaluation,solution, developers);
			double f_devCost=0.0;
			double f_delayCost=0.0;
			double f_Time=0.0;		
			int numOfVar=0;
			for(Bug b:valid_scheduling) {
				 double endTime_bug=0.0;
				 for(Zone zone_bug:b.BZone_Coefficient.keySet()){
						double compeletionTime=0.0;
						double delayTime=0.0;
						Entry<Zone, Double> zone=new AbstractMap.SimpleEntry<Zone, Double>(zone_bug,b.BZone_Coefficient.get(zone_bug));
						compeletionTime=fitnessCalc.compeletionTime(b,zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
						f_devCost+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).hourlyWage;
						delayTime=fitnessCalc.getDelayTime(b, zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
						f_delayCost+=delayTime*GA_Problem_Parameter.delayPenaltyCostRate;f_Time+=compeletionTime+delayTime;
						endTime_bug=Math.max(endTime_bug, delayTime+compeletionTime);
						//update developer nextAvailableHours
						developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).developerNextAvailableHour+=(delayTime+ compeletionTime);
						if(Double.isInfinite(developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).developerNextAvailableHour)){
							int t=0;
							System.out.println(developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).getID());
							delayTime=fitnessCalc.getDelayTime(b, zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
							//System.out.println("devID: "+developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).getID());
						}
						
						//update bug endTime
						if(Double.isInfinite(delayTime+compeletionTime)){
							System.out.println(b.ID);
						}
						numOfVar++;
				 }  

					b.endTime=endTime_bug;
					f_Time+=endTime_bug;
			}
			f_totalCost=f_delayCost+f_devCost;
			f_totalTime=f_Time;
			if(solution.getObjectives()[0]!=0){
				solution.setObjective(0, Math.min(f_totalTime,solution.getObjectives()[0]));
				//assigning the best schedule for the solution 
				GA_Problem_Parameter.selectedSchedules.put(solution.getNumberOfVariables(),valid_scheduling);
			}
			else{
				solution.setObjective(0,f_totalTime);
			}
			if(solution.getObjectives()[1]!=0){
				 solution.setObjective(1, Math.min(f_totalCost,solution.getObjectives()[1]));
				 //assigning the best schedule for the solution 
				 GA_Problem_Parameter.selectedSchedules.put(solution.getNumberOfVariables(),valid_scheduling);
			 }
			else {
				solution.setObjective(1, f_totalCost);
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
