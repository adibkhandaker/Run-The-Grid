package org.example.nfldata;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TradeSimulatorController implements Initializable {

    @FXML private ComboBox<String> teamASelector;
    @FXML private Label teamAName;
    @FXML private ComboBox<String> teamAPlayerSelector;
    @FXML private Button addPlayerTeamAButton;
    @FXML private ListView<String> teamAOfferList;
    @FXML private ComboBox<String> teamAPickSelector;
    @FXML private Button addPickTeamAButton;
    @FXML private Label teamAValueLabel;

    @FXML private ComboBox<String> teamBSelector;
    @FXML private Label teamBName;
    @FXML private ComboBox<String> teamBPlayerSelector;
    @FXML private Button addPlayerTeamBButton;
    @FXML private ListView<String> teamBOfferList;
    @FXML private ComboBox<String> teamBPickSelector;
    @FXML private Button addPickTeamBButton;
    @FXML private Label teamBValueLabel;

    @FXML private ProgressBar tradeInterestBar;
    @FXML private Button proposeTradeButton;
    @FXML private Label tradeStatusLabel;

    @FXML private Button resetButton;
    @FXML private Button backButton;

    private Scene previousScene;
    private Map<String, String> teamNameToIdMap = new HashMap<>();
    private Map<String, JSONObject> teamAPlayerMap = new HashMap<>();
    private Map<String, JSONObject> teamBPlayerMap = new HashMap<>();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateTeamSelectors();
        populatePickSelectors();
        setControlsDisabled(true, true); 

        teamASelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean disabled = (newVal == null);
            setControlsDisabled(disabled, true);
            if (!disabled) {
                teamAName.setText(newVal);
                String teamId = teamNameToIdMap.get(newVal);
                loadRosterIntoSelector(teamId, teamAPlayerSelector, teamAPlayerMap);
            }
            updateProposeButtonState();
        });

        teamBSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean disabled = (newVal == null);
            setControlsDisabled(disabled, false);
            if (!disabled) {
                teamBName.setText(newVal);
                String teamId = teamNameToIdMap.get(newVal);
                loadRosterIntoSelector(teamId, teamBPlayerSelector, teamBPlayerMap);
            }
            updateProposeButtonState();
        });

        teamAOfferList.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends String> c) -> {
            updateTradeValues();
        });
        teamBOfferList.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends String> c) -> {
            updateTradeValues();
        });
    }

    private void populatePickSelectors() {
        List<String> rounds = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            rounds.add("Round " + i);
        }
        ObservableList<String> pickItems = FXCollections.observableArrayList(rounds);
        teamAPickSelector.setItems(pickItems);
        teamBPickSelector.setItems(pickItems);
    }

    private void populateTeamSelectors() {
        new Thread(() -> {
            try {
                JSONObject teamsData = APIController.getTeams();
                JSONObject sports = (JSONObject) ((JSONArray) teamsData.get("sports")).get(0);
                JSONObject leagues = (JSONObject) ((JSONArray) sports.get("leagues")).get(0);
                JSONArray teams = (JSONArray) leagues.get("teams");

                List<String> teamNames = new ArrayList<>();
                for (Object teamObj : teams) {
                    JSONObject teamWrapper = (JSONObject) teamObj;
                    JSONObject team = (JSONObject) teamWrapper.get("team");
                    String displayName = (String) team.get("displayName");
                    String id = (String) team.get("id");
                    teamNames.add(displayName);
                    teamNameToIdMap.put(displayName, id);
                }
                teamNames.sort(String::compareTo);

                Platform.runLater(() -> {
                    ObservableList<String> items = FXCollections.observableArrayList(teamNames);
                    teamASelector.setItems(items);
                    teamBSelector.setItems(items);
                });

            } catch (IOException | ParseException e) {
                e.printStackTrace();
                Platform.runLater(() -> tradeStatusLabel.setText("Failed to load teams."));
            }
        }).start();
    }

    private void loadRosterIntoSelector(String teamId, ComboBox<String> playerSelector, Map<String, JSONObject> playerMap) {
        playerSelector.setPromptText("Loading Roster...");
        playerMap.clear();

        new Thread(() -> {
            try {
                System.out.println("Loading roster for team ID: " + teamId);
                JSONObject rosterData = APIController.getRoster(teamId);
                System.out.println("Roster API response: " + (rosterData != null ? rosterData.toJSONString() : "null"));
                
                if (rosterData == null || !rosterData.containsKey("athletes")) {
                    System.out.println("No roster data or athletes found");
                    Platform.runLater(() -> playerSelector.setPromptText("Roster not available."));
                    return;
                }

                List<String> playerNames = new ArrayList<>();
                JSONArray groups = (JSONArray) rosterData.get("athletes");
                System.out.println("Found " + (groups != null ? groups.size() : 0) + " groups");

                if (groups != null) {
                    for (Object groupObj : groups) {
                        JSONObject group = (JSONObject) groupObj;
                        JSONArray items = (JSONArray) group.get("items");
                        if (items != null) {
                            for (Object playerObj : items) {
                                JSONObject player = (JSONObject) playerObj;
                                if (player.containsKey("displayName")) {
                                    String playerName = (String) player.get("displayName");
                                    playerNames.add(playerName);
                                    playerMap.put(playerName, player);
                                }
                            }
                        }
                    }
                }

                System.out.println("Processed " + playerNames.size() + " player names");

                Collections.sort(playerNames);

                Platform.runLater(() -> {
                    playerSelector.getItems().setAll(playerNames);
                    playerSelector.setPromptText("Select Player");
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> playerSelector.setPromptText("Failed to load roster."));
            }
        }).start();
    }
    
    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    @FXML
    private void addPlayerToTeamA(ActionEvent event) {
        addPlayerToOffer(teamAPlayerSelector, teamAOfferList);
    }

    @FXML
    private void addPickToTeamA(ActionEvent event) {
        addPickToOffer(teamAPickSelector, teamAOfferList);
    }

    @FXML
    private void addPlayerToTeamB(ActionEvent event) {
        addPlayerToOffer(teamBPlayerSelector, teamBOfferList);
    }

    private void addPlayerToOffer(ComboBox<String> selector, ListView<String> offerList) {
        String selectedPlayer = selector.getValue();
        if (selectedPlayer != null && !selectedPlayer.isEmpty() && !offerList.getItems().contains(selectedPlayer)) {
            offerList.getItems().add(selectedPlayer);
        }
    }

    @FXML
    private void addPickToTeamB(ActionEvent event) {
        addPickToOffer(teamBPickSelector, teamBOfferList);
    }

    private void addPickToOffer(ComboBox<String> selector, ListView<String> offerList) {
        String selectedRound = selector.getValue();
        if (selectedRound != null && !selectedRound.isEmpty()) {
            String pickDescription = "2025 " + selectedRound + " Pick";
            if (!offerList.getItems().contains(pickDescription)) {
                offerList.getItems().add(pickDescription);
            }
        }
    }

    @FXML
    private void proposeTrade(ActionEvent event) {
        if (teamASelector.getValue() == null || teamBSelector.getValue() == null) {
            tradeStatusLabel.setText("Please select both teams before proposing a trade.");
            tradeStatusLabel.setStyle("-fx-text-fill: #ff8e8e;");  
            return;
        }

        int teamAValue = parseOfferListValue(teamAOfferList, teamAPlayerMap);
        int teamBValue = parseOfferListValue(teamBOfferList, teamBPlayerMap);

        boolean tradeAccepted = teamAValue >= teamBValue * 0.95;

        if (tradeAccepted) {
            tradeStatusLabel.setText("Trade Accepted!");
            tradeStatusLabel.setStyle("-fx-text-fill: #8eff8e;");  
        } else {
            tradeStatusLabel.setText("Trade Rejected!");
            tradeStatusLabel.setStyle("-fx-text-fill: #ff8e8e;"); 
        }
    }

    @FXML
    private void resetTrade(ActionEvent event) {
        teamAOfferList.getItems().clear();
        teamBOfferList.getItems().clear();
        tradeStatusLabel.setText("Awaiting Offer...");
        tradeStatusLabel.setStyle("-fx-text-fill: #ffc107;");
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        if (previousScene != null) {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(previousScene);
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        }
    }

    private int parseOfferListValue(ListView<String> offerList, Map<String, JSONObject> playerMap) {
        int totalValue = 0;
        for (String item : offerList.getItems()) {
            if (item.contains("Pick")) {
                totalValue += parsePickValue(item);
            } else {
                JSONObject player = playerMap.get(item);
                totalValue += TradeValueCalculator.calculatePlayerValue(player);
            }
        }
        return totalValue;
    }

    private void updateTradeValues() {
        int teamAValue = parseOfferListValue(teamAOfferList, teamAPlayerMap);
        teamAValueLabel.setText(String.valueOf(teamAValue));

        int teamBValue = parseOfferListValue(teamBOfferList, teamBPlayerMap);
        teamBValueLabel.setText(String.valueOf(teamBValue));

        updateTradeInterest(teamAValue, teamBValue);
    }
    
    private int parsePickValue(String pickDescription) {
        try {
            String[] parts = pickDescription.split(" "); 
            int round = Integer.parseInt(parts[2]);
            int estimatedPickNumber = (round - 1) * 32 + 16;
            return TradeValueCalculator.calculatePickValue(estimatedPickNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateTradeInterest(int teamAValue, int teamBValue) {
        if (teamAValue == 0 && teamBValue == 0) {
            tradeInterestBar.setProgress(0.0);
            return;
        }
        
        double interest = (double) teamAValue / (double) (teamAValue + teamBValue);
        tradeInterestBar.setProgress(interest);
    }

    private void setControlsDisabled(boolean disabled, boolean isTeamA) {
        if (isTeamA) {
            addPlayerTeamAButton.setDisable(disabled);
            addPickTeamAButton.setDisable(disabled);
        } else {
            addPlayerTeamBButton.setDisable(disabled);
            addPickTeamBButton.setDisable(disabled);
        }
    }
    
    private void updateProposeButtonState() {
        boolean bothTeamsSelected = teamASelector.getValue() != null && teamBSelector.getValue() != null;
        proposeTradeButton.setDisable(!bothTeamsSelected);
    }
} 