package org.example.nfldata;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TeamScheduleController {

    @FXML
    private Button backButton;
    @FXML
    private Label titleLabel;
    @FXML
    private ChoiceBox<Integer> yearChoice;
    @FXML
    private TableView<ScheduleEntry> scheduleTable;
    @FXML
    private TableColumn<ScheduleEntry, String> weekColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> dateColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> opponentColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> resultColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> venueColumn;
    
    // Record labels
    @FXML
    private Label recordLabel;
    
    // View toggle
    @FXML
    private Button viewToggleButton;
    @FXML
    private Button playoffsButton;
    
    // Week view elements
    @FXML
    private VBox weekViewContainer;
    @FXML
    private ImageView teamOneLogo, teamTwoLogo;
    @FXML
    private Label teamOneName, teamTwoName;
    @FXML
    private Label teamOneRecord, teamTwoRecord;
    @FXML
    private Label vsLabel, scoreLabel, gameStatusLabel;
    @FXML
    private Label weekLabel, dateLabel, stadiumLabel;
    @FXML
    private Button prevButton, nextButton;
    @FXML
    private Label weekCounterLabel;

    private JSONObject team;
    private int teamID;
    private final ObservableList<ScheduleEntry> scheduleData = FXCollections.observableArrayList();
    private List<JSONObject> scheduleEvents = new ArrayList<>();
    private int currentEventIndex = 0;
    private boolean isTableView = true;
    
    @FXML
    public void initialize() {
        weekColumn.setCellValueFactory(cellData -> cellData.getValue().weekProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        opponentColumn.setCellValueFactory(cellData -> cellData.getValue().opponentProperty());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
        venueColumn.setCellValueFactory(cellData -> cellData.getValue().venueProperty());

        resultColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    getStyleClass().removeAll("win", "loss");
                    if (item.startsWith("W")) {
                        getStyleClass().add("win");
                    } else if (item.startsWith("L")) {
                        getStyleClass().add("loss");
                    }
                }
            }
        });

        List<Integer> years = IntStream.rangeClosed(2015, 2025).boxed().collect(Collectors.toList());
        yearChoice.setItems(FXCollections.observableArrayList(years));
        yearChoice.setValue(2024);
        yearChoice.setOnAction(event -> loadScheduleData());
        scheduleTable.setItems(scheduleData);
        
        updateViewToggle();
    }

    public void setTeam(JSONObject team) {
        this.team = team;
        this.teamID = Integer.parseInt((String) team.get("id"));
        titleLabel.setText(team.get("displayName") + " Schedule");
        loadScheduleData();
    }

    private void loadScheduleData() {
        Integer year = yearChoice.getValue();
        if (year == null) return;

        scheduleTable.setPlaceholder(new Label("Loading schedule for " + year + "..."));
        scheduleData.clear();
        scheduleEvents.clear();
        currentEventIndex = 0;

        Task<JSONObject> task = new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                // Fetch regular season
                String regularSeasonUrl = "https://site.api.espn.com/apis/site/v2/sports/football/nfl/teams/" + teamID + "/schedule?season=" + year + "&seasontype=2";
                String regularSeasonResponse = APIController.readAPIResponse(APIController.fetchAPIResponse(regularSeasonUrl));
                JSONObject regularSeasonRoot = (JSONObject) new JSONParser().parse(regularSeasonResponse);
                JSONArray regularSeasonEvents = (JSONArray) regularSeasonRoot.get("events");

                // Fetch post season
                String postSeasonUrl = "https://site.api.espn.com/apis/site/v2/sports/football/nfl/teams/" + teamID + "/schedule?season=" + year + "&seasontype=3";
                String postSeasonResponse = APIController.readAPIResponse(APIController.fetchAPIResponse(postSeasonUrl));
                JSONObject postSeasonRoot = (JSONObject) new JSONParser().parse(postSeasonResponse);
                JSONArray postSeasonEvents = (JSONArray) postSeasonRoot.get("events");

                // Combine events
                if (regularSeasonEvents != null && postSeasonEvents != null) {
                    regularSeasonEvents.addAll(postSeasonEvents);
                }
                
                return regularSeasonRoot; // This root contains the combined events
            }
        };

        task.setOnSucceeded(e -> {
            JSONObject root = task.getValue();
            updateTeamInfo(root);

            scheduleEvents = (JSONArray) root.get("events");
            boolean hasPlayoffs = false;
            if (scheduleEvents != null) {
                List<ScheduleEntry> entries = new ArrayList<>();
                for (JSONObject event : scheduleEvents) {
                    entries.add(createScheduleEntry(event));
                    JSONObject seasonType = (JSONObject) event.get("seasonType");
                    if (seasonType != null) {
                        String type = seasonType.get("type").toString();
                        if ("3".equals(type)) {
                            hasPlayoffs = true;
                        }
                    }
                }
                scheduleData.setAll(entries);
                
                if (scheduleData.isEmpty()) {
                    scheduleTable.setPlaceholder(new Label("No schedule data available for " + year + "."));
                } else {
                    displayCurrentEvent();
                    updateButtonStates();
                }
            }
            playoffsButton.setDisable(!hasPlayoffs);
        });

        task.setOnFailed(e -> {
            scheduleTable.setPlaceholder(new Label("Failed to load schedule data."));
            playoffsButton.setDisable(true);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }
    
    private void updateTeamInfo(JSONObject root) {
        JSONObject teamData = (JSONObject) root.get("team");
        if(teamData != null) {
            
            JSONObject recordObject = (JSONObject) teamData.get("record");
             if(recordObject != null){
                JSONArray summaries = (JSONArray) recordObject.get("items");
                 if(summaries != null){
                    for(Object summaryObj : summaries){
                        JSONObject summary = (JSONObject) summaryObj;
                        if("Overall".equalsIgnoreCase(summary.get("type").toString())){
                            recordLabel.setText("Record: " + summary.get("summary"));
                            break;
                        }
                    }
                 }
            } else {
                 recordLabel.setText("Record: N/A");
            }
        }
    }


    private ScheduleEntry createScheduleEntry(JSONObject event) {
        JSONArray competitions = (JSONArray) event.get("competitions");
        JSONObject competition = (JSONObject) competitions.get(0);
        JSONArray competitors = (JSONArray) competition.get("competitors");

        JSONObject competitor1 = (JSONObject) competitors.get(0);
        JSONObject competitor2 = (JSONObject) competitors.get(1);

        JSONObject team1 = (JSONObject) competitor1.get("team");
        JSONObject team2 = (JSONObject) competitor2.get("team");

        String opponentName;
        String result;
        String homeAway;

        if (String.valueOf(teamID).equals(team1.get("id"))) {
            opponentName = (String) team2.get("displayName");
            homeAway = "vs.";
        } else {
            opponentName = (String) team1.get("displayName");
            homeAway = "@";
        }
        opponentName = homeAway + " " + opponentName;

        if (competitor1.get("score") != null && competitor2.get("score") != null) {
            String score1 = (String) ((JSONObject) competitor1.get("score")).get("displayValue");
            String score2 = (String) ((JSONObject) competitor2.get("score")).get("displayValue");
            int s1 = Integer.parseInt(score1);
            int s2 = Integer.parseInt(score2);
            String winner = "L";
            if (String.valueOf(teamID).equals(team1.get("id"))) {
                if (s1 > s2) winner = "W";
            } else {
                if (s2 > s1) winner = "W";
            }
            result = winner + " " + score1 + "-" + score2;
        } else {
            result = "N/A";
        }

        JSONObject weekObj = (JSONObject) event.get("week");
        String week = weekObj != null ? weekObj.get("text").toString() : "N/A";
        String date = (String) event.get("date");
        JSONObject venueObj = (JSONObject) competition.get("venue");
        String venue = venueObj != null ? (String) venueObj.get("fullName") : "N/A";

        return new ScheduleEntry(week, date.substring(0, 10), opponentName, result, venue);
    }

    private void displayCurrentEvent() {
        if (scheduleEvents == null || currentEventIndex < 0 || currentEventIndex >= scheduleEvents.size()) {
            return;
        }

        JSONObject event = scheduleEvents.get(currentEventIndex);
        Platform.runLater(() -> {
            try {
                String date = (String) event.get("date");
                dateLabel.setText(date.substring(0, 10));

                JSONObject week = (JSONObject) event.get("week");
                weekLabel.setText((String) week.get("text"));

                JSONArray competitions = (JSONArray) event.get("competitions");
                JSONObject mainInfo = (JSONObject) competitions.get(0);

                JSONObject venue = (JSONObject) mainInfo.get("venue");
                stadiumLabel.setText((String) venue.get("fullName"));

                JSONArray competitors = (JSONArray) mainInfo.get("competitors");
                updateCompetitorInfo((JSONObject) competitors.get(0), teamOneName, teamOneLogo, teamOneRecord);
                updateCompetitorInfo((JSONObject) competitors.get(1), teamTwoName, teamTwoLogo, teamTwoRecord);

                // Update score and status
                if (competitors.get(0) != null && competitors.get(1) != null) {
                    JSONObject comp1 = (JSONObject) competitors.get(0);
                    JSONObject comp2 = (JSONObject) competitors.get(1);
                    
                    if (comp1.get("score") != null && comp2.get("score") != null) {
                        String score1 = (String) ((JSONObject) comp1.get("score")).get("displayValue");
                        String score2 = (String) ((JSONObject) comp2.get("score")).get("displayValue");
                        scoreLabel.setText(score1 + " - " + score2);
                        gameStatusLabel.setText("Final");
                        vsLabel.setVisible(false);
                    } else {
                        scoreLabel.setText("TBD");
                        gameStatusLabel.setText("Scheduled");
                        vsLabel.setVisible(true);
                    }
                }

                weekCounterLabel.setText((currentEventIndex + 1) + " of " + scheduleEvents.size());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateCompetitorInfo(JSONObject competitor, Label nameLabel, ImageView logoView, Label recordLabel) {
        JSONObject team = (JSONObject) competitor.get("team");
        nameLabel.setText((String) team.get("displayName"));

        JSONArray logos = (JSONArray) team.get("logos");
        if (logos != null && !logos.isEmpty()) {
            JSONObject logo = (JSONObject) logos.get(0);
            logoView.setImage(new Image((String) logo.get("href")));
        }

        Object recordObj = competitor.get("record");
        if (recordObj != null) {
            String recordText = "(0-0)";
            if (recordObj instanceof JSONObject) {
                JSONObject record = (JSONObject) recordObj;
                recordText = "(" + record.get("displayValue") + ")";
            } else if (recordObj instanceof JSONArray) {
                JSONArray record = (JSONArray) recordObj;
                if (!record.isEmpty()) {
                    JSONObject recordItem = (JSONObject) record.get(0);
                    recordText = "(" + recordItem.get("displayValue") + ")";
                }
            }
            recordLabel.setText(recordText);
        } else {
            recordLabel.setText("(0-0)");
        }
    }

    @FXML
    private void toggleView() {
        isTableView = !isTableView;
        scheduleTable.setVisible(isTableView);
        weekViewContainer.setVisible(!isTableView);
        updateViewToggle();
        
        if (!isTableView && !scheduleEvents.isEmpty()) {
            displayCurrentEvent();
        }
    }

    private void updateViewToggle() {
        if (isTableView) {
            viewToggleButton.setText("Switch to Week View");
        } else {
            viewToggleButton.setText("Switch to Table View");
        }
    }

    @FXML
    private void viewPlayoffs() {
        int playoffIndex = -1;
        for (int i = 0; i < scheduleEvents.size(); i++) {
            JSONObject event = scheduleEvents.get(i);
            JSONObject seasonType = (JSONObject) event.get("seasonType");
            if (seasonType != null) {
                String type = seasonType.get("type").toString();
                if ("3".equals(type)) {
                    playoffIndex = i;
                    break;
                }
            }
        }
        
        if (playoffIndex >= 0) {
            currentEventIndex = playoffIndex;
            isTableView = false;
            scheduleTable.setVisible(false);
            weekViewContainer.setVisible(true);
            displayCurrentEvent();
            updateViewToggle();
            updateButtonStates();
        } else {
            System.out.println("No playoff games found for this team/season");
        }
    }

    @FXML
    private void nextWeek() {
        if (currentEventIndex < scheduleEvents.size() - 1) {
            currentEventIndex++;
            displayCurrentEvent();
            updateButtonStates();
        }
    }

    @FXML
    private void prevWeek() {
        if (currentEventIndex > 0) {
            currentEventIndex--;
            displayCurrentEvent();
            updateButtonStates();
        }
    }

    private void updateButtonStates() {
        prevButton.setDisable(currentEventIndex <= 0);
        nextButton.setDisable(currentEventIndex >= scheduleEvents.size() - 1);
    }

    @FXML
    private void goBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamOptions.fxml"));
        Parent root = loader.load();
        TeamOptionsController controller = loader.getController();
        controller.setTeam(team);
        Scene scene = new Scene(root);
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    private void viewTeamStats() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamStats.fxml"));
        Parent root = loader.load();
        TeamStatsController controller = loader.getController();

        String logoUrl = "";
        if (team.containsKey("logos")) {
            org.json.simple.JSONArray logos = (org.json.simple.JSONArray) team.get("logos");
            if (logos != null && !logos.isEmpty()) {
                JSONObject logo = (JSONObject) logos.get(0);
                if (logo != null && logo.containsKey("href")) {
                    logoUrl = (String) logo.get("href");
                }
            }
        }
        
        // Pass team information
        controller.setTeamInfo(String.valueOf(teamID), (String) team.get("displayName"), logoUrl);
        controller.setPreviousScene(backButton.getScene());
        
        Scene scene = new Scene(root);
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(scene);
    }

    public static class ScheduleEntry {
        private final SimpleStringProperty week;
        private final SimpleStringProperty date;
        private final SimpleStringProperty opponent;
        private final SimpleStringProperty result;
        private final SimpleStringProperty venue;

        public ScheduleEntry(String week, String date, String opponent, String result, String venue) {
            this.week = new SimpleStringProperty(week);
            this.date = new SimpleStringProperty(date);
            this.opponent = new SimpleStringProperty(opponent);
            this.result = new SimpleStringProperty(result);
            this.venue = new SimpleStringProperty(venue);
        }

        public String getWeek() { return week.get(); }
        public SimpleStringProperty weekProperty() { return week; }
        public String getDate() { return date.get(); }
        public SimpleStringProperty dateProperty() { return date; }
        public String getOpponent() { return opponent.get(); }
        public SimpleStringProperty opponentProperty() { return opponent; }
        public String getResult() { return result.get(); }
        public SimpleStringProperty resultProperty() { return result; }
        public String getVenue() { return venue.get(); }
        public SimpleStringProperty venueProperty() { return venue; }
    }
}