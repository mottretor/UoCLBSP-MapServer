package Servers;

import Functions.UocMap;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TheMapServer {

    static final int PORT = 1978;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            System.out.println("Started new client");
            new MapClient(socket).start();
        }
    }
}

class MapClient extends Thread {

    protected Socket socket;

    public MapClient(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        DataOutputStream dataOutputStream = null;
        try {
            inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;

        try {
            line = bufferedReader.readLine();
            String rawData = "";
            boolean isPost = line.startsWith("POST");
            int contentLength = 0;
            if (isPost) {
                while (!(line = bufferedReader.readLine()).equals("")) {
                    final String contentHeader = "Content-Length: ";
                    if (line.startsWith(contentHeader)) {
                        contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                    }
                }
                StringBuilder body = new StringBuilder();
                int c = 0;
                for (int i = 0; i < contentLength; i++) {
                    c = bufferedReader.read();
                    body.append((char) c);
                }
                rawData = body.toString();
            } else {
                rawData = line;
            }

            System.out.println(rawData);
            String rawOut = computeResult(rawData);
             System.out.println(rawOut);

            if (isPost) {
                // send response
                dataOutputStream.writeBytes("HTTP/1.1 200 OK\r\n");
                dataOutputStream.writeBytes("Content-Type: text/html\r\n");
                dataOutputStream.writeBytes("Access-Control-Allow-Origin: *\r\n");
                dataOutputStream.writeBytes("\r\n");

            }
            dataOutputStream.writeBytes(rawOut);
            //
            // do not in.close();
            dataOutputStream.flush();
            dataOutputStream.close();

//                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
//                    socket.close();
//                    return;
//                } else {
//                    dataOutputStream.writeBytes(line + "\n");
//                    dataOutputStream.flush();
//                }
        } catch (IOException e) {
            e.printStackTrace();
            return;

        }
    }

    private String computeResult(String rawData) {
        try {
            JSONObject mainObject = (JSONObject) new JSONParser().parse(rawData);
            String type = (String) mainObject.get("type");
            JSONObject outObject = null;
            switch(type){
                case "polyRequest":
                    //outObject = UocMap.getPolygons();
                    outObject = (JSONObject) new JSONParser().parse("{\"polygons\":[{\"id\":100,\"vertexes\":[{\"lat\": 6.903045, \"lng\": 79.860281},{\"lat\": 6.902116, \"lng\": 79.861996},{\"lat\": 6.899326, \"lng\": 79.860805},{\"lat\": 6.898815, \"lng\": 79.860429}],\"edges\":[{\"edge1\":1},{\"edge2\":2},{\"edge3\":3},{\"edge4\":4}]},{\"id\":200,\"vertexes\":[{\"lat\": 6.899528, \"lng\": 79.859785},{\"lat\": 6.903181, \"lng\": 79.858584},{\"lat\": 6.902351, \"lng\": 79.857511}],\"edges\":[{\"edge1\":1},{\"edge2\":2},{\"edge3\":3}]},{\"id\":647,\"vertexes\":[{\"lat\": 6.901509, \"lng\": 79.856942},{\"lat\": 6.901019, \"lng\": 79.855193},{\"lat\": 6.900242, \"lng\": 79.855440}],\"edges\":[{\"edge1\":1},{\"edge2\":2},{\"edge3\":3}]}]}");
                    break;
                case "getPath":
                    outObject = UocMap.getRoute(mainObject);
                    break;
                case "geofence":
                    UocMap.addPolygon(mainObject);
                    outObject = (JSONObject) new JSONParser().parse("{\"result\":\"success\"}");
                    break;
                
            }
            
            return outObject.toJSONString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
