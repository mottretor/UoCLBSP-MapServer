package Functions;


import Algorithms.DijkstraAlgorithm;
import java.util.ArrayList;
import java.util.LinkedList;
import model.Edge;
import model.Graph;
import model.Vertex;

public class UocMap {
    public static Graph uocGraph;
    
    public static void main(String[] args) {
        uocGraph = new Graph(new ArrayList<Vertex>(), new ArrayList<Edge>());
        Vertex v1 = uocGraph.addVertex(6.9022, 79.8606);
        Vertex v2 = uocGraph.addVertex(6.9024, 79.8605);
        Vertex v3 = uocGraph.addVertex(6.9016, 79.8600);
        Vertex v4 = uocGraph.addVertex(6.9021, 79.8596);
        
        uocGraph.addEdge(v1, v2);
        uocGraph.addEdge(v1, v3);
        uocGraph.addEdge(v2, v4);
        uocGraph.addEdge(v3, v4);
        
        DijkstraAlgorithm da = new DijkstraAlgorithm(uocGraph);
        da.execute(v4);
        LinkedList<Vertex> mylist = da.getPath(v1);
        
        for (Vertex vertex : mylist) {
            System.out.println(vertex);
        }
        
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            DijkstraAlgorithm dw = new DijkstraAlgorithm(uocGraph);
        dw.execute(v1);
        LinkedList<Vertex> mylist1 = dw.getPath(v4);
        
        for (Vertex vertex : mylist1) {
            System.out.println(vertex);
        }
        }
        
final long endTime = System.currentTimeMillis();

System.out.println("Total execution time: " + (endTime - startTime) );

System.out.println(uocGraph.getMap().toJSONString());
        
    }
}
