package org.example.nfldata;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class ScheduleController implements Initializable {

    @FXML
    private ChoiceBox<Integer> yearChoice;
    @FXML
    private ChoiceBox<String> seasonTypeChoice;
    @FXML
    private ChoiceBox<String> weekChoice;
    @FXML
    private Label statusLabel;
    @FXML
    private ScrollPane contentScrollPane;
    @FXML
    private FlowPane gamesPane;
    @FXML
    private Button backButton;

    private final Map<String, Image> logoCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        IntStream.rangeClosed(2002, 2025).sorted().forEach(year -> yearChoice.getItems().add(year));
        yearChoice.setValue(2025);

        seasonTypeChoice.getItems().addAll("Regular Season", "Postseason");
        seasonTypeChoice.setValue("Regular Season");

        updateWeekChoices("Regular Season");

        yearChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadSchedule());
        seasonTypeChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateWeekChoices(newVal);
        });
        weekChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadSchedule());

        Platform.runLater(this::loadSchedule);
    }

    private void updateWeekChoices(String seasonType) {
        weekChoice.getItems().clear();
        if ("Regular Season".equals(seasonType)) {
            IntStream.rangeClosed(1, 18).forEach(w -> weekChoice.getItems().add(String.valueOf(w)));
            weekChoice.setValue("1");
        } else if ("Postseason".equals(seasonType)) {
            weekChoice.getItems().addAll("Wild Card (1)", "Divisional (2)", "Conference (3)", "Super Bowl (5)");
            weekChoice.setValue("Wild Card (1)");
        }
    }

    private void loadSchedule() {
        Integer year = yearChoice.getValue();
        String seasonType = seasonTypeChoice.getValue();
        String week = weekChoice.getValue();

        if (year == null || seasonType == null || week == null) {
            return;
        }

        statusLabel.setText("Loading schedule for " + year + " " + seasonType + " Week " + week + "...");
        statusLabel.setVisible(true);
        gamesPane.getChildren().clear();

        Task<List<VBox>> task = new Task<>() {
            @Override
            protected List<VBox> call() throws Exception {
                List<VBox> gameCards = new ArrayList<>();
                String apiUrl = buildApiUrl(year, seasonType, week);
                HttpURLConnection connection = APIController.fetchAPIResponse(apiUrl);

                if (connection.getResponseCode() != 200) {
                    Platform.runLater(() -> statusLabel.setText("No schedule data found for this period."));
                    return gameCards;
                }

                String response = APIController.readAPIResponse(connection);
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(response);
                JSONArray events = (JSONArray) data.get("events");

                if (events != null) {
                    for (Object eventObj : events) {
                        gameCards.add(createGameCard((JSONObject) eventObj));
                    }
                }
                return gameCards;
            }
        };

        task.setOnSucceeded(e -> {
            gamesPane.getChildren().addAll(task.getValue());
            if (gamesPane.getChildren().isEmpty()) {
                statusLabel.setText("No games scheduled for this period.");
            } else {
                statusLabel.setVisible(false);
            }
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Failed to load schedule data.");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private String buildApiUrl(int year, String seasonType, String week) {
        int seasonTypeCode = "Regular Season".equals(seasonType) ? 2 : 3;
        String weekNumber = week.replaceAll("\\D+", ""); 
        return "https://site.api.espn.com/apis/site/v2/sports/football/nfl/scoreboard?dates=" + year + "&seasontype=" + seasonTypeCode + "&week=" + weekNumber;
    }

    private VBox createGameCard(JSONObject event) {
        JSONObject status = (JSONObject) event.get("status");
        JSONObject type = (JSONObject) status.get("type");
        String statusName = (String) type.get("name");

        String dateStr = (String) event.get("date");
        String displayDetail;

        if ("STATUS_SCHEDULED".equals(statusName)) {
            displayDetail = formatGameDate(dateStr, "EEE, MMM d 'at' h:mm a");
        } else {
            displayDetail = "Final - " + formatGameDate(dateStr, "EEE, MMM d, yyyy");
        }

        JSONArray competitions = (JSONArray) event.get("competitions");
        JSONObject competition = (JSONObject) competitions.get(0);
        JSONArray competitors = (JSONArray) competition.get("competitors");

        JSONObject competitor1 = (JSONObject) competitors.get(0);
        JSONObject competitor2 = (JSONObject) competitors.get(1);

        JSONObject homeTeamData = "home".equals(competitor1.get("homeAway")) ? competitor1 : competitor2;
        JSONObject awayTeamData = "away".equals(competitor1.get("homeAway")) ? competitor1 : competitor2;

        HBox awayTeamBox = createTeamBox(awayTeamData);
        HBox homeTeamBox = createTeamBox(homeTeamData);

        Label detailLabel = new Label(displayDetail);
        detailLabel.getStyleClass().add("game-card-status");

        VBox card = new VBox(10, awayTeamBox, homeTeamBox, detailLabel);
        card.getStyleClass().add("game-card");
        card.setAlignment(Pos.CENTER);

        card.setOnMouseClicked(e -> openBoxScore(event));
        
        return card;
    }
    
    private HBox createTeamBox(JSONObject teamData) {
        JSONObject team = (JSONObject) teamData.get("team");
        String logoUrl = (String) team.get("logo");
        String teamName = (String) team.get("displayName");
        String score = (String) teamData.get("score");

        ImageView logo = new ImageView();
        logo.setFitHeight(40);
        logo.setFitWidth(40);
        
        if (logoUrl != null && !logoUrl.isEmpty()) {
            Image teamImage = logoCache.computeIfAbsent(logoUrl, k -> new Image(k, true)); 
            logo.setImage(teamImage);
        }

        Label nameLabel = new Label(teamName);
        nameLabel.getStyleClass().add("game-card-team-name");
        
        Label scoreLabel = new Label(score != null ? score : "0");
        scoreLabel.getStyleClass().add("game-card-score");

        HBox teamBox = new HBox(10, logo, nameLabel, scoreLabel);
        teamBox.setAlignment(Pos.CENTER_LEFT);
        return teamBox;
    }

    private void openBoxScore(JSONObject event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BoxScore.fxml"));
            Parent root = loader.load();

            BoxScoreController controller = loader.getController();
            controller.setPreviousScene(gamesPane.getScene());
            controller.initialize(event); 

            Stage stage = (Stage) gamesPane.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading box score.");
            statusLabel.setVisible(true);
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