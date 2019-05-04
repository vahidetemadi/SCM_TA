package SCM_TA_V2;

import org.apache.commons.math3.distribution.*;
public class observation {
	private double SLA_violation;
	private int teamChangeRate;
	private double teamUtilizationRate_avg;
	private double teamUtilizartionRate_var;
	
	public double getSLA_violation() {
		return SLA_violation;
	}
	public void setSLA_violation(double sLA_violation) {
		SLA_violation = sLA_violation;
	}
	public int getTeamChangeRate() {
		return teamChangeRate;
	}
	public void setTeamChangeRate(int teamChangeRate) {
		this.teamChangeRate = teamChangeRate;
	}
	public double getTeamUtilizationRate_avg() {
		return teamUtilizationRate_avg;
	}
	public void setTeamUtilizationRate_avg(double teamUtilization_avg) {
		this.teamUtilizationRate_avg = teamUtilization_avg;
	}
	public double getTeamUtilizationRate_var() {
		return teamUtilizartionRate_var;
	}
	public void setTeamUtilizationRate_var(double teamUtilizartion_var) {
		this.teamUtilizartionRate_var = teamUtilizartion_var;
	}
	
	public static int[] get_the_observation(observation o){
		int[] instance_of_observation=new int[8];
		//assign the sla
		if(o.getSLA_violation()>0.6)
		{
			instance_of_observation[0]=1;
			instance_of_observation[1]=0;
		}
		else {
			instance_of_observation[0]=0;
			instance_of_observation[1]=1;
		}
		
		//assign team change rate
		if(o.getTeamChangeRate()>0.6)
		{
			instance_of_observation[2]=1;
			instance_of_observation[3]=0;
		}
		else {
			instance_of_observation[2]=0;
			instance_of_observation[3]=1;
		}
		
		//assign team utilization rate
		if(o.getTeamUtilizationRate_avg()>0.6)
		{
			instance_of_observation[4]=1;
			instance_of_observation[5]=0;
		}
		else {
			instance_of_observation[4]=0;
			instance_of_observation[5]=1;
		}
		
		//assign team utilization rate var
		if(o.getTeamUtilizationRate_var()>0.25)
		{
			instance_of_observation[6]=1;
			instance_of_observation[7]=0;
		}
		else {
			instance_of_observation[6]=0;
			instance_of_observation[7]=1;
		}
		return instance_of_observation;
	}
	
	

}
