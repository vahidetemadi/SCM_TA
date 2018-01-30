package SCM_TA_V1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class Bug 
{
	public ArrayList<Bug> DB=new ArrayList<Bug>();
	int competenceProfileCount;
	int ID;
	double competenceProfile[];
	int manualDeveloperID;
	double totalEstimatedEffort;
	double arrivalTime;
	HashMap<Zone, Double> BZone_Coefficient=new HashMap<Zone,Double>();
	DirectedAcyclicGraph<Zone, DefaultEdge> Zone_DEP; 
	double startTime;
	double startTime_evaluate;
	double endTime;
	double endTime_evaluate;
	int algorithmicDeveloperAssignmentID=0;
	public int getAlgorithmicDeveloperAssignmentID() {
		return algorithmicDeveloperAssignmentID;
	}

	public void setAlgorithmicDeveloperAssignmentID(
			int algorithmicDeveloperAssignmentID) {
		this.algorithmicDeveloperAssignmentID = algorithmicDeveloperAssignmentID;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public Bug(int competenceCount)
	{
		this.competenceProfileCount=competenceCount;
		competenceProfile=new double[competenceCount];
	}
	
	public int getCompetenceProfileCount() {
		return competenceProfileCount;
	}
	public void setCompetenceProfileCount(int competenceProfileCount) {
		this.competenceProfileCount = competenceProfileCount;
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
	
	public int getManualDeveloperID() {
		return manualDeveloperID;
	}

	public void setManualDeveloperID(int manualDeveloperID) {
		this.manualDeveloperID = manualDeveloperID;
	}
	
	public double getTotalEstimatedEffort() {
		return totalEstimatedEffort;
	}

	public void setTotalEstimatedEffort(double totalEstimatedEffort) {
		this.totalEstimatedEffort = totalEstimatedEffort;
	}
	public void setZoneDEP(){
		Class<? extends DefaultEdge> eclass=null;
		Zone_DEP=new DirectedAcyclicGraph<Zone, DefaultEdge>(eclass);
		for (Entry<Zone, Double>  zone:BZone_Coefficient.entrySet())
		{
			if(zone.getKey().DZ.size()>0){
				for(Zone z:zone.getKey().DZ){
					if(!Zone_DEP.containsEdge(z,zone.getKey()))
						Zone_DEP.addEdge(z, zone.getKey());
				}
			}
			else
				Zone_DEP.addVertex(zone.getKey());
		}
	}
}
