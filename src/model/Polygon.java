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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public strictfp class Polygon {
 
    
    private int id; 
    private List<Vertex> vertexes;
    

    public Polygon(int id, List<Vertex> vertexes) {
        this.id = id;
        this.vertexes = vertexes;
        
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
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
            statement.executeUpdate("INSERT INTO p_vertex(latitudes,longitudes,graph_id) VALUES('"+latitude+"','"+longitude+"','"+id+"') ");
            ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID() FROM p_vertex");
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
    
    
    
    public JSONObject getMap(){
        JSONObject map = new JSONObject();
        JSONArray elements = new JSONArray();
        for (Vertex element : vertexes) {
            JSONObject jo = new JSONObject();
            jo.put("lng",element.getLongitude());
            jo.put( "lat",element.getLatitude());            
            elements.add(jo);
        }
        map.put("id", id);
        map.put("vertexes",elements);
        return map;
    
    }
    
    public Vertex searchVertex(double latitudes, double longitudes){
        double minimum = Double.MAX_VALUE;
        Vertex minVertex = null;
        for (Vertex vertex : vertexes) {
            double x = vertex.getLatitude()-latitudes;
            double y = vertex.getLongitude()-longitudes;
            double distance = Math.sqrt((x*x)+(y*y));
            if(minimum>distance){
                minimum = distance;
                minVertex = vertex;
                if (minimum==0) {
                    return minVertex;
                }
            }
        }
        return minVertex;
    }
    
    public Vertex searchVertex(int id){
        for (Vertex vertex : vertexes) {
            if (vertex.getId()==id) {
                return vertex;
            }
        }
        return null;
    }
    
    
    
    
    



}
