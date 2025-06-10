package org.example.nfldata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class APIController {

    public static HttpURLConnection fetchAPIResponse(String urlString) throws IOException {
        try {
            URL url =  new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readAPIResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        Scanner scanner = new Scanner(connection.getInputStream());
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();
        return response.toString();
    }
}
