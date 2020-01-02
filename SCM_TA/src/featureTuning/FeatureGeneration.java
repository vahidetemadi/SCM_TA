package featureTuning;
import org.moeaframework.core.Solution;

public interface FeatureGeneration {
	public Solution getTheFeatureVector(Solution solution);
	
	public  Double getTCR();
	
	public Integer getNumOfDevs();
	
	public Integer getNumOfBugs();
	
	public Double[][] getmissionMatrix();
	
	public Double[][] getTransitionMatrix();
	
}
