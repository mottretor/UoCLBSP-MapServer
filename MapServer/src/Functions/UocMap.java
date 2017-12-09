package Functions;

import Algorithms.DijkstraAlgorithm;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import model.Edge;
import model.Graph;
import model.Polygon;
import model.Vertex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public strictfp class UocMap {

    public static Graph uocGraph;
    public static HashMap<Integer, Graph> uocGraphs = new HashMap<Integer, Graph>();
    public static HashMap<Integer, Polygon> uocPolygons = new HashMap<Integer, Polygon>();
    public static HashMap<Integer, ArrayList<Vertex>> uocOut = new HashMap<Integer, ArrayList<Vertex>>();

    public static void LoadDatabase() {
        try {
            Statement statement = DataBase.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM graph");
            while (resultSet.next()) {
                HashMap<Integer, Vertex> vertexes = new HashMap<Integer, Vertex>();
                ArrayList<Edge> edges = new ArrayList<Edge>();
                Statement verStatement = DataBase.createStatement();
                ResultSet verResultSet = verStatement.executeQuery("SELECT * FROM g_vertex WHERE graph_id='" + resultSet.getInt("id") + "'");
                while (verResultSet.next()) {
                    vertexes.put(verResultSet.getInt("id"), new Vertex(verResultSet.getInt("id"), verResultSet.getDouble("latitudes"), verResultSet.getDouble("longitudes")));
                }
                for (Vertex vertexe : vertexes.values()) {
                    Statement edgeStatement = DataBase.createStatement();
                    ResultSet edgeResultSet = edgeStatement.executeQuery("SELECT * FROM g_edge WHERE g_vertex_id='" + vertexe.getId() + "'");
                    while (edgeResultSet.next()) {
                        edges.add(new Edge(edgeResultSet.getInt("id"), vertexe, vertexes.get(edgeResultSet.getInt("g_vertex_id1")), edgeResultSet.getDouble("weight")));
                    }
                }
                Statement outStatement = DataBase.createStatement();
                ResultSet outResultSet = outStatement.executeQuery("SELECT * FROM g_out WHERE graph_id='" + resultSet.getInt("id") + "'");
                ArrayList<Vertex> outVertex = new ArrayList<Vertex>();
                while (outResultSet.next()) {
                    outVertex.add(vertexes.get(outResultSet.getInt("g_vertex_id")));
                }
                uocOut.put(resultSet.getInt("id"), outVertex);

                uocGraphs.put(resultSet.getInt("id"), new Graph(resultSet.getInt("id"), new ArrayList<Vertex>(vertexes.values()), edges));

                vertexes = new HashMap<Integer, Vertex>();
                verStatement = DataBase.createStatement();
                verResultSet = verStatement.executeQuery("SELECT * FROM p_vertex WHERE graph_id='" + resultSet.getInt("id") + "'");
                while (verResultSet.next()) {
                    vertexes.put(verResultSet.getInt("id"), new Vertex(verResultSet.getInt("id"), verResultSet.getDouble("latitudes"), verResultSet.getDouble("longitudes")));
                }

                uocPolygons.put(resultSet.getInt("id"), new Polygon(resultSet.getInt("id"), new ArrayList<Vertex>(vertexes.values())));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getRoute(JSONObject points) {
        JSONObject source = (JSONObject) points.get("source");
        JSONObject destination = (JSONObject) points.get("destination");
        Vertex[] minPath;
        LinkedList<Vertex> verList;
        if ((Long) source.get("inside") != 0 && (Long) destination.get("inside") != 0) {
            Graph sGraph = uocGraphs.get((Long) source.get("inside"));
            Graph dGraph = uocGraphs.get((Long) destination.get("inside"));
            Vertex sVertex = sGraph.searchVertex((Double) source.get("latitudes"), (Double) source.get("longitudes"));
            Vertex dVertex = dGraph.searchVertex((Double) destination.get("latitudes"), (Double) destination.get("longitudes"));
            if (sGraph.getId() == dGraph.getId()) {
                verList = sGraph.getInnerDirections(sVertex, dVertex);
            } else {
                minPath = findMinimum(uocOut.get(sGraph.getId()), uocOut.get(dGraph.getId()));

                LinkedList<Vertex> sRoute = sGraph.getInnerDirections(sVertex, minPath[0]);
                LinkedList<Vertex> gRoute = getGoogleRoute(minPath[0].getLatitude(), minPath[0].getLongitude(), minPath[1].getLatitude(), minPath[1].getLongitude());
                LinkedList<Vertex> dRoute = dGraph.getInnerDirections(minPath[1], dVertex);
                sRoute.addAll(gRoute);
                sRoute.addAll(dRoute);
                verList = sRoute;
            }
        } else if ((Long) source.get("inside") != 0) {
            Graph sGraph = uocGraphs.get((Long) source.get("inside"));
            ArrayList<Vertex> outpoint = new ArrayList<Vertex>();
            outpoint.add(new Vertex(0, (Double) destination.get("latitudes"), (Double) destination.get("longitudes")));
            minPath = findMinimum(uocOut.get(sGraph.getId()), outpoint);
            Vertex sVertex = sGraph.searchVertex((Double) source.get("latitudes"), (Double) source.get("longitudes"));
            LinkedList<Vertex> sRoute = sGraph.getInnerDirections(sVertex, minPath[0]);
            LinkedList<Vertex> gRoute = getGoogleRoute(minPath[0].getLatitude(), minPath[0].getLongitude(), minPath[1].getLatitude(), minPath[1].getLongitude());
            sRoute.addAll(gRoute);
            verList = sRoute;
        } else if ((Long) destination.get("inside") != 0) {
            Graph dGraph = uocGraphs.get((Long) destination.get("inside"));
            ArrayList<Vertex> outpoint = new ArrayList<Vertex>();
            outpoint.add(new Vertex(0, (Double) source.get("latitudes"), (Double) source.get("longitudes")));
            minPath = findMinimum(outpoint, uocOut.get(dGraph.getId()));
            Vertex dVertex = dGraph.searchVertex((Double) destination.get("latitudes"), (Double) destination.get("longitudes"));
            LinkedList<Vertex> gRoute = getGoogleRoute(minPath[0].getLatitude(), minPath[0].getLongitude(), minPath[1].getLatitude(), minPath[1].getLongitude());
            LinkedList<Vertex> dRoute = dGraph.getInnerDirections(minPath[1], dVertex);
            gRoute.addAll(dRoute);
            verList = gRoute;
        } else {
            LinkedList<Vertex> gRoute = getGoogleRoute((Double) source.get("latitudes"), (Double) source.get("longitudes"), (Double) destination.get("latitudes"), (Double) destination.get("longitudes"));
            verList = gRoute;
        }
        JSONArray steps = new JSONArray();
        for (Vertex vertex : verList) {
            JSONObject jObj = new JSONObject();
            jObj.put("lattitudes", vertex.getLatitude());
            jObj.put("longitudes", vertex.getLongitude());
            steps.add(jObj);
        }
        JSONObject routes = new JSONObject();
        routes.put("steps", steps);
        return routes;
    }

    public static LinkedList<Vertex> getGoogleRoute(double sLat, double sLon, double dLat, double dLon) {
        try {

            String str_origin = "origin=" + sLat + "," + sLon;
            String str_dest = "destination=" + dLat + "," + dLon;
            String sensor = "sensor=false&mode=driving";
            String parameters = str_origin + "&" + str_dest + "&" + sensor;
            String output = "json";
            String url1 = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
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
            return DirectionsJsonParser.parse((JSONObject) new JSONParser().parse(data));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Vertex[] findMinimum(ArrayList<Vertex> source, ArrayList<Vertex> destination) {
        double x1, x2, y1, y2, minimum;
        minimum = -1;
        Vertex[] mini = new Vertex[2];
        for (Vertex vertex : source) {
            for (Vertex vertex1 : destination) {
                x1 = vertex.getLatitude();
                x2 = vertex1.getLatitude();
                y1 = vertex.getLongitude();
                y2 = vertex1.getLongitude();
                x1 = Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
                if (minimum == -1 | minimum > x1) {
                    minimum = x1;
                    mini[0] = vertex;
                    mini[1] = vertex1;
                }
            }
        }
        return mini;

    }

    public static void addPolygon(JSONObject jSONObject) {
         try {
            Statement statement = DataBase.createStatement();
            statement.executeUpdate("INSERT INTO graph VALUES()");
            ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID() FROM graph");
            rs.next();
            int id = rs.getInt(1); 
            Graph newGraph = new Graph(id, new ArrayList<Vertex>(), new ArrayList<Edge>());
            Polygon newPolygon = new Polygon(id, new ArrayList<Vertex>());
            JSONArray polyArray = (JSONArray) jSONObject.get("polygon");
             for (Object object : polyArray) {
                 JSONObject jsonVertex = (JSONObject)object;
                 newPolygon.addVertex((Double)jsonVertex.get("latitudes"), (Double)jsonVertex.get("latitudes"));
             }
             ArrayList<Vertex> outVertexs = new ArrayList<Vertex>();
             JSONArray outArray = (JSONArray) jSONObject.get("outvertexes");
             for (Object object : outArray) {
                 JSONObject jsonVertex = (JSONObject)object;
                 Vertex outVertex = newGraph.addVertex((Double)jsonVertex.get("latitudes"), (Double)jsonVertex.get("latitudes"));
                 outVertexs.add(outVertex);
                 Statement statements = DataBase.createStatement();
                 statements.executeUpdate("INSERT INTO g_out VALUES('"+id+"','"+outVertex.getId()+"')");
             }
             uocOut.put(id, outVertexs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static JSONObject getPolygons(){
        JSONObject outObject = new JSONObject();
        JSONArray polyArray = new JSONArray();
        for (Polygon value : uocPolygons.values()) {
            polyArray.add(value.getMap());
        }
        outObject.put("polygons", polyArray);
        return outObject;
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

        LoadDatabase();

        for (Graph uocGraph1 : uocGraphs.values()) {
            for (Vertex vertexe : uocGraph1.getVertexes()) {
                System.out.println(vertexe);
            }
        }
        for (Vertex vertex : getGoogleRoute(6.818446, 79.917678, 6.935378, 79.984086)) {
            System.out.println(vertex);
        }
        JSONObject myjson = null;
        try {
           myjson =  (JSONObject) new JSONParser().parse("{\"polygon\":[{\"latitudes\":6.903559594396503,\"longitudes\":79.85890030860901},{\"latitudes\":6.904646000317804,\"longitudes\":79.86027359962463},{\"latitudes\":6.903815219543549,\"longitudes\":79.86171126365662},{\"latitudes\":6.901003335332038,\"longitudes\":79.86103534698486},{\"latitudes\":6.901716959043271,\"longitudes\":79.85937237739563}], \"outvertexes\":[{\"latitudes\":6.902791134641299, \"longitudes\":79.85763155288691},\n" +
"{\"latitudes\":6.904016005872301,\"longitudes\": 79.86369334526057}]}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        addPolygon(myjson);
    }
}
