package SCM_TA_V1;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
public class DAGEdge implements EdgeFactory<Bug, DefaultEdge> {

	@Override
	public DefaultEdge createEdge(Bug arg0, Bug arg1) {
		// TODO Auto-generated method stub
		return new DefaultEdge();
	}

}
