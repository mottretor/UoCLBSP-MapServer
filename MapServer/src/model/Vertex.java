package model;

import Functions.DataBase;
import java.sql.ResultSet;
import java.sql.Statement;

public class Vertex {

    final private int id;
    final private double latitude;
    final private double longitude;

    //Function to add Vertex to the Graph. Returns the added Vertex, if the Vertex exists returns it. Returns null if unsucceeded. 
    public static Vertex addVertex(Graph graph, double latitude, double longitude) {
        int currid = -1;
        for (Vertex vertex : graph.getVertexes()) {
            if (vertex.latitude == latitude && vertex.longitude == longitude) {
                return vertex;
            }
        }
        try {
            Statement statement = DataBase.createStatement();
            ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID() FROM g_vertex");
            rs.next();
            currid = rs.getInt(1)+1;
            statement.executeUpdate("INSERT INTO g_vertex(latitudes,longitudes) VALUES('"+latitude+"','"+longitude+"') ");
            return new Vertex(currid, latitude, longitude);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        
    }

    //initiation
    public Vertex(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == 0) ? 0 : String.valueOf(id).hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vertex other = (Vertex) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(latitude) + ", " + String.valueOf(longitude);
    }

}
