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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
            
            String parameters = "input="+URLEncoder.encode(clue, "UTF-8")+"&location=7.479908803880181,80.67368828124995&radius=200000&key="+placesApiKey;
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
//        for (JSONObject jSONObject : buildingSearch("s")) {
//            System.out.println(jSONObject.toJSONString());
//        }
            //System.out.println(getSearchResults("sc", "registerd").toJSONString());
    }
    
}

