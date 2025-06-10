package org.example.nfldata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class DepthChartController implements Initializable {

    @FXML
    private TableView<DepthChartPlayer> depthTable;

    @FXML
    private TableColumn<DepthChartPlayer, String> name;

    @FXML
    private TableColumn<DepthChartPlayer, Long> age;

    @FXML
    private TableColumn<DepthChartPlayer, Double> weight;

    @FXML
    private TableColumn<DepthChartPlayer, String> position;

    @FXML
    private TableColumn<DepthChartPlayer, Long> rank;

    @FXML
    private TableColumn<DepthChartPlayer, String> birth;

    @FXML
    private TableColumn<DepthChartPlayer, Long> debutYear;

    private int teamID;

    @FXML
    private ImageView teamLogo;

    public void getDepthChartData(ActionEvent event) {
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
                    System.out.println((String) posDetail.get("name"));
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
                        System.out.println(fullName + " " + weight + " " + age);
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
                        System.out.println(rank);
                    }
                }
            }
            depthTable.setItems(data);


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

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        age.setCellValueFactory(new PropertyValueFactory<>("age"));
        weight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        position.setCellValueFactory(new PropertyValueFactory<>("position"));
        rank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        birth.setCellValueFactory(new PropertyValueFactory<>("DOB"));
        debutYear.setCellValueFactory(new PropertyValueFactory<>("debut"));
        depthTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
