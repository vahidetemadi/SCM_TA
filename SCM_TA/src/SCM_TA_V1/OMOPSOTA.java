package SCM_TA_V1;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.events.StartDocument;

import org.jgraph.JGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

public class OMOPSOTA extends AbstractProblem {
	
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee=new ArrayList<Triplet<Bug,Zone,Integer>>();
	
	public OMOPSOTA(){
		super(GA_Problem_Parameter.setNum_of_Variables(GA_Problem_Parameter.bugs),GA_Problem_Parameter.Num_of_functions_Multi);
	}
	
	
	public void init(){
		bugs=GA_Problem_Parameter.bugs;
		int ttt=GA_Problem_Parameter.setNum_of_Variables(bugs);
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso_ID;
		/*
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);*/
		int index=0;
		while(tso.hasNext()){
			Bug b=tso.next();
			b.setZoneDEP();
			TopologicalOrderIterator<Zone,DefaultEdge> tso_zones=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_zones.hasNext()){
				genes.add(tso_zones.next());
				index++;
			}
		}
		int ffff=0;
	}
	
	
	@Override
	public Solution newSolution(){
		if(GA_Problem_Parameter.flag==1){
			init();
			GA_Problem_Parameter.flag=0;
		}
		
		//changed NUM of variables for the solution
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions_Multi);
		int min=GA_Problem_Parameter.getMinIdofDeveloper();
		int max=GA_Problem_Parameter.getMaxIdofDeveloper();
		int j=0;
		for(Zone z:genes){
			RealVariable r=new RealVariable(GA_Problem_Parameter.getMinIdofDeveloper(), GA_Problem_Parameter.getMaxIdofDeveloper());
			r.randomize();
			solution.setVariable(j,r);
			j++;
		}
		return solution;
	}

	
	@Override 	
	public void evaluate(Solution solution){
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		//reset all the associate time for the bugs and their zones
		//assign associate Dev to zone
		zoneAssignee.clear();
		GA_Problem_Parameter.assignZoneDev(zoneAssignee,GA_Problem_Parameter.tasks, solution );
		
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
			b.startTime_evaluate=fitnessCalc.getMaxEndTimes(b, DEP_evaluation);
			TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			//iterate by the order provided in y graph
			while(tso_Zone.hasNext()){
				Zone zone=tso_Zone.next();
				double compeletionTime=0.0;
				Entry<Zone, Double> zone_bug=new AbstractMap.SimpleEntry<Zone, Double>(zone,b.BZone_Coefficient.get(zone));
				compeletionTime=fitnessCalc.compeletionTime(b,zone_bug, developers.get(EncodingUtils.getInt(solution.getVariable(index))));
				totalExecutionTime+=compeletionTime;
				//need to be changed????///
				totalDevCost+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(index))).hourlyWage;
				zone.zoneStartTime_evaluate=b.startTime_evaluate+fitnessCalc.getZoneStartTime(developers.get(EncodingUtils.getInt(solution.getVariable(index))), zone.DZ);
				zone.zoneEndTime_evaluate=zone.zoneStartTime_evaluate+compeletionTime;
				developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour=Math.max(developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour,
						zone.zoneEndTime_evaluate);
				b.endTime_evaluate=Math.max(b.endTime_evaluate, zone.zoneEndTime_evaluate);
				index++;
			}
			totalStartTime=Math.min(totalStartTime, b.startTime_evaluate);
			totalEndTime=Math.max(totalEndTime, b.endTime_evaluate);
			//pay for those 
			totalDelayTime+=b.endTime_evaluate-(2.5*totalExecutionTime+totalExecutionTime);
			if(totalDelayTime>0)
				totalDelayCost+=totalDelayTime*GA_Problem_Parameter.priorities.get(b.priority);
		}
		//end=System.currentTimeMillis()-start;
		totalTime=totalEndTime-totalStartTime;
		totalCost=totalDevCost+totalDelayCost;
		
		solution.setObjective(0, totalTime);
		solution.setObjective(1, totalCost);
	}
	
}

