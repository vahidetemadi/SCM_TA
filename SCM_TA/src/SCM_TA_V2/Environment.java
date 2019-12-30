package SCM_TA_V2;

import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.math3.distribution.*;
import smile.stat.distribution.PoissonDistribution;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import SCM_TA_V1.Developer;

public abstract class Environment {
	
	static PoissonDistribution TCR;
	static DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge> devNetwork;	
	static HashMap<Integer,Developer> developers=null;
	
	//public abstract void generaeListOfObservation();
}
