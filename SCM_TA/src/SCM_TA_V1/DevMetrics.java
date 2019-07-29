package SCM_TA_V1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import SCM_TA_V1.Sorting;

public class DevMetrics extends Metrics implements Serializable{
	
	public static void sortByMetric(ArrayList<Ranking<Developer, Double>> Devs){
		//sort by the value of specific metric
		Collections.sort(Devs, new Sorting());
	}
	public static Ranking<Developer, Double> computeMetric(Developer d){
		double pVal=0.0;
		for(Double pValue:d.DZone_Coefficient.values()){
			pVal+=pValue;
		}
		pVal=(pVal/d.DZone_Coefficient.size());

		Ranking<Developer, Double> item=new Ranking<Developer, Double>(d, pVal);
		return item;
	}
}
