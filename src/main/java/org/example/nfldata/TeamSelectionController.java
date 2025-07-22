package org.example.nfldata;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class TeamSelectionController {

    @FXML
    private TilePane afcTeamContainer;

    @FXML
    private TilePane nfcTeamContainer;

    @FXML
    private Button backButton;

    private JSONArray teams;

    private static final Map<String, String> TEAM_CONFERENCE_MAP = new HashMap<>();
    
    static {
        TEAM_CONFERENCE_MAP.put("2", "1");   // Buffalo Bills
        TEAM_CONFERENCE_MAP.put("33", "1");  // Baltimore Ravens
        TEAM_CONFERENCE_MAP.put("4", "1");   // Cincinnati Bengals
        TEAM_CONFERENCE_MAP.put("5", "1");   // Cleveland Browns
        TEAM_CONFERENCE_MAP.put("7", "1");   // Denver Broncos
        TEAM_CONFERENCE_MAP.put("34", "1");  // Houston Texans
        TEAM_CONFERENCE_MAP.put("11", "1");  // Indianapolis Colts
        TEAM_CONFERENCE_MAP.put("30", "1");  // Jacksonville Jaguars
        TEAM_CONFERENCE_MAP.put("12", "1");  // Kansas City Chiefs
        TEAM_CONFERENCE_MAP.put("13", "1");  // Las Vegas Raiders
        TEAM_CONFERENCE_MAP.put("24", "1");  // Los Angeles Chargers
        TEAM_CONFERENCE_MAP.put("15", "1");  // Miami Dolphins
        TEAM_CONFERENCE_MAP.put("17", "1");  // New England Patriots
        TEAM_CONFERENCE_MAP.put("20", "1");  // New York Jets
        TEAM_CONFERENCE_MAP.put("23", "1");  // Pittsburgh Steelers
        TEAM_CONFERENCE_MAP.put("10", "1");  // Tennessee Titans
        
        
        TEAM_CONFERENCE_MAP.put("22", "2");  // Arizona Cardinals
        TEAM_CONFERENCE_MAP.put("1", "2");   // Atlanta Falcons
        TEAM_CONFERENCE_MAP.put("3", "2");   // Chicago Bears
        TEAM_CONFERENCE_MAP.put("6", "2");   // Dallas Cowboys
        TEAM_CONFERENCE_MAP.put("8", "2");   // Detroit Lions
        TEAM_CONFERENCE_MAP.put("9", "2");   // Green Bay Packers
        TEAM_CONFERENCE_MAP.put("14", "2");  // Los Angeles Rams
        TEAM_CONFERENCE_MAP.put("16", "2");  // Minnesota Vikings
        TEAM_CONFERENCE_MAP.put("18", "2");  // New Orleans Saints
        TEAM_CONFERENCE_MAP.put("19", "2");  // New York Giants
        TEAM_CONFERENCE_MAP.put("21", "2");  // Philadelphia Eagles
        TEAM_CONFERENCE_MAP.put("25", "2");  // San Francisco 49ers
        TEAM_CONFERENCE_MAP.put("26", "2");  // Seattle Seahawks
        TEAM_CONFERENCE_MAP.put("27", "2");  // Tampa Bay Buccaneers
        TEAM_CONFERENCE_MAP.put("28", "2");  // Washington Commanders
        TEAM_CONFERENCE_MAP.put("29", "2");  // Carolina Panthers
    }

    @FXML
    public void initialize() {
        loadTeams();
        if (teams != null) {
            for (Object obj : teams) {
                JSONObject teamContainer = (JSONObject) obj;
                JSONObject team = (JSONObject) teamContainer.get("team");
                if (team == null) {
                    continue;
                }

                String teamId = (String) team.get("id");
                String conferenceId = TEAM_CONFERENCE_MAP.get(teamId);

                if (conferenceId != null) {
                    VBox teamBox = createTeamBox(team);
                    if (conferenceId.equals("1")) { 
                        afcTeamContainer.getChildren().add(teamBox);
                    } else if (conferenceId.equals("2")) {
                        nfcTeamContainer.getChildren().add(teamBox);
                    }
                }
            }
        }
    }

    private VBox createTeamBox(JSONObject team) {
        String teamId = (String) team.get("id");
        String teamName = (String) team.get("displayName");
        JSONArray logos = (JSONArray) team.get("logos");
        String logoUrl = "";
        if (logos != null && !logos.isEmpty()) {
            JSONObject logo = (JSONObject) logos.get(0);
            logoUrl = (String) logo.get("href");
        }

        ImageView teamLogo = new ImageView(new Image(logoUrl, 100, 100, true, true));
        teamLogo.setFitHeight(100);
        teamLogo.setFitWidth(100);
        teamLogo.setPreserveRatio(true);

        Label teamLabel = new Label(teamName);
        teamLabel.getStyleClass().add("team-label-selection");
        teamLabel.setAlignment(javafx.geometry.Pos.CENTER);
        teamLabel.setWrapText(true);
        teamLabel.setMaxWidth(150);

        VBox teamBox = new VBox(10, teamLogo, teamLabel);
        teamBox.setAlignment(javafx.geometry.Pos.CENTER);
        teamBox.getStyleClass().add("team-box");
        teamBox.setOnMouseClicked(event -> handleTeamSelection(teamId));
        return teamBox;
    }

    private void loadTeams() {
        try {
            HttpURLConnection connection = APIController.fetchAPIResponse("https://site.api.espn.com/apis/site/v2/sports/football/nfl/teams?limit=32");
            if (connection != null && connection.getResponseCode() == 200) {
                String response = APIController.readAPIResponse(connection);
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(response);
                JSONArray sports = (JSONArray) jsonObject.get("sports");
                if (sports.isEmpty()) return;
                JSONObject sport = (JSONObject) sports.get(0);
                JSONArray leagues = (JSONArray) sport.get("leagues");
                if (leagues.isEmpty()) return;
                JSONObject league = (JSONObject) leagues.get(0);
                this.teams = (JSONArray) league.get("teams");
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void handleTeamSelection(String teamIdStr) {
        JSONObject selectedTeam = null;
        if (teams != null) {
            for (Object obj : teams) {
                JSONObject teamContainer = (JSONObject) obj;
                JSONObject team = (JSONObject) teamContainer.get("team");
                if (team.get("id").equals(teamIdStr)) {
                    selectedTeam = team;
                    break;
                }
            }
        }

        if (selectedTeam != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamOptions.fxml"));
                Parent root = loader.load();
                TeamOptionsController controller = loader.getController();
                controller.setTeam(selectedTeam);
                Stage stage = (Stage) afcTeamContainer.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
