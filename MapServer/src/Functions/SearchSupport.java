/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Functions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author X556U- DM818
 */
public class SearchSupport {
    
    public static String placesApiKey = "AIzaSyCc6Pbmh1lT4xvAhVjRPNUbGevqE8CKUv4";
    public static int resultCount = 5;
    
    public static JSONObject getSearchResults(String clue, String role){
        JSONArray resultArray = new JSONArray();
        if (role.equals("registerd")) {
            LinkedList<JSONObject> people = peopleSearch(clue);
            while (resultArray.size()<resultCount & people.size()>0) {                
                resultArray.add(people.pollFirst());
            }
        }
        if (resultArray.size()<resultCount) {
            LinkedList<JSONObject> buildings = buildingSearch(clue);
            while (resultArray.size()<resultCount & buildings.size()>0) {                
                resultArray.add(buildings.pollFirst());
            }
        }
        if (resultArray.size()<resultCount) {
            LinkedList<JSONObject> places = placesSearch(clue);
            while (resultArray.size()<resultCount & places.size()>0) {
                JSONObject placeJSONObject = formalize(places.pollFirst());
                resultArray.add(placeJSONObject);
            }
        }
        JSONObject resultJSONObject = new JSONObject();
        resultJSONObject.put("Results", resultArray);
        return resultJSONObject;
    }

