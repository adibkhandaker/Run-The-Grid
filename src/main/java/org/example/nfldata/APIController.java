package org.example.nfldata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class APIController {

    private static final String BASE_URL = "http://site.api.espn.com/apis/site/v2/sports/football/nfl/";

    public static JSONObject getBoxScore(String gameId) throws IOException, ParseException {
        String urlString = BASE_URL + "summary?event=" + gameId;
        HttpURLConnection connection = fetchAPIResponse(urlString);
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static JSONObject getTeams() throws IOException, ParseException {
        String urlString = BASE_URL + "teams";
        HttpURLConnection connection = fetchAPIResponse(urlString);
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static JSONObject getDepthChart(String teamId) throws IOException, ParseException {
        String urlString = BASE_URL + "teams/" + teamId + "/depthchart";
        HttpURLConnection connection = fetchAPIResponse(urlString);
        if (connection != null && connection.getResponseCode() == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static JSONObject getPlayerInfo(String playerId) throws IOException, ParseException {
        String urlString = BASE_URL + "athletes/" + playerId;
        HttpURLConnection connection = fetchAPIResponse(urlString);
        if (connection != null && connection.getResponseCode() == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static JSONObject getRoster(String teamId) throws IOException, ParseException {
        String urlString = BASE_URL + "teams/" + teamId + "/roster";
        HttpURLConnection connection = fetchAPIResponse(urlString);
        if (connection != null && connection.getResponseCode() == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static JSONObject getPlayerStats(String playerId, String season) throws IOException, ParseException {
        String urlString = "https://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/" + season + "/athletes/" + playerId + "/eventlog";
        HttpURLConnection connection = fetchAPIResponse(urlString);
        if (connection != null && connection.getResponseCode() == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static JSONObject getStatisticsFromUrl(String url) throws IOException, ParseException {
        HttpURLConnection connection = fetchAPIResponse(url);
        if (connection != null && connection.getResponseCode() == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static JSONObject getDraftByYear(String year) throws IOException, ParseException {
        String urlString = BASE_URL + "draft/" + year;
        HttpURLConnection connection = fetchAPIResponse(urlString);
        if (connection != null && connection.getResponseCode() == 200) {
            String response = readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response);
        }
        return null;
    }

    public static String getRawJsonFromUrl(String urlString) throws IOException {
        HttpURLConnection connection = fetchAPIResponse(urlString);
        if (connection != null && connection.getResponseCode() == 200) {
            return readAPIResponse(connection);
        }
        return null;
    }

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
