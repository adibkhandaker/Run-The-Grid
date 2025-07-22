package org.example.nfldata;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class MainMenuController implements Initializable {
    @FXML
    private Label welcomeText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void draftByYear(ActionEvent event) {
        loadScene("DraftByYear.fxml", event);
    }

    public void teamSelection(ActionEvent event) {
        loadScene("TeamSelectionOne.fxml", event);
    }

    public void latestNews(ActionEvent event) {
        loadScene("LatestNews.fxml", event);
    }

    public void scheduleVisualizer(ActionEvent event) {
        loadScene("Schedule.fxml", event);
    }

    @FXML
    public void openTradeSimulator(ActionEvent event) {
        try {
            Scene currentScene = ((Node) event.getSource()).getScene();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TradeSimulator.fxml"));
            Parent root = loader.load();

            TradeSimulatorController controller = loader.getController();
            controller.setPreviousScene(currentScene);

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openNFLRecords(ActionEvent event) {
        loadScene("NFLRecords.fxml", event);
    }

    @FXML
    public void openMatchupPredictor(ActionEvent event) {
        try {
            Scene currentScene = ((Node) event.getSource()).getScene();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MatchupPredictor.fxml"));
            Parent root = loader.load();

            MatchupPredictorController controller = loader.getController();
            controller.setPreviousScene(currentScene);

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScene(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit(ActionEvent event) {
        System.exit(0);
    }
} 