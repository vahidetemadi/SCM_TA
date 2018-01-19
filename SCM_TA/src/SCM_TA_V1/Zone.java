package SCM_TA_V1;

import java.util.ArrayList;

public class Zone {
	public int zId;
	public String zName;
	public ArrayList<Zone> DZ=new ArrayList<Zone>();
	public double zoneEndTime=0;
	public double zoneStartTime=0;
	public Zone(int id, String name){
		this.zId=id;
		this.zName=name;
	}		
	public ArrayList<Zone> getDZ(){
		return DZ;
	}

}
