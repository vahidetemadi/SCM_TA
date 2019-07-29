package SCM_TA_V1;

import java.io.Serializable;
import java.util.HashMap;

public class Developer implements Serializable
{
	private int competenceProfileCount;
	private int ID;
	private double competenceProfile[];
	public double developerNextAvailableHour=0.0;
	private int totalAssignedBugs;
	public double hourlyWage;
	public DevMetrics devMetrics=new DevMetrics();
	public HashMap<Zone, Double> DZone_Wage=new HashMap<Zone,Double>();
	public HashMap<Zone, Double> DZone_Coefficient=new HashMap<Zone,Double>();
	public int getTotalAssignedBugs() {
		return totalAssignedBugs;
	}

	public void setTotalAssignedBugs(int totalAssignedBugs) 
	{
		this.totalAssignedBugs = totalAssignedBugs;
	}

	public double getDeveloperNextAvailableHour() {
		return this.developerNextAvailableHour;
	}

	public void setDeveloperNextAvailableHour(int developerNextAvailableHour) {
		this.developerNextAvailableHour = developerNextAvailableHour;
	}

	public Developer(int competenceCount)
	{
		this.competenceProfileCount=competenceCount;
		competenceProfile=new double[competenceCount];
	}
	
	public int getCompetenceProfileCount() {
		return competenceProfileCount;
	}
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	
	public double[] getCompetenceProfile() {
		return competenceProfile;
	}
	public void setCompetenceProfile(double[] competenceProfile) {
		this.competenceProfile = competenceProfile;
	}
	
	public HashMap< Zone, Double> getDZone_Coefficient(){
		return DZone_Coefficient;
	}
	
	public HashMap< Zone, Double> getDZone_Wage(){
		return DZone_Wage;
	}
	
}
