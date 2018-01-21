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
	Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	public InformationDifussion(){
		super(GA_Problem_Parameter.Num_of_variables,GA_Problem_Parameter.Num_of_functions);
		//this.bugs=bugs;
		//this.developers= new ArrayList<Developer>(Arrays.asList(developers));
	}
	
	
	@Override
	public Solution newSolution(){
		/*
		 		
		int j=0;
		//change the code
		for( int i=0;i<GA_Problem_Parameter.Num_of_variables;i++){
				int randDevId=GA_Problem_Parameter.getRandomDevId();
				
				solution.setVariable(j,EncodingUtils.newInt(randDevId, randDevId));
				
				j++;
			}
		
		*/
		
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
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
		//chnged num of variables for the soultuion
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions);
		for(Zone z:genes){
			int randDevId=GA_Problem_Parameter.getRandomDevId();
			solution.setVariable(j,EncodingUtils.newInt(randDevId, randDevId));
		}
		return solution;
	}
		
	
	@Override 	
	public void evaluate(Solution solution){
		double[] x = EncodingUtils.getReal(solution);
		double f1 = 0.0;
		double f1_1=0.0;
		double f1_2=0.0;
		double f2=0.0;
		double f2_1 = 0.0;
		double f2_2 = 0.0;
		
		int numOfVar=0; 
		Bug b;
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		TopologicalOrderIterator<Bug, DefaultEdge> tso_evaluate=GA_Problem_Parameter.getTopologicalSorted(DEP_evaluation);
		while(tso_evaluate.hasNext()) {
			 b=tso_evaluate.next();
			 for(Zone  zone_bug:b.Zone_DEP){
				double delayTime=0.0;
				double compeletionTime=0.0;
				Entry<Zone, Double> zone=new AbstractMap.SimpleEntry<Zone, Double>(zone_bug,b.BZone_Coefficient.get(zone_bug));
				compeletionTime=fitnessCalc.compeletionTime(b,zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				f1_1+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).getDZone_Wage().get(zone.getKey());
						numOfVar++;
						delayTime=fitnessCalc.getDelayTime(b, zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				f1_2+=delayTime*GA_Problem_Parameter.delayPenaltyCostRate;		
				
				//update developer nextAvailableHours
				developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))).developerNextAvailableHour+=fitnessCalc.getDelayTime(b, zone, developers.get(EncodingUtils.getInt(solution.getVariable(numOfVar))));
				b.endTime=Math.max(b.endTime, delayTime+compeletionTime);
			 }
		 }
			 f1=f1_1+f1_2;
		
		
		//compute the infomration difuusion
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
		 
		solution.setObjective(0, f1);
		solution.setObjective(1, f2);
		
		 }

	public void generateDAG(){
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
	}
}
