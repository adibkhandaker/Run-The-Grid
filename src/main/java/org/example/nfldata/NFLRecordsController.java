package org.example.nfldata;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class NFLRecordsController implements Initializable {
    @FXML private ChoiceBox<String> categoryChoice;
    @FXML private Button loadButton;
    @FXML private Label statusLabel;
    @FXML private TableView<LeaderRow> leadersTable;
    @FXML private TableColumn<LeaderRow, Integer> rankCol;
    @FXML private TableColumn<LeaderRow, String> playerCol;
    @FXML private TableColumn<LeaderRow, String> valueCol;
    @FXML private TableColumn<LeaderRow, String> statusCol;

    private final Map<String, JSONObject> categoryMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        playerCol.setCellValueFactory(new PropertyValueFactory<>("player"));
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        fetchCategories();
    }

    private void fetchCategories() {
        new Thread(() -> {
            try {
                String apiUrl = "https://sports.core.api.espn.com/v2/sports/football/leagues/nfl/leaders";
                String json = fetchJson(apiUrl);
                JSONParser parser = new JSONParser();
                JSONObject root = (JSONObject) parser.parse(json);
                JSONArray categories = (JSONArray) root.get("categories");
                List<String> categoryNames = new ArrayList<>();
                for (Object obj : categories) {
                    JSONObject cat = (JSONObject) obj;
                    String displayName = (String) cat.get("displayName");
                    categoryNames.add(displayName);
                    categoryMap.put(displayName, cat);
                }
                Platform.runLater(() -> {
                    categoryChoice.setItems(FXCollections.observableArrayList(categoryNames));
                    if (!categoryNames.isEmpty()) categoryChoice.setValue(categoryNames.get(0));
                });
            } catch (Exception e) {
                showStatus("Failed to load categories", true);
            }
        }).start();
    }

    @FXML
    private void loadLeaders() {
        String selected = categoryChoice.getValue();
        if (selected == null || !categoryMap.containsKey(selected)) {
            showStatus("Select a category", true);
            return;
        }
        showStatus("Loading leaders...", false);
        new Thread(() -> {
            try {
                JSONObject cat = categoryMap.get(selected);
                JSONArray leaders = (JSONArray) cat.get("leaders");
                List<LeaderRow> rows = new ArrayList<>();
                int rank = 1;
                for (Object obj : leaders) {
                    JSONObject leader = (JSONObject) obj;
                    String value = String.valueOf(leader.get("displayValue"));
                    String status = (Boolean.TRUE.equals(leader.get("active"))) ? "Active" : "Retired";
                    JSONObject athleteObj = (JSONObject) leader.get("athlete");
                    String athleteUrl = (String) athleteObj.get("$ref");
                    String playerName = fetchAthleteName(athleteUrl);
                    rows.add(new LeaderRow(rank++, playerName, value, status));
                }
                Platform.runLater(() -> {
                    leadersTable.setItems(FXCollections.observableArrayList(rows));
                    showStatus("Loaded successfully", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showStatus("Failed to load leaders", true));
            }
        }).start();
    }

    private String fetchAthleteName(String url) {
        try {
            String json = fetchJson(url);
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(json);
            return (String) root.getOrDefault("displayName", "Unknown");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String fetchJson(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();
        return sb.toString();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        if (isError) {
            if (!statusLabel.getStyleClass().contains("error-label"))
                statusLabel.getStyleClass().add("error-label");
        } else {
            statusLabel.getStyleClass().remove("error-label");
        }
    }

    @FXML
    private void goBack() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.setScene(scene);
    }

    public static class LeaderRow {
        private final Integer rank;
        private final String player;
        private final String value;
        private final String status;
        public LeaderRow(Integer rank, String player, String value, String status) {
            this.rank = rank;
            this.player = player;
            this.value = value;
            this.status = status;
        }
        public Integer getRank() { return rank; }
        public String getPlayer() { return player; }
        public String getValue() { return value; }
        public String getStatus() { return status; }
    }
} 