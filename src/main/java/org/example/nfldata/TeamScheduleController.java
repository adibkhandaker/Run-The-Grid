package org.example.nfldata;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class TeamScheduleController implements Initializable {

    @FXML
    private Label teamOneName;

    @FXML
    private Label teamTwoName;

    @FXML
    private Label teamOneABBLabel;

    @FXML
    private Label teamTwoABBLabel;

    @FXML
    private Label weekLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label shortNameLabel;

    @FXML
    private Label seasonTypeLabel;

    @FXML
    private Label stadiumLabel;

    @FXML
    private ImageView teamOneLogo;

    @FXML
    private ImageView teamTwoLogo;

    @FXML
    private Button nextButton;

    @FXML
    private Button prevButton;

    @FXML
    private ChoiceBox<Integer> yearChoice;

    private int teamID;

    private int week = 0;
    private int year;

    public void getSchedule() {
        String teamOne = "";
        String teamTwo = "";
        String teamOneABB = "";
        String teamTwoABB = "";
        try {
            String url = "https://site.api.espn.com/apis/site/v2/sports/football/nfl/teams/" + teamID + "/schedule?season=" + year;
            HttpURLConnection connection = APIController.fetchAPIResponse(url);
            String response = APIController.readAPIResponse(connection);
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(response);
            JSONArray events = (JSONArray) root.get("events");
            JSONObject weekOne = (JSONObject) events.get(week);
            String date = (String) weekOne.get("date");
            date = date.substring(0, 10);
            dateLabel.setText(date);

            String shortName = (String) weekOne.get("shortName");
            shortNameLabel.setText(shortName);
            JSONObject seasonType = (JSONObject) weekOne.get("seasonType");
            String nameSeasonType = (String) seasonType.get("name");
            seasonTypeLabel.setText(nameSeasonType);
            JSONObject week = (JSONObject) weekOne.get("week");
            String weekName = (String) week.get("text");
            weekLabel.setText(weekName);
            JSONArray competitions = (JSONArray) weekOne.get("competitions");

            JSONObject mainInfo = (JSONObject) competitions.get(0);
            JSONObject venue = (JSONObject) mainInfo.get("venue");
            String stadium = (String) venue.get("fullName");
            stadiumLabel.setText(stadium);
            JSONArray competitors = (JSONArray) mainInfo.get("competitors");
            for (int i = 0; i < competitors.size(); i++) {
                JSONObject competitor = (JSONObject) competitors.get(i);
                JSONObject team = (JSONObject) competitor.get("team");
                JSONArray logos = (JSONArray) team.get("logos");
                JSONObject logo = (JSONObject) logos.get(0);
                String logoLink = (String) logo.get("href");
                Image image = new Image(logoLink);
                if (i == 0) {
                    teamOneABB = (String) team.get("abbreviation");
                    teamOne = (String) team.get("displayName");
                    teamOneABBLabel.setText(teamOneABB);
                    teamOneName.setText(teamOne);
                    teamOneLogo.setImage(image);
                } else {
                    teamTwoABB = (String) team.get("abbreviation");
                    teamTwo = (String) team.get("displayName");
                    teamTwoABBLabel.setText(teamTwoABB);
                    teamTwoName.setText(teamTwo);
                    teamTwoLogo.setImage(image);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void nextWeek(ActionEvent event) {
        ++week;
        getSchedule();
        checkButtons();
    }

    public void prevWeek(ActionEvent event) {
        --week;
        getSchedule();
        checkButtons();
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    private void checkButtons() {
        if (week == 16) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }

        if (week == 0) {
            prevButton.setDisable(true);
        } else {
            prevButton.setDisable(false);
        }
    }

    private void changeYear(ActionEvent event) {
        year = yearChoice.getValue();
        week = 0;
        getSchedule();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        prevButton.setDisable(true);
        for (int i = 2015; i <= 2025; ++i) {
            yearChoice.getItems().add(i);
        }
        yearChoice.setValue(2025);
        year = yearChoice.getValue();
        yearChoice.setOnAction(this::changeYear);

    }
}
