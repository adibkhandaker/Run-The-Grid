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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class TeamDraftController implements Initializable {
    @FXML
    private ChoiceBox<Integer> yearChoice;

    @FXML
    private TableView<DraftPlayer2025> draftTable;

    @FXML
    private TableColumn<DraftPlayer2025, String> pickColumn;

    @FXML
    private TableColumn<DraftPlayer2025, String> playerColumn;

    @FXML
    private TableColumn<DraftPlayer2025, String> positionColumn;

    @FXML
    private TableColumn<DraftPlayer2025, String> collegeColumn;

    @FXML
    private TableColumn<DraftPlayer2025, String> roundColumn;

    @FXML
    private Label draftLabel;

    @FXML
    private ImageView teamLogo;

    @FXML
    private Label statusLabel;

    @FXML
    private Label teamNameLabel;

    @FXML
    private Button backButton;

    private int teamID;
    private JSONObject team;
    private final Map<Integer, JSONArray> draftDataCache = new HashMap<>();
    private final Map<String, JSONObject> athleteCache = new HashMap<>();
    private final Map<String, String> collegeCache = new HashMap<>();

    public void setTeam(JSONObject team) {
        this.team = team;
        this.teamID = Integer.parseInt((String) team.get("id"));
        teamNameLabel.setText((String) team.get("displayName") + " Draft History");
        setTeamLogo();
        getDraftData(null);
    }

    public void getDraftData(ActionEvent event) {
        Integer year = yearChoice.getValue();
        if (year == null) return;

        draftLabel.setText(year + " DRAFT CLASS");
        statusLabel.setText("Loading draft data for " + year + "...");
        statusLabel.setVisible(true);
        draftTable.getItems().clear();
        athleteCache.clear();
        collegeCache.clear();

        Task<ObservableList<DraftPlayer2025>> task = new Task<>() {
            @Override
            protected ObservableList<DraftPlayer2025> call() throws Exception {
                return fetchDraftDataForYear(year);
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<DraftPlayer2025> result = task.getValue();
            if (result.isEmpty()) {
                statusLabel.setText("No data available for " + year);
                statusLabel.setVisible(true);
            } else {
                statusLabel.setVisible(false);
            }
            draftTable.setItems(result);
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Failed to load draft data.");
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
                    JSONObject teamPick = (JSONObject) pick;
                    JSONObject teamRef = (JSONObject) teamPick.get("team");
                    if (teamRef != null) {
                        String teamAPI = (String) teamRef.get("$ref");
                        if (teamAPI != null && !teamAPI.isEmpty()) {
                            String teamIdStr = teamAPI.substring(teamAPI.lastIndexOf('/') + 1);
                            if (teamIdStr.contains("?")) {
                                teamIdStr = teamIdStr.substring(0, teamIdStr.indexOf("?"));
                            }
                            if (Integer.parseInt(teamIdStr) == this.teamID) {
                                data.add(parsePick(parser, teamPick, String.valueOf(year)));
                            }
                        }
                    }
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

        JSONObject athleteRef = (JSONObject) teamPick.get("athlete");
        String athleteName = "N/A";
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

            athleteName = (String) athleteRoot.get("displayName");

            JSONObject collegeRef = (JSONObject) athleteRoot.get("college");
            if (collegeRef != null && collegeRef.get("$ref") != null) {
                String collegeAPI = (String) collegeRef.get("$ref");
                collegeName = collegeCache.computeIfAbsent(collegeAPI, this::fetchCollegeName);
            }

            JSONObject positionRef = (JSONObject) athleteRoot.get("position");
            if (positionRef != null) {
                positionName = (String) positionRef.get("abbreviation");
            }
        }

        return new DraftPlayer2025("", round, overallPick, "", athleteName, positionName, "", collegeName);
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

    private void setTeamLogo() {
        try {
            String teamAPI = "http://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/2025/teams/" + teamID + "?lang=en&region=us";
            HttpURLConnection team = APIController.fetchAPIResponse(teamAPI);
            String responseTeam = APIController.readAPIResponse(team);
            JSONParser myParser = new JSONParser();
            JSONObject teamJSON = (JSONObject) myParser.parse(responseTeam);
            JSONArray logos = (JSONArray) teamJSON.get("logos");
            JSONObject mainLogo = (JSONObject) logos.get(0);
            String logoLink = (String) mainLogo.get("href");
            Image logoImage = new Image(logoLink);
            teamLogo.setImage(logoImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int i = 1970; i <= 2025; ++i) {
            yearChoice.getItems().add(i);
        }
        yearChoice.setValue(2025);
        yearChoice.setOnAction(this::getDraftData);
        roundColumn.setCellValueFactory(new PropertyValueFactory<>("round"));
        pickColumn.setCellValueFactory(new PropertyValueFactory<>("pick"));
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        collegeColumn.setCellValueFactory(new PropertyValueFactory<>("college"));
        draftTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        statusLabel.setVisible(false);
    }

    @FXML
    private void goBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamOptions.fxml"));
        Parent root = loader.load();
        TeamOptionsController controller = loader.getController();
        controller.setTeam(this.team);
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
