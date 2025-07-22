package org.example.nfldata;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BoxScoreController {

    @FXML
    private ImageView awayLogo, homeLogo;
    @FXML
    private Label awayTeamName, homeTeamName, finalScoreLabel, gameStatusLabel, gameDateLabel;
    @FXML
    private GridPane teamStatsGrid;
    @FXML
    private HBox playerStatsBox;
    @FXML
    private Button backButton;

    private String gameId;
    private Scene previousScene;

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    public void initialize(JSONObject eventData) {
        populateHeader(eventData);
        
        String gameId = (String) eventData.get("id");
        fetchAndDisplayBoxScore(gameId);
    }

    private void fetchAndDisplayBoxScore(String gameId) {
        new Thread(() -> {
            try {
                JSONObject boxScoreData = APIController.getBoxScore(gameId);
                Platform.runLater(() -> {
                    if (boxScoreData != null) {
                        populateStats(boxScoreData);
                    } else {
                        gameStatusLabel.setText("Box score data not available.");
                    }
                });
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                Platform.runLater(() -> gameStatusLabel.setText("Failed to load box score."));
            }
        }).start();
    }

    private void populateHeader(JSONObject eventData) {
        try {
            JSONArray competitions = (JSONArray) eventData.get("competitions");
            if (competitions == null || competitions.isEmpty()) return;

            JSONObject competition = (JSONObject) competitions.get(0);
            JSONArray competitors = (JSONArray) competition.get("competitors");
            if (competitors == null || competitors.size() < 2) return;
            
            String dateStr = (String) eventData.get("date");
            gameDateLabel.setText(formatGameDate(dateStr, "EEEE, MMMM d, yyyy"));

            JSONObject competitor1 = (JSONObject) competitors.get(0);
            JSONObject competitor2 = (JSONObject) competitors.get(1);

            JSONObject homeData = "home".equals(competitor1.get("homeAway")) ? competitor1 : competitor2;
            JSONObject awayData = "away".equals(competitor1.get("homeAway")) ? competitor1 : competitor2;

            JSONObject homeTeam = (JSONObject) homeData.get("team");
            JSONObject awayTeam = (JSONObject) awayData.get("team");

            awayTeamName.setText((String) awayTeam.get("displayName"));
            homeTeamName.setText((String) homeTeam.get("displayName"));
            finalScoreLabel.setText(awayData.get("score") + " - " + homeData.get("score"));
            
            String awayLogoUrl = (String) awayTeam.get("logo");
            if (awayLogoUrl != null && !awayLogoUrl.isEmpty()) {
                awayLogo.setImage(new Image(awayLogoUrl));
            }

            String homeLogoUrl = (String) homeTeam.get("logo");
            if (homeLogoUrl != null && !homeLogoUrl.isEmpty()) {
                homeLogo.setImage(new Image(homeLogoUrl));
            }
            
            awayLogo.setUserData(awayTeam);
            homeLogo.setUserData(homeTeam);

            JSONObject status = (JSONObject) competition.get("status");
            if (status != null) {
                JSONObject type = (JSONObject) status.get("type");
                if (type != null) {
                    gameStatusLabel.setText((String) type.get("detail"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> gameStatusLabel.setText("Error parsing header data."));
        }
    }

    private void populateStats(JSONObject data) {
        try {
            JSONObject boxscore = (JSONObject) data.get("boxscore");
            if (boxscore != null) {
                populateTeamStats((JSONArray) boxscore.get("teams"));
                populatePlayerStats((JSONArray) boxscore.get("players"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> gameStatusLabel.setText("Error parsing box score data."));
        }
    }

    private void populateTeamStats(JSONArray teamsStatsArray) {
        if (teamsStatsArray == null || teamsStatsArray.size() < 2) return;

        teamStatsGrid.getChildren().clear();
        teamStatsGrid.getColumnConstraints().clear();

        String[] headers = {"Stat", "Away", "Home"};
        for (int i = 0; i < headers.length; i++) {
            Label headerLabel = new Label(headers[i]);
            headerLabel.setStyle("-fx-font-weight: bold;");
            GridPane.setHalignment(headerLabel, HPos.CENTER);
            teamStatsGrid.add(headerLabel, i, 0);
        }

        JSONObject team1Stats = (JSONObject) teamsStatsArray.get(0);
        JSONObject team2Stats = (JSONObject) teamsStatsArray.get(1);
        
        JSONObject awayStats, homeStats;
        if (((JSONObject)team1Stats.get("team")).get("id").equals(((JSONObject)awayLogo.getUserData()).get("id"))) {
            awayStats = team1Stats;
            homeStats = team2Stats;
        } else {
            awayStats = team2Stats;
            homeStats = team1Stats;
        }

        JSONArray awayTeamStats = (JSONArray) awayStats.get("statistics");
        JSONArray homeTeamStats = (JSONArray) homeStats.get("statistics");
        
        for (int i = 0; i < awayTeamStats.size(); i++) {
            JSONObject awayStat = (JSONObject) awayTeamStats.get(i);
            JSONObject homeStat = (JSONObject) homeTeamStats.get(i);
            String awayLabel = (String) awayStat.get("label");
            String homeLabel = (String) homeStat.get("label");
            if(awayLabel != null && awayLabel.equals(homeLabel)) {
                teamStatsGrid.add(new Label(awayLabel), 0, i + 1);
                GridPane.setHalignment(teamStatsGrid.getChildren().get(teamStatsGrid.getChildren().size()-1), HPos.LEFT);
                Label awayValue = new Label((String) awayStat.get("displayValue"));
                awayValue.setStyle("-fx-font-weight: bold;");
                GridPane.setHalignment(awayValue, HPos.CENTER);
                teamStatsGrid.add(awayValue, 1, i + 1);
                
                Label homeValue = new Label((String) homeStat.get("displayValue"));
                homeValue.setStyle("-fx-font-weight: bold;");
                GridPane.setHalignment(homeValue, HPos.CENTER);
                teamStatsGrid.add(homeValue, 2, i + 1);
            }
        }
    }

    private void populatePlayerStats(JSONArray playerStatsArray) {
        if (playerStatsArray == null) {
            System.out.println("Player stats array is null. No stats to display.");
            return;
        }

        playerStatsBox.getChildren().clear();

        JSONObject homeTeamData = (JSONObject) homeLogo.getUserData();
        JSONObject awayTeamData = (JSONObject) awayLogo.getUserData();
        if (homeTeamData == null || awayTeamData == null) {
            System.out.println("Could not get team user data. Cannot sort player stats.");
            return;
        }

        String homeTeamId = (String) homeTeamData.get("id");
        String homeColor = (String) homeTeamData.get("color");
        String awayColor = (String) awayTeamData.get("color");
        if (homeColor == null) homeColor = "#1b263b";
        else homeColor = "#" + homeColor;
        if (awayColor == null) awayColor = "#415a77";
        else awayColor = "#" + awayColor;

        VBox awayContainer = createPlayerStatContainer("AWAY - " + awayTeamData.get("displayName"), awayColor);
        VBox homeContainer = createPlayerStatContainer("HOME - " + homeTeamData.get("displayName"), homeColor);

        playerStatsBox.getChildren().addAll(awayContainer, homeContainer);

        for (Object teamStatsObj : playerStatsArray) {
            JSONObject teamStats = (JSONObject) teamStatsObj;
            JSONObject teamInfo = (JSONObject) teamStats.get("team");
            String currentTeamId = (String) teamInfo.get("id");

            VBox targetContainer = currentTeamId.equals(homeTeamId) ? homeContainer : awayContainer;

            JSONArray statistics = (JSONArray) teamStats.get("statistics");
            if (statistics == null) continue;

            for (Object statCatObj : statistics) {
                JSONObject statCategory = (JSONObject) statCatObj;
                String categoryName = (String) statCategory.get("text");
                JSONArray labels = (JSONArray) statCategory.get("labels");
                JSONArray athletes = (JSONArray) statCategory.get("athletes");

                if (athletes == null || athletes.isEmpty() || labels == null || labels.isEmpty()) {
                    continue;
                }
                
                GridPane table = createPlayerStatTable(targetContainer, categoryName, labels);

                int rowIdx = 1; 
                for (Object athObj : athletes) {
                    JSONObject athleteContainer = (JSONObject) athObj;
                    JSONObject athlete = (JSONObject) athleteContainer.get("athlete");
                    JSONArray stats = (JSONArray) athleteContainer.get("stats");

                    if (athlete == null || stats == null) continue;

                    Label playerName = new Label((String) athlete.get("displayName"));
                    playerName.getStyleClass().add("stat-table-player");
                    table.add(playerName, 0, rowIdx);

                    for (int i = 0; i < stats.size(); i++) {
                        if (i < labels.size()) {
                            Label statLabel = new Label(stats.get(i).toString());
                            statLabel.getStyleClass().add("stat-table-cell");
                            table.add(statLabel, i + 1, rowIdx);
                        }
                    }
                    rowIdx++;
                }
            }
        }
    }

    private VBox createPlayerStatContainer(String teamName, String teamColor) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-radius: 5; -fx-border-width: 0 0 0 8; -fx-border-color: transparent transparent transparent " + teamColor + ";");
        container.getStyleClass().add("player-stat-box");
        Label title = new Label(teamName);
        title.getStyleClass().add("section-title");
        container.getChildren().add(title);
        return container;
    }

    private GridPane createPlayerStatTable(VBox container, String groupName, JSONArray labels) {
        Label groupLabel = new Label(groupName);
        groupLabel.getStyleClass().add("stat-table-title");
        
        GridPane table = new GridPane();
        table.setHgap(10);
        table.setVgap(0);
        table.getStyleClass().add("stat-table");

        Label playerHeader = new Label("Player");
        playerHeader.getStyleClass().add("stat-table-header");
        table.add(playerHeader, 0, 0);
        for (int i = 0; i < labels.size(); i++) {
            Label header = new Label((String) labels.get(i));
            header.getStyleClass().add("stat-table-header");
            table.add(header, i + 1, 0);
        }

        container.getChildren().addAll(groupLabel, table);
        return table;
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(previousScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatGameDate(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) {
            return "Date TBD";
        }
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString).withZoneSameInstant(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return formatter.format(zonedDateTime);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }
} 