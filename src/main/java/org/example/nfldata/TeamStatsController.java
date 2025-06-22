package org.example.nfldata;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

public class TeamStatsController {

    @FXML
    private Button backButton;
    @FXML
    private ImageView teamLogoImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label teamNameLabel;
    @FXML
    private ChoiceBox<String> yearChoice;
    @FXML
    private Button loadStatsButton;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox statsContainer;
    
    // Grid Panes for different stat categories
    @FXML
    private GridPane kpiGrid;
    @FXML
    private GridPane offenseGrid;
    @FXML
    private GridPane defenseGrid;
    @FXML
    private GridPane specialGrid;
    
    // Containers for insights and fun facts
    @FXML
    private VBox insightsContainer;
    @FXML
    private VBox funFactsContainer;

    private String currentTeamId;
    private String currentTeamName;
    private Scene previousScene;
    private Map<String, StatData> allStats = new HashMap<>();

    @FXML
    public void initialize() {
        setupYearChoice();
        setupEventHandlers();
    }

    private void setupYearChoice() {
        List<String> years = new ArrayList<>();
        int currentYear = java.time.Year.now().getValue();
        for (int year = currentYear; year >= 2002; year--) {
            years.add(String.valueOf(year));
        }
        yearChoice.setItems(javafx.collections.FXCollections.observableArrayList(years));
        yearChoice.setValue(String.valueOf(currentYear));
    }

    private void setupEventHandlers() {
        backButton.setOnAction(event -> goBack());
        loadStatsButton.setOnAction(event -> loadTeamStats());
    }

    public void setTeamInfo(String teamId, String teamName, String logoUrl) {
        this.currentTeamId = teamId;
        this.currentTeamName = teamName;
        teamNameLabel.setText(teamName);

        if (logoUrl != null && !logoUrl.isEmpty()) {
            try {
                teamLogoImageView.setImage(new Image(logoUrl));
            } catch (Exception e) {
                System.err.println("Error loading team logo: " + e.getMessage());
            }
        }
    }

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    @FXML
    private void goBack() {
        if (previousScene != null) {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(previousScene);
        }
    }

