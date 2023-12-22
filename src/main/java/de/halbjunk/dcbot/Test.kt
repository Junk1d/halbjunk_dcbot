package de.halbjunk.dcbot;



import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.sql.*;

public class Test {
    public static void main(String[] args) throws IOException {
        String url = "jdbc:mysql://localhost:3306/halbjunk";
        String user = "root";
        String pass = "mysql";

        try {
            Connection con = DriverManager.getConnection(url , user, pass);
            System.out.println("Verbindung erfolgreich hergestellt");
            ResultSet result = con.createStatement().executeQuery("SELECT * FROM Clans");
            while (result.next()){
                System.out.println(result.getString("id"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }





    }
}