    private static LinkedList<JSONObject> peopleSearch(String clue) {
        LinkedList<JSONObject> result = new LinkedList<JSONObject>();
        try {
            Statement statement = DataBase.createStatement();
            ResultSet resultSet = statement.executeQuery("select p.name,b.latitudes, b.longitudes from people as p, room as r, building as b where p.name Like '"+clue+"%"+"' and p.room_id=r.id and r.building_id=b.id");
            while (resultSet.next() & result.size()<resultCount) {                
                JSONObject node = new JSONObject();
                node.put("name", resultSet.getString(1));
                node.put("lat", resultSet.getDouble(2));
                node.put("lng", resultSet.getDouble(3));
                result.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static LinkedList<JSONObject> buildingSearch(String clue) {
        LinkedList<JSONObject> result = new LinkedList<JSONObject>();
        try {
            Statement statement = DataBase.createStatement();
            ResultSet resultSet = statement.executeQuery("select name, latitudes, longitudes from building where name Like '"+clue+"%"+"'");
            while (resultSet.next() & result.size()<resultCount) {                
                JSONObject node = new JSONObject();
                node.put("name", resultSet.getString(1));
                node.put("lat", resultSet.getDouble(2));
                node.put("lng", resultSet.getDouble(3));
                result.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result; //To change body of generated methods, choose Tools | Templates.
    }

    private static LinkedList<JSONObject> placesSearch(String clue) {
        LinkedList<JSONObject> result = new LinkedList<JSONObject>();
        try {
            
            String parameters = "input="+URLEncoder.encode(clue, "UTF-8")+"&location=7.479908803880181,80.67368828124995&radius=200000&strictbounds&key="+placesApiKey;
            String output = "json";
            String url1 = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;

            URL ur = new URL(url1);
            urlConnection = (HttpURLConnection) ur.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();         
            br.close();
            
            JSONArray resultObj = (JSONArray) ((JSONObject) new JSONParser().parse(data)).get("predictions");
            for (Object object : resultObj) {
                if(result.size()<5){
                    JSONObject node = (JSONObject) object;
                    JSONObject element = new JSONObject();
                    element.put("name", (String)((JSONObject)node.get("structured_formatting")).get("main_text"));
                    element.put("place_id", (String)node.get("place_id"));
                    result.add(element);
                }else{
                    break;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }

    private static JSONObject formalize(JSONObject pollFirst) {
        JSONObject result = new JSONObject();
        try {
            String placeId = (String) pollFirst.get("place_id");
            String parameters = "placeid="+placeId+"&key="+placesApiKey;
            String output = "json";
            String url1 = "https://maps.googleapis.com/maps/api/place/details/" + output + "?" + parameters;
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;

            URL ur = new URL(url1);
            urlConnection = (HttpURLConnection) ur.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();         
            br.close();
            result =  (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(data)).get("result")).get("geometry")).get("location");
            result.put("name", (String)pollFirst.get("name"));            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static void main(String[] args) {
        try {
            //        for (JSONObject jSONObject : buildingSearch("s")) {
//            System.out.println(jSONObject.toJSONString());
//        }
//System.out.println(getSearchResults("sc", "registerd").toJSONString());

JSONObject newobJSONObject = (JSONObject) new JSONParser().parse("{\"graphs\":[{\"vertexes\":[{\"latitude\":6.9021,\"id\":32,\"longitude\":79.8596},{\"latitude\":6.9022,\"id\":5,\"longitude\":79.8606},{\"latitude\":6.9024,\"id\":6,\"longitude\":79.8605},{\"latitude\":6.9016,\"id\":7,\"longitude\":79.86},{\"latitude\":6.9021,\"id\":8,\"longitude\":79.8596},{\"latitude\":6.9022,\"id\":9,\"longitude\":79.8606},{\"latitude\":6.9024,\"id\":10,\"longitude\":79.8605},{\"latitude\":6.9016,\"id\":11,\"longitude\":79.86},{\"latitude\":6.9021,\"id\":12,\"longitude\":79.8596},{\"latitude\":6.9022,\"id\":13,\"longitude\":79.8606},{\"latitude\":6.9024,\"id\":14,\"longitude\":79.8605},{\"latitude\":6.9016,\"id\":15,\"longitude\":79.86},{\"latitude\":6.9021,\"id\":16,\"longitude\":79.8596},{\"latitude\":6.9022,\"id\":17,\"longitude\":79.8606},{\"latitude\":6.9024,\"id\":18,\"longitude\":79.8605},{\"latitude\":6.9016,\"id\":19,\"longitude\":79.86},{\"latitude\":6.9021,\"id\":20,\"longitude\":79.8596},{\"latitude\":6.9022,\"id\":25,\"longitude\":79.8606},{\"latitude\":6.9024,\"id\":26,\"longitude\":79.8605},{\"latitude\":6.9016,\"id\":27,\"longitude\":79.86},{\"latitude\":6.9021,\"id\":28,\"longitude\":79.8596},{\"latitude\":6.9022,\"id\":29,\"longitude\":79.8606},{\"latitude\":6.9024,\"id\":30,\"longitude\":79.8605},{\"latitude\":6.9016,\"id\":31,\"longitude\":79.86}],\"edges\":[{\"destination\":6,\"id\":5,\"source\":5},{\"destination\":7,\"id\":6,\"source\":5},{\"destination\":8,\"id\":7,\"source\":6},{\"destination\":8,\"id\":8,\"source\":7},{\"destination\":10,\"id\":9,\"source\":9},{\"destination\":11,\"id\":10,\"source\":9},{\"destination\":12,\"id\":11,\"source\":10},{\"destination\":12,\"id\":12,\"source\":11},{\"destination\":14,\"id\":13,\"source\":13},{\"destination\":15,\"id\":14,\"source\":13},{\"destination\":16,\"id\":15,\"source\":14},{\"destination\":16,\"id\":16,\"source\":15},{\"destination\":18,\"id\":17,\"source\":17},{\"destination\":19,\"id\":18,\"source\":17},{\"destination\":20,\"id\":19,\"source\":18},{\"destination\":20,\"id\":20,\"source\":19},{\"destination\":26,\"id\":21,\"source\":25},{\"destination\":27,\"id\":22,\"source\":25},{\"destination\":28,\"id\":23,\"source\":26},{\"destination\":28,\"id\":24,\"source\":27},{\"destination\":30,\"id\":25,\"source\":29},{\"destination\":31,\"id\":26,\"source\":29},{\"destination\":32,\"id\":27,\"source\":30},{\"destination\":32,\"id\":28,\"source\":31}],\"id\":1},{\"vertexes\":[],\"edges\":[],\"id\":2},{\"vertexes\":[],\"edges\":[],\"id\":3},{\"vertexes\":[{\"latitude\":6.902791,\"id\":21,\"longitude\":6.902791},{\"latitude\":6.904016,\"id\":22,\"longitude\":6.904016}],\"edges\":[],\"id\":4},{\"vertexes\":[{\"latitude\":6.902791,\"id\":23,\"longitude\":6.902791},{\"latitude\":6.904016,\"id\":24,\"longitude\":6.904016}],\"edges\":[],\"id\":5},{\"vertexes\":[{\"latitude\":6.902791,\"id\":33,\"longitude\":6.902791},{\"latitude\":6.904016,\"id\":34,\"longitude\":6.904016}],\"edges\":[],\"id\":6}],\"polygons\":[{\"vertexes\":[],\"id\":1},{\"vertexes\":[],\"id\":2},{\"vertexes\":[],\"id\":3},{\"vertexes\":[{\"lng\":6.90356,\"id\":1,\"lat\":6.90356},{\"lng\":6.904646,\"id\":2,\"lat\":6.904646},{\"lng\":6.903815,\"id\":3,\"lat\":6.903815},{\"lng\":6.901003,\"id\":4,\"lat\":6.901003},{\"lng\":6.901717,\"id\":5,\"lat\":6.901717}],\"id\":4},{\"vertexes\":[{\"lng\":6.90356,\"id\":6,\"lat\":6.90356},{\"lng\":6.904646,\"id\":7,\"lat\":6.904646},{\"lng\":6.903815,\"id\":8,\"lat\":6.903815},{\"lng\":6.901003,\"id\":9,\"lat\":6.901003},{\"lng\":6.901717,\"id\":10,\"lat\":6.901717}],\"id\":5},{\"vertexes\":[{\"lng\":6.90356,\"id\":11,\"lat\":6.90356},{\"lng\":6.904646,\"id\":12,\"lat\":6.904646},{\"lng\":6.903815,\"id\":13,\"lat\":6.903815},{\"lng\":6.901003,\"id\":14,\"lat\":6.901003},{\"lng\":6.901717,\"id\":15,\"lat\":6.901717}],\"id\":6}]}");
            System.out.println(newobJSONObject.toJSONString());
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
    
}

