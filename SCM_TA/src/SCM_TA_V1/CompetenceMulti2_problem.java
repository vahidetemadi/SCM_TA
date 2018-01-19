package SCM_TA_V1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.Soundbank;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

public class CompetenceMulti2_problem extends AbstractProblem {
	
	Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	public CompetenceMulti2_problem(){
		super(GA_Problem_Parameter.Num_of_variables,GA_Problem_Parameter.Num_of_functions);
		//System.out.println(GA_Problem_Parameter.Num_of_variables);
		//System.out.println(bugs.length+"-----"+developers.size()+"----"+GA_Problem_Parameter.Num_of_functions);
	}
	
	
	@Override
	public Solution newSolution(){
		Solution solution=new Solution(GA_Problem_Parameter.Num_of_variables,GA_Problem_Parameter.Num_of_functions);
			int j=0;
		for( int i=0;i<GA_Problem_Parameter.Num_of_variables;i++){
				int randDevId=GA_Problem_Parameter.getRandomDevId();
				solution.setVariable(i,EncodingUtils.newInt(randDevId,randDevId));
				j++;
				//System.out.println(GA_Problem_Parameter.startDevId+"----------"+EncodingUtils.getInt(solution.getVariable(i)));
			
			}
		return solution;
	}
		
	
	@Override 	
	public void evaluate(Solution solution){
		double f1 = 0.0;
		double f2 = 0.0;
		int numOfVar=0;
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
		
		solution.setObjective(0, f1);
		solution.setObjective(1, f2);
		 }
		
	
}

