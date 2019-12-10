package SCM_TA_V2;

import org.apache.commons.math3.distribution.*;
public class Observation {
	private int teamChangeRate;
	private int symbol;
	public Observation(double teamChangeRate){
		if(teamChangeRate>0.6)
			this.teamChangeRate=1;
		else 
			this.teamChangeRate=0;
	}
	
	public int getTeamChangeRate() {
		return this.teamChangeRate;
	}
	
	public void setTeamChangeRate(int teamChangeRate) {
		this.teamChangeRate = teamChangeRate;
	}
}
