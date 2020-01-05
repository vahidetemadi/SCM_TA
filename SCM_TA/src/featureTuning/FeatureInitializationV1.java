package featureTuning;

import java.util.ArrayList;

public class FeatureInitializationV1 implements FeatureInitialization {

	ArrayList<Integer> devNum=new ArrayList<Integer>();
	ArrayList<Integer> bugNum=new ArrayList<Integer>();
	ArrayList<Double> TCR=new ArrayList<Double>();
 	ArrayList<Double[][]> em=new ArrayList<Double[][]>();
 	ArrayList<Double[][]> tm=new ArrayList<Double[][]>();
	
	
 	//create the private static--- required for singleton
 	private static FeatureInitializationV1 single_instance=null;
 	
 	
 	//private constructor to prevent instanceation
 	private FeatureInitializationV1() {
		// TODO Auto-generated constructor stub
	}
 	
 	
 	public static FeatureInitializationV1 getInstance(){
 		if(single_instance==null)
 			single_instance=new FeatureInitializationV1();
 		return single_instance;
 	}
 	
 	
	@Override
	public void initializeSourceDevNum() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeSourceBugNum() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeSourceTCR() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeSourceEM() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeSourceTM() {
		// TODO Auto-generated method stub
		
	}

}
