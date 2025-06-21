package org.example.nfldata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class DraftByYearController implements Initializable {

    @FXML
    private ChoiceBox<Integer> yearChoice;

    @FXML
    private ChoiceBox<String> roundChoice;

    @FXML
    private TableView<DraftPlayer2025> draftTable;

    @FXML
    private TableColumn<DraftPlayer2025, String> round;

    @FXML
    private TableColumn<DraftPlayer2025, String> pick;

    @FXML
    private TableColumn<DraftPlayer2025, String> team;

    @FXML
    private TableColumn<DraftPlayer2025, String> name;

    @FXML
    private TableColumn<DraftPlayer2025, String> position;

    @FXML
    private TableColumn<DraftPlayer2025, String> height;

    @FXML
    private TableColumn<DraftPlayer2025, String> college;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    private final ObservableList<DraftPlayer2025> allDraftData = FXCollections.observableArrayList();
    private final Map<String, String> teamCache = new HashMap<>();
    private final Map<String, JSONObject> athleteCache = new HashMap<>();
    private final Map<String, String> collegeCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int i = 1970; i <= 2025; ++i) {
            yearChoice.getItems().add(i);
        }
        for (int i = 1; i <= 7; ++i) {
            roundChoice.getItems().add(String.valueOf(i));
        }
        roundChoice.getItems().add("All Rounds");
        roundChoice.setValue("All Rounds");

        statusLabel.setVisible(false);

        yearChoice.setOnAction(this::getYearData);
        roundChoice.setOnAction(this::filterByRound);

        setupTableColumns();
    }

    private void setupTableColumns() {
        round.setCellValueFactory(new PropertyValueFactory<>("round"));
        pick.setCellValueFactory(new PropertyValueFactory<>("pick"));
        team.setCellValueFactory(new PropertyValueFactory<>("team"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        position.setCellValueFactory(new PropertyValueFactory<>("position"));
        height.setCellValueFactory(new PropertyValueFactory<>("height"));
        college.setCellValueFactory(new PropertyValueFactory<>("college"));
        draftTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void getYearData(ActionEvent event) {
        Integer selectedYear = yearChoice.getValue();
        if (selectedYear == null) return;

        statusLabel.setText("Loading draft data for " + selectedYear + "...");
        statusLabel.setVisible(true);
        draftTable.getItems().clear();
        allDraftData.clear();
        teamCache.clear();
        athleteCache.clear();
        collegeCache.clear();


        Task<ObservableList<DraftPlayer2025>> task = new Task<>() {
            @Override
            protected ObservableList<DraftPlayer2025> call() throws Exception {
                return fetchDraftDataForYear(selectedYear);
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<DraftPlayer2025> result = task.getValue();
            if (result.isEmpty()) {
                statusLabel.setText("No data available for " + selectedYear);
            } else {
                statusLabel.setVisible(false);
            }
            allDraftData.setAll(result);
            draftTable.setItems(allDraftData);
            roundChoice.setVisible(true);
            handleRoundSelection(); // Filter for "All Rounds" initially
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Failed to load draft data for " + selectedYear + ".");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private ObservableList<DraftPlayer2025> fetchDraftDataForYear(int year) throws IOException, ParseException {
        ObservableList<DraftPlayer2025> data = FXCollections.observableArrayList();
        String url = "https://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/" + year + "/draft/rounds?limit=20";
        HttpURLConnection connection = APIController.fetchAPIResponse(url);

        if (connection.getResponseCode() == 404) {
            return data;
        }

        String response = APIController.readAPIResponse(connection);

        JSONParser parser = new JSONParser();
        JSONObject root = (JSONObject) parser.parse(response);
        JSONArray items = (JSONArray) root.get("items");

        for (Object item : items) {
            JSONObject roundObject = (JSONObject) item;
            JSONArray picks = (JSONArray) roundObject.get("picks");

            if (picks != null) {
                for (Object pick : picks) {
                    data.add(parsePick(parser, (JSONObject) pick, String.valueOf(year)));
                }
            }
        }
        return data;
    }

    private DraftPlayer2025 parsePick(JSONParser parser, JSONObject teamPick, String year) throws IOException, ParseException {
        Long overallPickInt = (Long) teamPick.get("overall");
        Long roundInt = (Long) teamPick.get("round");
        String overallPick = String.valueOf(overallPickInt);
        String round = String.valueOf(roundInt);

        JSONObject teamRef = (JSONObject) teamPick.get("team");
        String teamName = "N/A";
        if (teamRef != null) {
            String teamAPI = (String) teamRef.get("$ref");
            teamName = teamCache.computeIfAbsent(teamAPI, this::fetchTeamName);
        }

        JSONObject athleteRef = (JSONObject) teamPick.get("athlete");
        String athleteName = "N/A";
        String height = "N/A";
        String collegeName = "N/A";
        String positionName = "N/A";

        if (athleteRef != null) {
            String athleteAPI = (String) athleteRef.get("$ref");
            JSONObject athleteRoot = athleteCache.computeIfAbsent(athleteAPI, key -> {
                try {
                    HttpURLConnection conn = APIController.fetchAPIResponse(key);
                    if (conn == null) return new JSONObject();
                    String resp = APIController.readAPIResponse(conn);
                    return (JSONObject) parser.parse(resp);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new JSONObject();
                }
            });

            if (athleteRoot.get("displayHeight") != null) {
                height = athleteRoot.get("displayHeight").toString().replace("/\"", "\"");
            }
            athleteName = (String) athleteRoot.get("displayName");

            JSONObject collegeRef = (JSONObject) athleteRoot.get("college");
            if (collegeRef != null) {
                String collegeAPI = (String) collegeRef.get("$ref");
                collegeName = collegeCache.computeIfAbsent(collegeAPI, this::fetchCollegeName);
            }

            JSONObject positionRef = (JSONObject) athleteRoot.get("position");
            if(positionRef != null) {
                positionName = (String) positionRef.get("abbreviation");
            }
        }

        return new DraftPlayer2025(year, round, overallPick, teamName, athleteName, positionName, height, collegeName);
    }

    private String fetchTeamName(String url) {
        try {
            HttpURLConnection conn = APIController.fetchAPIResponse(url);
            if (conn == null) return "N/A";
            String resp = APIController.readAPIResponse(conn);
            return (String) ((JSONObject) new JSONParser().parse(resp)).get("abbreviation");
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    private String fetchCollegeName(String url) {
        try {
            HttpURLConnection conn = APIController.fetchAPIResponse(url);
            if (conn == null) return "N/A";
            String resp = APIController.readAPIResponse(conn);
            return (String) ((JSONObject) new JSONParser().parse(resp)).get("name");
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    private void handleRoundSelection() {
        String selectedRound = roundChoice.getValue();
        if (selectedRound == null || allDraftData.isEmpty()) {
            return;
        }

        if ("All Rounds".equals(selectedRound)) {
            draftTable.setItems(allDraftData);
        } else {
            ObservableList<DraftPlayer2025> filteredData = allDraftData.stream()
                    .filter(p -> selectedRound.equals(p.getRound()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            draftTable.setItems(filteredData);
        }
    }

    private void filterByRound(ActionEvent event) {
        String selectedRound = roundChoice.getValue();
        if (selectedRound == null || allDraftData.isEmpty()) {
            return;
        }

        if ("All Rounds".equals(selectedRound)) {
            draftTable.setItems(allDraftData);
        } else {
            ObservableList<DraftPlayer2025> filteredData = allDraftData.stream()
                    .filter(p -> selectedRound.equals(p.getRound()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            draftTable.setItems(filteredData);
        }
    }

    @FXML
    private void goBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
