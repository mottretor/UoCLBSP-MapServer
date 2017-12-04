/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import Functions.DataBase;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class Graph {
    private List<Vertex> vertexes;
    private List<Edge> edges;

    public Graph(List<Vertex> vertexes, List<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
    
    //Function to add Vertex to the Graph. Returns the added Vertex, if the Vertex exists returns it. Returns null if unsucceeded. 
    public Vertex addVertex(double latitude, double longitude) {
        int currid = -1;
        for (Vertex vertex : vertexes) {
            if (vertex.getLatitude() == latitude && vertex.getLongitude() == longitude) {
                return vertex;
            }
        }
        try {
            Statement statement = DataBase.createStatement();
            statement.executeUpdate("INSERT INTO g_vertex(latitudes,longitudes) VALUES('"+latitude+"','"+longitude+"') ");
            ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID() FROM g_vertex");
            rs.next();
            currid = rs.getInt(1); 
            Vertex newVertex = new Vertex(currid, latitude, longitude);
            vertexes.add( newVertex);
            return newVertex;
            
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        return null;
    }
    
    public Edge addEdge(Vertex v1 , Vertex v2){
        int currid = -1;
        for (Edge edge : edges) {
            if (edge.getSource().equals(v1) && edge.getDestination().equals(v2)) {
                return edge;
            }
            if (edge.getSource().equals(v2) && edge.getDestination().equals(v1)) {
                return edge;
            }
        }
        try {
            double x1, x2, y1, y2;
            x1 = v1.getLatitude();
            x2 = v2.getLatitude();
            y1 = v1.getLongitude();
            y2 = v2.getLongitude();
            
            x1 = Math.sqrt(((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2)));            
            
            Statement statement = DataBase.createStatement();
            statement.executeUpdate("INSERT INTO g_edge(g_vertex_id,g_vertex_id1,weight) VALUES('"+v1.getId()+"','"+v2.getId()+"','"+x1+"') ");
            ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID() FROM g_edge");
            rs.next();
            currid = rs.getInt(1); 
            Edge newEdge = new Edge(currid, v1, v2, x1);
            edges.add(newEdge);
            return newEdge;
            
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        return null;
    }



}
