package Resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;



public class Values {
    public static String api_key = "";
    
    public static void getApiKey(){
        try {
            FileReader fileReader = new FileReader(".env");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine())!= null) {              
                if(line.substring(0, 7).equals("API-KEY") && line.split(":").length==2){
                    api_key = line.split(":")[1].trim();
                    break;
                }
            }
            
        } catch (FileNotFoundException fileNotFoundException) {
            System.err.println(".env File not Found");
        }catch (IOException iOException){
            System.err.println("Can't read from .env File");
        }
    }
}
