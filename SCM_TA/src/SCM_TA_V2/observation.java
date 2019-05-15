package SCM_TA_V2;

import org.apache.commons.math3.distribution.*;
public class observation {
	private int teamChangeRate;
	
	public observation(double teamChangeRate){
		if(teamChangeRate>0.6)
			this.teamChangeRate=1;
		else 
			this.teamChangeRate=0;
	}
	
	public double getTeamChangeRate() {
		return this.teamChangeRate;
	}
	
	public void setTeamChangeRate(int teamChangeRate) {
		this.teamChangeRate = teamChangeRate;
	}
}
