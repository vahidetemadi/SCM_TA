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

public class CompetenceMulti2_problem extends AbstractProblem {
	
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	public CompetenceMulti2_problem(){
		super(GA_Problem_Parameter.setNum_of_Variables(bugs),GA_Problem_Parameter.Num_of_functions_Multi);
	}
	
	
	public void init(){
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);
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
		double f1 = 0.0;
		double f2 = 0.0;
		Bug b;
		int numOfVar=0;
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		System.out.println(DEP_evaluation.edgeSet().size());
		TopologicalOrderIterator<Bug, DefaultEdge> tso_evaluate=GA_Problem_Parameter.getTopologicalSorted(DEP_evaluation);
		//reset all the associate time for the bugs and their zones
		GA_Problem_Parameter.resetParameters(DEP_evaluation,solution);
		//assign Devs to zone
		GA_Problem_Parameter.assignZoneDev(GA_Problem_Parameter.getTopologicalSorted(DEP_evaluation), solution);
		
		while(tso_evaluate.hasNext()) {
			 b=tso_evaluate.next();
			 for(Zone zone_bug:b.Zone_DEP){
				double compeletionTime=0.0;
				Entry<Zone, Double> zone=new AbstractMap.SimpleEntry<Zone, Double>(zone_bug,b.BZone_Coefficient.get(zone_bug));
				compeletionTime=fitnessCalc.compeletionTime(b,zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				f1+=compeletionTime;
				//compute the cost
				f2+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).getDZone_Wage().get(zone.getKey());
				numOfVar++;
				//update developer nextAvailableHours
				developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).developerNextAvailableHour+=fitnessCalc.getDelayTime(b, zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				//update bug endTime
				b.endTime=Math.max(b.endTime, b.endTime+compeletionTime);
			 }
		}
		
	/*	
		System.out.println(developers.keySet());
		for (int i = 0; i < GA_Problem_Parameter.Num_of_Bugs; i++) {
			 for(Map.Entry<Zone, Double>  zone:bugs[i].BZone_Coefficient.entrySet()){
				f1+=fitnessCalc.compeletionTime(bugs[i],zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				numOfVar++;
			 }

			bugs[i].endTime=f1+bugs[i].startTime;
		 }
		
		numOfVar=0;
		 for (int i = 0; i < GA_Problem_Parameter.Num_of_Bugs; i++) {
			 for(Map.Entry<Zone, Double>  zone:bugs[i].BZone_Coefficient.entrySet()){
				 	f2+=fitnessCalc.compeletionTime(bugs[i],zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))))
							*developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).getDZone_Wage().get(zone.getKey());
					numOfVar++;
			 }
		 }
	*/	
		System.out.println("f1: "+f1+"--- f2: "+f2);
		solution.setObjective(0, f1);
		solution.setObjective(1, f2);
		 }
		
	
}

