/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package functions;



import java.util.LinkedList;
import model.Vertex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by X556U- DM818 on 8/23/2017.
 */

public strictfp class DirectionsJsonParser {

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public static LinkedList<Vertex> parse(JSONObject jObject){

        LinkedList<Vertex> routes = new LinkedList<Vertex>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = (JSONArray) jObject.get("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.size();i++){
                jLegs = (JSONArray) ( (JSONObject)jRoutes.get(i)).get("legs");
                

                /** Traversing all legs */
                for(int j=0;j<jLegs.size();j++){
                    jSteps = (JSONArray) ( (JSONObject)jLegs.get(j)).get("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.size();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        LinkedList<Vertex> list = decodePoly(polyline);
                        routes.addAll(list);
                        
                    }
                    
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return routes;
    }
    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private static LinkedList<Vertex> decodePoly(String encoded) {

        LinkedList<Vertex> poly = new LinkedList<Vertex>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

           
            poly.add(new Vertex(0, (((double) lat / 1E5)), (((double) lng / 1E5))));
        }

        return poly;
    }
}