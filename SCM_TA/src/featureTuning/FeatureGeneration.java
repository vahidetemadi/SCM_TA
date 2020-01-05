package featureTuning;
import org.moeaframework.core.Solution;

public interface FeatureGeneration {
	public Solution getTheFeatureVector(Solution solution);
	
	public void setTCR(Solution solution);
	
	public void setNumOfDevs(Solution solution);
	
	public void setNumOfBugs(Solution solution);
	
	public void setEM(Solution solution);
	
	public void setTM(Solution solution);
	
}
