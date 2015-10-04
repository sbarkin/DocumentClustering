package article;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;


import java.util.List;

public class ArticleSetGraph {

	SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph;
	
	ArticleSetGraph() {
		graph =
		new SimpleWeightedGraph<Integer, DefaultWeightedEdge>
		       (DefaultWeightedEdge.class);
	}
	
	boolean containsArticle(Integer idx) {
		return graph.containsVertex(idx);
	}
	
	List<Integer> similarArticles(Integer idx) {
		return Graphs.neighborListOf(graph, idx);
	}
	
	public int numArticles() {
		return graph.vertexSet().size();
	}
	
	public int numEdges() {
		return graph.edgeSet().size();
	}

	void addArticlePair(int idx1, int idx2, double similarity) {
		
		
		graph.addVertex(idx1);  /* does nothing if already in graph */
		graph.addVertex(idx2);
		
		DefaultWeightedEdge edge = graph.addEdge(idx1, idx2); 
		assert edge != null;  /* error if attempt to insert duplicate edge */
		graph.setEdgeWeight(edge,  similarity);
		/* Note:  does not set cluster ID for any vertices - this is done later */
	}
	
	
}
