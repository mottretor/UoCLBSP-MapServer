package Functions;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class DataBase {    

    public static Statement createStatement() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://uoclbspdbinstance.c5cec24wzera.us-east-1.rds.amazonaws.com:3306/uoclbsp_db", "uocroot", "uocrootpass");
        Statement statement = connection.createStatement();
        return statement;
    }
    
    public static void main(String[] args) {
        try {
            Statement statement = createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM building");
            while (rs.next()) {
                System.out.println(rs.getString(1));
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

   
}
