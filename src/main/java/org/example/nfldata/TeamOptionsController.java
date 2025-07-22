package org.example.nfldata;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.IOException;

public class TeamOptionsController {

    private JSONObject team;
    private int teamID;

    @FXML
    private Label teamNameLabel;

    @FXML
    private Button backButton;

    public void setTeam(JSONObject team) {
        this.team = team;
        this.teamID = Integer.parseInt((String) team.get("id"));
        teamNameLabel.setText((String) team.get("displayName"));
    }

    public void depthChart(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DepthChart.fxml"));
        Parent root = loader.load();
        DepthChartController controller = loader.getController();
        controller.setTeam(team);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public void schedule(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamSchedule.fxml"));
        Parent root = loader.load();
        TeamScheduleController controller = loader.getController();
        controller.setTeam(team);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public void draftHistory(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamDraft.fxml"));
        Parent root = loader.load();
        TeamDraftController controller = loader.getController();
        controller.setTeam(team);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public void teamStats(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamStats.fxml"));
        Parent root = loader.load();
        TeamStatsController controller = loader.getController();

        String logoUrl = "";
        if (team.containsKey("logos")) {
            org.json.simple.JSONArray logos = (org.json.simple.JSONArray) team.get("logos");
            if (!logos.isEmpty()) {
                JSONObject logo = (JSONObject) logos.get(0);
                logoUrl = (String) logo.get("href");
            }
        }

        controller.setTeamInfo(String.valueOf(teamID), (String) team.get("displayName"), logoUrl);
        controller.setPreviousScene(((Node) event.getSource()).getScene());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void goBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamSelectionOne.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
