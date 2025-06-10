package org.example.nfldata;


import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TeamOptionsController {

    private int teamID;

    public void depthChart(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DepthChart.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        DepthChartController controller = loader.getController();
        controller.setTeamID(teamID);
        controller.getDepthChartData(event);
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public void schedule(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Schedule.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        TeamScheduleController controller = loader.getController();
        controller.setTeamID(teamID);
        controller.getSchedule();
    }
}
