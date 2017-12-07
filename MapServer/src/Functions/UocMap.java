package Functions;


import Algorithms.DijkstraAlgorithm;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import model.Edge;
import model.Graph;
import model.Polygon;
import model.Vertex;

public class UocMap {
    public static Graph uocGraph;
    public static LinkedList<Graph> uocGraphs = null;
    public static LinkedList<Polygon> uocPolygons = null;
    
    public static void LoadDatabase(){
        try {
            Statement statement = DataBase.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM graph");
            while (resultSet.next()) {
                HashMap<Integer,Vertex> vertexes = new HashMap<Integer,Vertex>();
                ArrayList<Edge> edges = new ArrayList<Edge>();
                Statement verStatement = DataBase.createStatement();
                ResultSet verResultSet = verStatement.executeQuery("SELECT * FROM g_vertex WHERE graph_id='"+resultSet.getInt("id")+"'");
                while (verResultSet.next()) {                    
                    vertexes.put(verResultSet.getInt("id"),new Vertex(verResultSet.getInt("id"), verResultSet.getDouble("latitudes"), verResultSet.getDouble("longitudes")));
                }
                for (Vertex vertexe : vertexes.values()) {
                    Statement edgeStatement = DataBase.createStatement();
                    ResultSet edgeResultSet = edgeStatement.executeQuery("SELECT * FROM g_edge WHERE g_vertex_id='"+vertexe.getId()+"'");
                    while (edgeResultSet.next()) {                        
                        edges.add(new Edge(edgeResultSet.getInt("id"),vertexe,vertexes.get(edgeResultSet.getInt("g_vertex_id1")),edgeResultSet.getDouble("weight")));
                    }
                }
                
                vertexes = new HashMap<Integer,Vertex>();
                edges = new ArrayList<Edge>();
                verStatement = DataBase.createStatement();
                verResultSet = verStatement.executeQuery("SELECT * FROM p_vertex WHERE graph_id='"+resultSet.getInt("id")+"'");
                while (verResultSet.next()) {                    
                    vertexes.put(verResultSet.getInt("id"),new Vertex(verResultSet.getInt("id"), verResultSet.getDouble("latitudes"), verResultSet.getDouble("longitudes")));
                }
                for (Vertex vertexe : vertexes.values()) {
                    Statement edgeStatement = DataBase.createStatement();
                    ResultSet edgeResultSet = edgeStatement.executeQuery("SELECT * FROM p_edge WHERE p_vertex_id='"+vertexe.getId()+"'");
                    while (edgeResultSet.next()) {                        
                        edges.add(new Edge(edgeResultSet.getInt("id"),vertexe,vertexes.get(edgeResultSet.getInt("p_vertex_id1")),edgeResultSet.getDouble("weight")));
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        uocGraph = new Graph(1, new ArrayList<Vertex>(), new ArrayList<Edge>());
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
