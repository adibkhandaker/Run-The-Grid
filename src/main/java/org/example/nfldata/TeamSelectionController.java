package org.example.nfldata;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class TeamSelectionController {

    public void team(ActionEvent event) throws IOException {
        Button clicked = (Button) event.getSource();
        int teamId = Integer.parseInt(clicked.getUserData().toString());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamOptions.fxml"));
        Scene scene = new Scene(loader.load());
        TeamOptionsController controller = loader.getController();
        controller.setTeamID(teamId);
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
