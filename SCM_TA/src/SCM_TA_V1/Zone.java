package SCM_TA_V1;

import java.io.Serializable;
import java.util.ArrayList;

public class Zone implements Cloneable, Serializable{
	public int zId;
	public String zName;
	public ArrayList<Zone> DZ=new ArrayList<Zone>();
	public double zoneEndTime=0.0;
	public double zoneStartTime=0.0;
	public double zoneEndTime_evaluate=0.0;
	public double zoneStartTime_evaluate=0.0;
	public int assignedDevID=0;
	public Zone(int id, String name){
		this.zId=id;
		this.zName=name;
	}		
	public ArrayList<Zone> getDZ(){
		return DZ;
	}
	protected Object clone() throws CloneNotSupportedException{
		
		return super.clone();
		
	}
}