    @FXML
    private void loadTeamStats() {
        if (currentTeamId == null || yearChoice.getValue() == null) {
            showStatus("Please select a team and year", true);
            return;
        }

        showStatus("Loading team statistics...", false);
        clearAllSections();
        
        new Thread(() -> {
            try {
                String year = yearChoice.getValue();
                String url = String.format("http://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/%s/types/2/teams/%s/statistics", year, currentTeamId);
                
                String jsonResponse = APIController.getRawJsonFromUrl(url);
                if (jsonResponse == null) {
                    Platform.runLater(() -> showStatus("Failed to load team statistics", true));
                    return;
                }

                JSONParser parser = new JSONParser();
                JSONObject statsJson = (JSONObject) parser.parse(jsonResponse);
                
                JSONObject splits = (JSONObject) statsJson.get("splits");
                JSONArray categories = (JSONArray) splits.get("categories");

                // Parse all stats into our data structure
                parseAllStats(categories);

                Platform.runLater(() -> {
                    createVisualStatsDisplay();
                    showStatus("Statistics loaded successfully", false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showStatus("Error loading statistics: " + e.getMessage(), true));
            }
        }).start();
    }

    private void parseAllStats(JSONArray categories) {
        allStats.clear();
        
        for (Object categoryObj : categories) {
            JSONObject category = (JSONObject) categoryObj;
            String categoryName = (String) category.get("name");
            JSONArray stats = (JSONArray) category.get("stats");

            for (Object statObj : stats) {
                JSONObject stat = (JSONObject) statObj;
                String displayName = (String) stat.get("displayName");
                String displayValue = (String) stat.get("displayValue");
                String rankDisplayValue = (String) stat.get("rankDisplayValue");
                
                StatData statData = new StatData(displayName, displayValue, rankDisplayValue, categoryName);
                allStats.put(displayName, statData);
            }
        }
    }

    private void createVisualStatsDisplay() {
        createKPISection();
        createInsightsSection();
        createOffenseSection();
        createDefenseSection();
        createSpecialSection();
        createFunFactsSection();
    }

    private void createKPISection() {
        kpiGrid.getChildren().clear();
        
        // Expanded key performance indicators
        String[] kpiStats = {
            "Points Per Game", "Total Yards Per Game", "Turnovers", "Sacks", "Interceptions",
            "Points Allowed Per Game", "Yards Allowed Per Game", "Third Down Conversion Percentage",
            "Red Zone Touchdown Percentage", "Time of Possession Per Game", "Penalties",
            "Passing Yards Per Game", "Rushing Yards Per Game", "Completion Percentage",
            "Passing Touchdowns", "Rushing Touchdowns", "First Downs", "Safeties",
            "Defensive Touchdowns", "Field Goal Percentage", "Punt Average"
        };
        
        int col = 0;
        int row = 0;
        for (String statName : kpiStats) {
            StatData stat = allStats.get(statName);
            if (stat != null) {
                VBox statCard = createStatCard(stat, true);
                kpiGrid.add(statCard, col, row);
                col++;
                if (col >= 4) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private void createInsightsSection() {
        insightsContainer.getChildren().clear();
        
        List<String> insights = generateInsights();
        for (String insight : insights) {
            Label insightLabel = new Label("💡 " + insight);
            insightLabel.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 14px; -fx-padding: 5px;");
            insightLabel.setWrapText(true);
            insightsContainer.getChildren().add(insightLabel);
        }
    }

    private void createOffenseSection() {
        offenseGrid.getChildren().clear();
        
        // Expanded offensive stats
        String[] offenseStats = {
            "Passing Yards Per Game", "Rushing Yards Per Game", "Completion Percentage", 
            "Passing Touchdowns", "Rushing Touchdowns", "First Downs",
            "Passing Yards", "Rushing Yards", "Total Yards", "Yards Per Play",
            "Passing Attempts", "Rushing Attempts", "Passing Completions",
            "Passing Yards Per Attempt", "Rushing Yards Per Attempt",
            "Third Down Conversion Percentage", "Fourth Down Conversion Percentage",
            "Red Zone Touchdown Percentage", "Time of Possession Per Game",
            "Plays Per Game", "Average Drive Time", "Average Drive Yards"
        };
        
        int col = 0;
        int row = 0;
        for (String statName : offenseStats) {
            StatData stat = allStats.get(statName);
            if (stat != null) {
                VBox statCard = createStatCard(stat, false);
                offenseGrid.add(statCard, col, row);
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private void createDefenseSection() {
        defenseGrid.getChildren().clear();
        
        // Expanded defensive stats
        String[] defenseStats = {
            "Points Allowed Per Game", "Yards Allowed Per Game", "Sacks", 
            "Interceptions", "Fumbles Recovered", "Third Down Conversion Percentage",
            "Passing Yards Allowed Per Game", "Rushing Yards Allowed Per Game",
            "Total Yards Allowed", "Yards Per Play Allowed", "Passing Touchdowns Allowed",
            "Rushing Touchdowns Allowed", "Red Zone Touchdown Percentage Allowed",
            "Fourth Down Conversion Percentage Allowed", "Safeties", "Defensive Touchdowns",
            "Passes Defended", "Tackles For Loss", "Quarterback Hits", "Forced Fumbles",
            "Fumble Recovery Touchdowns", "Interception Touchdowns", "Blocked Kicks"
        };
        
        int col = 0;
        int row = 0;
        for (String statName : defenseStats) {
            StatData stat = allStats.get(statName);
            if (stat != null) {
                VBox statCard = createStatCard(stat, false);
                defenseGrid.add(statCard, col, row);
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private void createSpecialSection() {
        specialGrid.getChildren().clear();
        
        // Expanded special teams and miscellaneous stats
        String[] specialStats = {
            "Field Goal Percentage", "Punt Average", "Kickoff Return Average", 
            "Punt Return Average", "Penalties", "Penalty Yards",
            "Field Goals Made", "Field Goals Attempted", "Extra Points Made",
            "Extra Points Attempted", "Touchbacks", "Fair Catches",
            "Punts Inside 20", "Kickoff Return Touchdowns", "Punt Return Touchdowns",
            "Blocked Field Goals", "Blocked Punts", "Onside Kicks Recovered",
            "Penalties Per Game", "Penalty Yards Per Game", "Challenge Success Rate"
        };
        
        int col = 0;
        int row = 0;
        for (String statName : specialStats) {
            StatData stat = allStats.get(statName);
            if (stat != null) {
                VBox statCard = createStatCard(stat, false);
                specialGrid.add(statCard, col, row);
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private void createFunFactsSection() {
        funFactsContainer.getChildren().clear();
        
        List<String> funFacts = generateFunFacts();
        for (String fact : funFacts) {
            Label factLabel = new Label("🎯 " + fact);
            factLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 14px; -fx-padding: 5px;");
            factLabel.setWrapText(true);
            funFactsContainer.getChildren().add(factLabel);
        }
    }

    private VBox createStatCard(StatData stat, boolean isKPI) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: rgba(65, 90, 119, 0.3); -fx-background-radius: 8px; -fx-padding: 15px; -fx-border-color: #415a77; -fx-border-width: 1px; -fx-border-radius: 8px;");
        card.setPrefWidth(isKPI ? 180 : 240);
        card.setPrefHeight(isKPI ? 110 : 100);

        // Stat name
        Label nameLabel = new Label(stat.displayName);
        nameLabel.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 11px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);

        // Stat value
        Label valueLabel = new Label(stat.displayValue);
        valueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Rank with performance bar
        HBox rankBox = new HBox(8);
        rankBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String originalRank = stat.rankDisplayValue;
        String displayRank = "";

        if (originalRank != null) {
            // Simplify rank for display by removing all non-numeric characters
            displayRank = originalRank.replaceAll("[^0-9]", "");
        }

        Label rankLabel = new Label("Rank: " + displayRank);
        rankLabel.setStyle("-fx-text-fill: #a0a0a0; -fx-font-size: 10px;");
        // Tooltip will show the original, more descriptive rank
        rankLabel.setTooltip(new Tooltip("Full Rank: " + originalRank));

        // Create a simple colored rectangle instead of a progress bar
        javafx.scene.shape.Rectangle rankBar = new javafx.scene.shape.Rectangle();
        rankBar.setWidth(80);
        rankBar.setHeight(8);
        rankBar.setArcWidth(4);
        rankBar.setArcHeight(4);
        
        // Default color
        String fillColor = "#9E9E9E";
        
        try {
            // Try to extract rank number from various formats
            String rankText = stat.rankDisplayValue;
            int rank = -1;
            
            if (rankText != null) {
                if (rankText.contains("st") || rankText.contains("nd") || rankText.contains("rd") || rankText.contains("th")) {
                    rank = Integer.parseInt(rankText.replaceAll("[^0-9]", ""));
                } else if (rankText.matches("\\d+")) {
                    rank = Integer.parseInt(rankText);
                } else if (rankText.contains("T-")) {
                    // Handle tied ranks like "T-5"
                    rank = Integer.parseInt(rankText.replace("T-", ""));
                }
            }
            
            if (rank > 0 && rank <= 32) {
                // Color the bar based on performance
                if (rank <= 8) {
                    fillColor = "#4CAF50"; // Green for top 8
                } else if (rank <= 16) {
                    fillColor = "#FFC107"; // Yellow for top 16
                } else if (rank <= 24) {
                    fillColor = "#FF9800"; // Orange for top 24
                } else {
                    fillColor = "#F44336"; // Red for bottom 8
                }
            }
        } catch (NumberFormatException e) {
            // Keep default color for parsing errors
        }
        
        rankBar.setFill(javafx.scene.paint.Color.web(fillColor));
        rankBar.setOpacity(0.8);
        // Add a subtle stroke
        rankBar.setStroke(javafx.scene.paint.Color.web(fillColor));
        rankBar.setStrokeWidth(1);

        rankBox.getChildren().addAll(rankLabel, rankBar);

        card.getChildren().addAll(nameLabel, valueLabel, rankBox);
        return card;
    }

    private List<String> generateInsights() {
        List<String> insights = new ArrayList<>();
        
        // Analyze offensive performance
        StatData pointsPerGame = allStats.get("Points Per Game");
        StatData yardsPerGame = allStats.get("Total Yards Per Game");
        StatData completionPct = allStats.get("Completion Percentage");
        StatData redZonePct = allStats.get("Red Zone Touchdown Percentage");
        StatData timeOfPossession = allStats.get("Time of Possession Per Game");
        
        if (pointsPerGame != null) {
            try {
                double points = Double.parseDouble(pointsPerGame.displayValue);
                if (points > 28) {
                    insights.add("🔥 Elite offense averaging over 28 points per game - among the league's best!");
                } else if (points > 25) {
                    insights.add("⚡ High-powered offense averaging over 25 points per game.");
                } else if (points < 18) {
                    insights.add("⚠️ Offensive struggles with under 18 points per game - needs improvement.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        if (completionPct != null) {
            try {
                double completion = Double.parseDouble(completionPct.displayValue.replace("%", ""));
                if (completion > 68) {
                    insights.add("🎯 Excellent quarterback accuracy with " + completionPct.displayValue + " completion rate.");
                } else if (completion < 60) {
                    insights.add("📉 Passing efficiency needs work with " + completionPct.displayValue + " completion rate.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        if (redZonePct != null) {
            try {
                double redZone = Double.parseDouble(redZonePct.displayValue.replace("%", ""));
                if (redZone > 70) {
                    insights.add("🏈 Outstanding red zone efficiency at " + redZonePct.displayValue + " - capitalizing on opportunities!");
                } else if (redZone < 50) {
                    insights.add("🚫 Red zone struggles at " + redZonePct.displayValue + " - leaving points on the field.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        // Analyze defensive performance
        StatData pointsAllowed = allStats.get("Points Allowed Per Game");
        StatData sacks = allStats.get("Sacks");
        StatData interceptions = allStats.get("Interceptions");
        StatData thirdDownDefense = allStats.get("Third Down Conversion Percentage");
        
        if (pointsAllowed != null) {
            try {
                double pointsAllowedValue = Double.parseDouble(pointsAllowed.displayValue);
                if (pointsAllowedValue < 18) {
                    insights.add("🛡️ Elite defense allowing under 18 points per game - championship caliber!");
                } else if (pointsAllowedValue > 25) {
                    insights.add("💥 Defensive concerns allowing over 25 points per game.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        if (sacks != null) {
            try {
                int sackCount = Integer.parseInt(sacks.displayValue);
                if (sackCount > 45) {
                    insights.add("💪 Dominant pass rush with " + sackCount + " sacks - quarterbacks beware!");
                } else if (sackCount < 25) {
                    insights.add("🔍 Pass rush needs improvement with only " + sackCount + " sacks.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        if (interceptions != null) {
            try {
                int intCount = Integer.parseInt(interceptions.displayValue);
                if (intCount > 18) {
                    insights.add("🦅 Ball-hawking secondary with " + intCount + " interceptions!");
                } else if (intCount < 8) {
                    insights.add("📊 Secondary needs more takeaways with only " + intCount + " interceptions.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        // Analyze special teams
        StatData fieldGoalPct = allStats.get("Field Goal Percentage");
        StatData puntAverage = allStats.get("Punt Average");
        
        if (fieldGoalPct != null) {
            try {
                double fgPct = Double.parseDouble(fieldGoalPct.displayValue.replace("%", ""));
                if (fgPct > 90) {
                    insights.add("🎯 Kicking excellence at " + fieldGoalPct.displayValue + " field goal accuracy!");
                } else if (fgPct < 75) {
                    insights.add("⚠️ Kicking struggles at " + fieldGoalPct.displayValue + " - costing points.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        // Analyze efficiency metrics
        StatData turnovers = allStats.get("Turnovers");
        StatData penalties = allStats.get("Penalties");
        
        if (turnovers != null) {
            try {
                int turnoverCount = Integer.parseInt(turnovers.displayValue);
                if (turnoverCount < 12) {
                    insights.add("🔒 Excellent ball security with only " + turnoverCount + " turnovers.");
                } else if (turnoverCount > 20) {
                    insights.add("🎲 Turnover-prone with " + turnoverCount + " giveaways - needs discipline.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        if (penalties != null) {
            try {
                int penaltyCount = Integer.parseInt(penalties.displayValue);
                if (penaltyCount < 80) {
                    insights.add("✅ Disciplined play with only " + penaltyCount + " penalties.");
                } else if (penaltyCount > 120) {
                    insights.add("🚨 Penalty issues with " + penaltyCount + " flags - hurting the team.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        // Add team identity insights
        StatData rushingYards = allStats.get("Rushing Yards Per Game");
        StatData passingYards = allStats.get("Passing Yards Per Game");
        
        if (rushingYards != null && passingYards != null) {
            try {
                double rushYards = Double.parseDouble(rushingYards.displayValue);
                double passYards = Double.parseDouble(passingYards.displayValue);
                
                if (rushYards > passYards * 0.8) {
                    insights.add("🏃 Run-heavy offense - controlling the clock and tempo.");
                } else if (passYards > rushYards * 2) {
                    insights.add("✈️ Pass-first offense - airing it out frequently.");
                } else {
                    insights.add("⚖️ Balanced offensive approach - keeping defenses guessing.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        if (insights.isEmpty()) {
            insights.add("📊 This team shows balanced performance across key metrics.");
        }

        return insights;
    }

    private List<String> generateFunFacts() {
        List<String> facts = new ArrayList<>();
        
        // Generate comprehensive fun facts based on available stats
        StatData turnovers = allStats.get("Turnovers");
        if (turnovers != null) {
            try {
                int turnoverCount = Integer.parseInt(turnovers.displayValue);
                if (turnoverCount < 10) {
                    facts.add("🏆 Elite ball security! Only " + turnoverCount + " turnovers - among the league's best at protecting the football.");
                } else if (turnoverCount > 25) {
                    facts.add("🎲 Turnover troubles with " + turnoverCount + " giveaways - ball security needs immediate attention.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData interceptions = allStats.get("Interceptions");
        if (interceptions != null) {
            try {
                int intCount = Integer.parseInt(interceptions.displayValue);
                if (intCount > 20) {
                    facts.add("🦅 Ball-hawking defense! " + intCount + " interceptions - quarterbacks fear this secondary!");
                } else if (intCount < 5) {
                    facts.add("📊 Secondary needs more takeaways with only " + intCount + " interceptions this season.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData sacks = allStats.get("Sacks");
        if (sacks != null) {
            try {
                int sackCount = Integer.parseInt(sacks.displayValue);
                if (sackCount > 50) {
                    facts.add("💥 Sack attack! " + sackCount + " sacks - this defensive line is absolutely dominant!");
                } else if (sackCount < 20) {
                    facts.add("🔍 Pass rush needs work with only " + sackCount + " sacks - quarterbacks have too much time.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData fieldGoalPct = allStats.get("Field Goal Percentage");
        if (fieldGoalPct != null) {
            try {
                double fgPct = Double.parseDouble(fieldGoalPct.displayValue.replace("%", ""));
                if (fgPct > 95) {
                    facts.add("🎯 Kicking perfection! " + fieldGoalPct.displayValue + " field goal accuracy - automatic from anywhere!");
                } else if (fgPct < 70) {
                    facts.add("⚠️ Kicking woes at " + fieldGoalPct.displayValue + " - costing the team valuable points.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData redZonePct = allStats.get("Red Zone Touchdown Percentage");
        if (redZonePct != null) {
            try {
                double redZone = Double.parseDouble(redZonePct.displayValue.replace("%", ""));
                if (redZone > 75) {
                    facts.add("🏈 Red zone masters! " + redZonePct.displayValue + " touchdown rate - capitalizing on every opportunity!");
                } else if (redZone < 40) {
                    facts.add("🚫 Red zone struggles at " + redZonePct.displayValue + " - leaving too many points on the field.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData timeOfPossession = allStats.get("Time of Possession Per Game");
        if (timeOfPossession != null) {
            try {
                String timeStr = timeOfPossession.displayValue;
                if (timeStr.contains(":") && timeStr.split(":")[0].equals("32")) {
                    facts.add("⏰ Clock controllers! " + timeStr + " average time of possession - keeping the ball away from opponents!");
                } else if (timeStr.contains(":") && Integer.parseInt(timeStr.split(":")[0]) < 28) {
                    facts.add("⚡ Quick-strike offense! " + timeStr + " average time of possession - scoring fast and often!");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData penalties = allStats.get("Penalties");
        if (penalties != null) {
            try {
                int penaltyCount = Integer.parseInt(penalties.displayValue);
                if (penaltyCount < 70) {
                    facts.add("✅ Disciplined squad! Only " + penaltyCount + " penalties - playing clean, smart football!");
                } else if (penaltyCount > 130) {
                    facts.add("🚨 Penalty problems! " + penaltyCount + " flags - undisciplined play is hurting the team.");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData safeties = allStats.get("Safeties");
        if (safeties != null) {
            try {
                int safetyCount = Integer.parseInt(safeties.displayValue);
                if (safetyCount > 0) {
                    facts.add("🛡️ Safety specialists! " + safetyCount + " safeties - the defense is scoring points too!");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData defensiveTouchdowns = allStats.get("Defensive Touchdowns");
        if (defensiveTouchdowns != null) {
            try {
                int defTdCount = Integer.parseInt(defensiveTouchdowns.displayValue);
                if (defTdCount > 3) {
                    facts.add("🏃 Defensive playmakers! " + defTdCount + " defensive touchdowns - the defense is scoring points!");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        StatData puntReturnTouchdowns = allStats.get("Punt Return Touchdowns");
        StatData kickoffReturnTouchdowns = allStats.get("Kickoff Return Touchdowns");
        if (puntReturnTouchdowns != null || kickoffReturnTouchdowns != null) {
            int totalReturnTds = 0;
            try {
                if (puntReturnTouchdowns != null) {
                    totalReturnTds += Integer.parseInt(puntReturnTouchdowns.displayValue);
                }
                if (kickoffReturnTouchdowns != null) {
                    totalReturnTds += Integer.parseInt(kickoffReturnTouchdowns.displayValue);
                }
                if (totalReturnTds > 2) {
                    facts.add("⚡ Special teams stars! " + totalReturnTds + " return touchdowns - explosive in the return game!");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        if (facts.isEmpty()) {
            facts.add("📊 This team has shown consistent performance throughout the season with solid fundamentals.");
        }

        return facts;
    }

    private void clearAllSections() {
        kpiGrid.getChildren().clear();
        insightsContainer.getChildren().clear();
        offenseGrid.getChildren().clear();
        defenseGrid.getChildren().clear();
        specialGrid.getChildren().clear();
        funFactsContainer.getChildren().clear();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #51cf66;");
        }
    }

    // Data class for statistics
    private static class StatData {
        String displayName;
        String displayValue;
        String rankDisplayValue;
        String category;

        public StatData(String displayName, String displayValue, String rankDisplayValue, String category) {
            this.displayName = displayName;
            this.displayValue = displayValue;
            this.rankDisplayValue = rankDisplayValue;
            this.category = category;
        }
    }
} 