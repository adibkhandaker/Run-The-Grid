package org.example.nfldata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class DepthChartController implements Initializable {

    @FXML
    private TableView<DepthChartPlayer> depthChartTable;

    private int teamID;
    private JSONObject team;

    @FXML
    private ImageView teamLogo;

    @FXML
    private Button backButton;

    @FXML
    private Label teamName;

    @FXML
    private TableColumn<DepthChartPlayer, String> positionColumn;

    @FXML
    private TableColumn<DepthChartPlayer, String> playersColumn;

    @FXML
    private TableColumn<DepthChartPlayer, String> rankColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        depthChartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rankColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getRank())));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        playersColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    public void setTeam(JSONObject team) {
        this.team = team;
        this.teamID = Integer.parseInt((String) team.get("id"));
        teamName.setText((String) team.get("displayName") + " Depth Chart");
        getDepthChartData();
    }

    public void getDepthChartData() {
        getTeamImage();
        String url = "https://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/2021/teams/" + teamID + "/depthcharts";
        try {
            ObservableList<DepthChartPlayer> data = FXCollections.observableArrayList();
            HttpURLConnection apiConnection = APIController.fetchAPIResponse(url);
            String JSONResponse = APIController.readAPIResponse(apiConnection);
            JSONParser parser = new JSONParser();
            JSONObject results = (JSONObject) parser.parse(JSONResponse);
            JSONArray items = (JSONArray) results.get("items");
            for (Object item : items) {
                JSONObject itemObj = (JSONObject) item;
                JSONObject positions = (JSONObject) itemObj.get("positions");
                for (Object position : positions.keySet()) {
                    String posKey = (String) position;
                    JSONObject posObj = (JSONObject) positions.get(posKey);
                    JSONObject posDetail = (JSONObject) posObj.get("position");
                    JSONArray athletes = (JSONArray) posObj.get("athletes");
                    for (Object athlete : athletes) {
                        JSONObject athleteObj = (JSONObject) athlete;
                        JSONObject athleteDetail = (JSONObject) athleteObj.get("athlete");

                        String athleteAPI = (String) athleteDetail.get("$ref");
                        HttpURLConnection URLConnection = APIController.fetchAPIResponse(athleteAPI);
                        String response = APIController.readAPIResponse(URLConnection);
                        JSONObject root = (JSONObject) parser.parse(response);
                        String fullName = (String) root.get("fullName");
                        Double weight = (Double) root.get("weight");
                        Long age = (Long) root.get("age");
                        String DOB = (String) root.get("dateOfBirth");
                        Long debut = (Long) root.get("debutYear");
                        if (age == null) {
                            age = 0L;
                        }
                        if (debut == null) {
                            debut = 0L;
                        }
                        if (DOB == null) {
                            DOB = "x";
                        }
                        Long rank = (Long) athleteObj.get("rank");
                        DepthChartPlayer player = null;
                        if (DOB.equals("x")) {
                            player = new DepthChartPlayer(fullName, age, weight, posKey.toUpperCase(), rank, DOB, debut);
                        } else {
                            player = new DepthChartPlayer(fullName, age, weight, posKey.toUpperCase(), rank, DOB.substring(0,10), debut);
                        }
                        data.add(player);
                    }
                }
            }
            depthChartTable.setItems(data);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTeamImage() {
        String teamUrl = "http://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/2025/teams/" + teamID + "?lang=en&region=us";
        try {
            HttpURLConnection apiConnection = APIController.fetchAPIResponse(teamUrl);
            String JSONResponse = APIController.readAPIResponse(apiConnection);
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(JSONResponse);
            JSONArray logos = (JSONArray) root.get("logos");
            JSONObject normalLogo = (JSONObject) logos.get(0);
            String imageLink = (String) normalLogo.get("href");
            teamLogo.setImage(new Image(imageLink));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
