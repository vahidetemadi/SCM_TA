package SCM_TA_V1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class fitnessCalc {


public static double compeletionTime(Bug bug, Entry<Zone, Double> zone, Developer developer) {	
	//compute total time for competency 
	double tct=bug.getTotalEstimatedEffort()*bug.BZone_Coefficient.get(zone.getKey())/((developer.getDZone_Coefficient().get(zone.getKey()))+1);
	return tct;
}



public static double getDelayTime(Bug bug, Entry<Zone, Double> zone,
		Developer developer){
	double delayTime=Math.max(taskDependencyDelayTime(bug, zone, developer), developer.developerNextAvailableHour);
	return delayTime;
}



public static double taskDependencyDelayTime(Bug bug, Entry<Zone, Double> zone,
Developer developer){
	for(Zone z:zone.getKey().DZ){
		zone.getKey().zoneStartTime=Math.max(zone.getKey().zoneStartTime,z.zoneEndTime);
	}
	
	for(int j=0;j<bug.DB.size();j++){
			bug.startTime=Math.max(bug.startTime, bug.DB.get(j).endTime);
	}
	
	return zone.getKey().zoneStartTime+ bug.startTime;
}




 public static double getSimDev(Developer d1, Developer d2){
	 double DDSim_intersection=0.0;
	 double DDSim_union=0.0;
	 for (Entry<Zone, Double>  zone:d1.DZone_Coefficient.entrySet()){
		 DDSim_intersection+=Math.min(Double.parseDouble(zone.getValue().toString()),d2.getDZone_Coefficient().get(zone.getKey()));
		 DDSim_union+=Math.min(Double.parseDouble(zone.getValue().toString()),d2.getDZone_Coefficient().get(zone.getKey()));
	 }
		 
	 return 1/(DDSim_intersection/DDSim_union);
 }
 
 
 
 public static double getSimBug(Developer d1,Bug b2, Zone z1){
	 double DBSim=0.0;
	 //for (Entry<Zone, Double>  zone:b2.BZone_Coefficient.entrySet())
	 DBSim+=Math.min(b2.BZone_Coefficient.get(z1),d1.DZone_Coefficient.get(z1));

	 return DBSim;
 }

 
 
 public static void setBugEndTime(Bug bug){
	 for(int j=0;j<bug.DB.size();j++){
			if(bug.endTime>bug.startTime)
				bug.startTime=bug.DB.get(j).endTime;
		}
 }
 
 
 
 public static double getTZoneSim(HashMap<Zone, Double> bugZone, ArrayList<Developer> devs){
	 HashMap<Zone, Double> devsUnionZone=new HashMap<Zone, Double>();
	 double tZoneSim=0;
	 for (Entry<Zone, Double>  devZone:devs.get(0).DZone_Coefficient.entrySet()){
		 devsUnionZone.put(devZone.getKey(), devZone.getValue());
	 }
	 for(int i=1;i<devs.size();i++){
		 for (Entry<Zone, Double>  devZone:devs.get(i).DZone_Coefficient.entrySet()){
			 if(devsUnionZone.get(devZone.getKey())<devZone.getValue())
				 devsUnionZone.put(devZone.getKey(),devZone.getValue());
		 }
	 }
	 
	 for (Map.Entry<Zone, Double>  bZone:bugZone.entrySet()){
		 tZoneSim+=Math.min(bZone.getValue(), devsUnionZone.get(bZone.getKey()));
	 }
	return tZoneSim/bugZone.size();
	
 }
 
 
 
 public static double getDataFlow(Bug bug, ArrayList<Developer> devs){
	 double dev_bugZone_sim=0;
	 double dev_not_assigned_sim=0;
	 double dataFlow=0;
	 /*int i=0;
	 for(Map.Entry<Zone, Double>  bZone:bugZone.entrySet()){
		 double maxZoneOverlap=0;
		 double devNotAssignedZoneSim=0;
		 for(Developer dev:devs){
			 maxZoneOverlap=Math.max(dev.DZone_Coefficient.get(bZone.getKey()), maxZoneOverlap);
			 for(Map.Entry<Zone, Double>  bug_zone:bugZone.entrySet()){
				 if(bug_zone.getKey().zId!=bZone.getKey().zId){
					 devNotAssignedZoneSim+=dev.DZone_Coefficient.get(bug_zone.getKey());
				 }
			 }
			 //the only problem might happen is originated from the situation that the bugZoneItem has not had for selected developer.
		 }
		 //dev_bugZone_sim+=Math.max(bZone.getValue(), devs.get(i).DZone_Coefficient.get(bZone.getKey()));
		 dev_bugZone_sim+=maxZoneOverlap;
		 dev_not_assigned_sim+=devNotAssignedZoneSim/(bugZone.size()-1);
		 
		 i++;
	 }*/
	 
	/* HashSet<Developer> developrs=new HashSet<Developer>();
	 	for(Developer d:devs){
	 		developrs.add(d);
	 	}*/
	 	
		double IDFlow[][]=new double[devs.size()][devs.size()];
		for(int i=0;i<IDFlow.length;i++){
			for(int j=0;j<IDFlow[0].length;j++){
				if(devs.get(i)!=devs.get(j)){
					IDFlow[i][j]=getFlowD2D(bug, devs);
				}
				else{
					IDFlow[i][j]=0;
				}
			}
			
		}
		
	return 0; //(dev_bugZone_sim/bugZone.size())+(dev_not_assigned_sim/devs.size());
	
	
	
	
	 
 }
 
 public static double getFlowD2D(Bug b, ArrayList<Developer> devs){
	 // implement (Zi-Zj) and (all zones) 
	 HashMap<Zone, Double> DevsDiff=new HashMap<Zone, Double>();
	 
	return 0;
	 
 }
 public static double getNotAssignedTaskCost(){
	 return 0;
 }
 
}

