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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MatchupPredictorController {

    @FXML private Button backButton;
    @FXML private ComboBox<String> team1Choice;
    @FXML private ComboBox<String> year1Choice;
    @FXML private ImageView team1Logo;
    @FXML private ComboBox<String> team2Choice;
    @FXML private ComboBox<String> year2Choice;
    @FXML private ImageView team2Logo;
    @FXML private Button predictButton;
    @FXML private VBox loadingSection;
    @FXML private ScrollPane resultsScrollPane;
    @FXML private VBox resultsContainer;
    
    @FXML private Label team1Name;
    @FXML private Label team1Score;
    @FXML private Label team2Name;
    @FXML private Label team2Score;
    
    @FXML private Label team1Probability;
    @FXML private Label team2Probability;
    @FXML private Label team1ProbabilityLabel;
    @FXML private Label team2ProbabilityLabel;
    
    @FXML private GridPane playersGrid;
    @FXML private VBox insightsContainer;
    @FXML private GridPane factorsGrid;

    private Scene previousScene;
    private Map<String, String> teamIds = new HashMap<>();
    private Map<String, String> teamLogos = new HashMap<>();
    private Map<String, String> playerNameCache = new HashMap<>();
    private List<String> years = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTeams();
        setupYears();
        setupEventHandlers();
    }

    private void setupTeams() {
        String[][] teams = {
            {"Arizona Cardinals", "22", "https://a.espncdn.com/i/teamlogos/nfl/500/ari.png"},
            {"Atlanta Falcons", "1", "https://a.espncdn.com/i/teamlogos/nfl/500/atl.png"},
            {"Baltimore Ravens", "33", "https://a.espncdn.com/i/teamlogos/nfl/500/bal.png"},
            {"Buffalo Bills", "2", "https://a.espncdn.com/i/teamlogos/nfl/500/buf.png"},
            {"Carolina Panthers", "29", "https://a.espncdn.com/i/teamlogos/nfl/500/car.png"},
            {"Chicago Bears", "3", "https://a.espncdn.com/i/teamlogos/nfl/500/chi.png"},
            {"Cincinnati Bengals", "4", "https://a.espncdn.com/i/teamlogos/nfl/500/cin.png"},
            {"Cleveland Browns", "5", "https://a.espncdn.com/i/teamlogos/nfl/500/cle.png"},
            {"Dallas Cowboys", "6", "https://a.espncdn.com/i/teamlogos/nfl/500/dal.png"},
            {"Denver Broncos", "7", "https://a.espncdn.com/i/teamlogos/nfl/500/den.png"},
            {"Detroit Lions", "8", "https://a.espncdn.com/i/teamlogos/nfl/500/det.png"},
            {"Green Bay Packers", "9", "https://a.espncdn.com/i/teamlogos/nfl/500/gb.png"},
            {"Houston Texans", "34", "https://a.espncdn.com/i/teamlogos/nfl/500/hou.png"},
            {"Indianapolis Colts", "11", "https://a.espncdn.com/i/teamlogos/nfl/500/ind.png"},
            {"Jacksonville Jaguars", "30", "https://a.espncdn.com/i/teamlogos/nfl/500/jax.png"},
            {"Kansas City Chiefs", "12", "https://a.espncdn.com/i/teamlogos/nfl/500/kc.png"},
            {"Las Vegas Raiders", "13", "https://a.espncdn.com/i/teamlogos/nfl/500/lv.png"},
            {"Los Angeles Chargers", "24", "https://a.espncdn.com/i/teamlogos/nfl/500/lac.png"},
            {"Los Angeles Rams", "14", "https://a.espncdn.com/i/teamlogos/nfl/500/lar.png"},
            {"Miami Dolphins", "15", "https://a.espncdn.com/i/teamlogos/nfl/500/mia.png"},
            {"Minnesota Vikings", "16", "https://a.espncdn.com/i/teamlogos/nfl/500/min.png"},
            {"New England Patriots", "17", "https://a.espncdn.com/i/teamlogos/nfl/500/ne.png"},
            {"New Orleans Saints", "18", "https://a.espncdn.com/i/teamlogos/nfl/500/no.png"},
            {"New York Giants", "19", "https://a.espncdn.com/i/teamlogos/nfl/500/nyg.png"},
            {"New York Jets", "20", "https://a.espncdn.com/i/teamlogos/nfl/500/nyj.png"},
            {"Philadelphia Eagles", "21", "https://a.espncdn.com/i/teamlogos/nfl/500/phi.png"},
            {"Pittsburgh Steelers", "23", "https://a.espncdn.com/i/teamlogos/nfl/500/pit.png"},
            {"San Francisco 49ers", "25", "https://a.espncdn.com/i/teamlogos/nfl/500/sf.png"},
            {"Seattle Seahawks", "26", "https://a.espncdn.com/i/teamlogos/nfl/500/sea.png"},
            {"Tampa Bay Buccaneers", "27", "https://a.espncdn.com/i/teamlogos/nfl/500/tb.png"},
            {"Tennessee Titans", "10", "https://a.espncdn.com/i/teamlogos/nfl/500/ten.png"},
            {"Washington Commanders", "28", "https://a.espncdn.com/i/teamlogos/nfl/500/wsh.png"}
        };

        List<String> teamNames = new ArrayList<>();
        for (String[] team : teams) {
            teamNames.add(team[0]);
            teamIds.put(team[0], team[1]);
            teamLogos.put(team[0], team[2]);
        }

        team1Choice.setItems(javafx.collections.FXCollections.observableArrayList(teamNames));
        team2Choice.setItems(javafx.collections.FXCollections.observableArrayList(teamNames));
        
        if (!teamNames.isEmpty()) {
            team1Choice.setValue(teamNames.get(0));
            team2Choice.setValue(teamNames.size() > 1 ? teamNames.get(1) : teamNames.get(0));
        }
    }

    private void setupYears() {
        int currentYear = java.time.Year.now().getValue();
        for (int year = currentYear; year >= 2002; year--) {
            years.add(String.valueOf(year));
        }
        
        year1Choice.setItems(javafx.collections.FXCollections.observableArrayList(years));
        year2Choice.setItems(javafx.collections.FXCollections.observableArrayList(years));
        year1Choice.setValue(String.valueOf(currentYear));
        year2Choice.setValue(String.valueOf(currentYear));
    }

    private void setupEventHandlers() {
        backButton.setOnAction(event -> goBack());
        predictButton.setOnAction(event -> predictMatchup());
        
        team1Choice.setOnAction(event -> updateTeamLogo(team1Choice.getValue(), team1Logo));
        team2Choice.setOnAction(event -> updateTeamLogo(team2Choice.getValue(), team2Logo));
        
        updateTeamLogo(team1Choice.getValue(), team1Logo);
        updateTeamLogo(team2Choice.getValue(), team2Logo);
    }

    private void updateTeamLogo(String teamName, ImageView logoView) {
        if (teamName != null && teamLogos.containsKey(teamName)) {
            try {
                logoView.setImage(new Image(teamLogos.get(teamName)));
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
    private void predictMatchup() {
        if (team1Choice.getValue() == null || team2Choice.getValue() == null ||
            year1Choice.getValue() == null || year2Choice.getValue() == null) {
            showAlert("Please select both teams and years.");
            return;
        }

        if (team1Choice.getValue().equals(team2Choice.getValue()) && 
            year1Choice.getValue().equals(year2Choice.getValue())) {
            showAlert("Please select different teams or years for the matchup.");
            return;
        }

        showLoading(true);
        
        new Thread(() -> {
            try {
                MatchupPrediction prediction = generatePrediction();
                
                Platform.runLater(() -> {
                    displayPrediction(prediction);
                    showLoading(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error generating prediction: " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }

    private MatchupPrediction generatePrediction() throws Exception {
        String team1Name = team1Choice.getValue();
        String team2Name = team2Choice.getValue();
        String year1 = year1Choice.getValue();
        String year2 = year2Choice.getValue();
        
        String team1Id = teamIds.get(team1Name);
        String team2Id = teamIds.get(team2Name);

        TeamStats team1Stats = fetchTeamStats(team1Id, year1);
        TeamStats team2Stats = fetchTeamStats(team2Id, year2);
        
        TeamLeaders team1Leaders = fetchTeamLeaders(team1Id, year1);
        TeamLeaders team2Leaders = fetchTeamLeaders(team2Id, year2);

        return calculatePrediction(team1Name, team2Name, year1, year2, 
                                 team1Stats, team2Stats, team1Leaders, team2Leaders);
    }

    private TeamStats fetchTeamStats(String teamId, String year) throws Exception {
        String url = String.format("http://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/%s/types/2/teams/%s/statistics", year, teamId);
        
        String jsonResponse = APIController.getRawJsonFromUrl(url);
        if (jsonResponse == null) {
            throw new Exception("Failed to fetch team statistics");
        }

        JSONParser parser = new JSONParser();
        JSONObject statsJson = (JSONObject) parser.parse(jsonResponse);
        
        return parseTeamStats(statsJson);
    }
    
    private TeamLeaders fetchTeamLeaders(String teamId, String year) throws Exception {
        String url = String.format("https://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/%s/types/2/teams/%s/leaders", year, teamId);
        
        String jsonResponse = APIController.getRawJsonFromUrl(url);
        if (jsonResponse == null) {
            throw new Exception("Failed to fetch team leaders");
        }

        JSONParser parser = new JSONParser();
        JSONObject leadersJson = (JSONObject) parser.parse(jsonResponse);
        
        return parseTeamLeaders(leadersJson);
    }

    private TeamStats parseTeamStats(JSONObject statsJson) {
        TeamStats stats = new TeamStats();
        
        try {
            JSONObject splits = (JSONObject) statsJson.get("splits");
            JSONArray categories = (JSONArray) splits.get("categories");

            System.out.println("=== AVAILABLE STATS DEBUG ===");
            for (Object categoryObj : categories) {
                JSONObject category = (JSONObject) categoryObj;
                String categoryName = (String) category.get("name");
                JSONArray statsArray = (JSONArray) category.get("stats");
                
                System.out.println("Category: " + categoryName);

                for (Object statObj : statsArray) {
                    JSONObject stat = (JSONObject) statObj;
                    String displayName = (String) stat.get("displayName");
                    String displayValue = (String) stat.get("displayValue");
                    String rankDisplayValue = (String) stat.get("rankDisplayValue");
                    
                    System.out.println("  " + displayName + ": " + displayValue + " (Rank: " + rankDisplayValue + ")");
                    stats.addStat(displayName, displayValue, rankDisplayValue);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing team stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    private TeamLeaders parseTeamLeaders(JSONObject leadersJson) {
        TeamLeaders leaders = new TeamLeaders();
        
        try {
            JSONArray categories = (JSONArray) leadersJson.get("categories");
            
            for (Object categoryObj : categories) {
                JSONObject category = (JSONObject) categoryObj;
                String categoryName = (String) category.get("displayName");
                String shortDisplayName = (String) category.get("shortDisplayName");
                JSONArray leadersArray = (JSONArray) category.get("leaders");
                
                if (leadersArray != null && !leadersArray.isEmpty()) {
                    for (Object leaderObj : leadersArray) {
                        JSONObject leader = (JSONObject) leaderObj;
                        String displayValue = (String) leader.get("displayValue");
                        Double value = (Double) leader.get("value");
                        JSONObject athlete = (JSONObject) leader.get("athlete");
                        
                        if (athlete != null) {
                            String athleteRef = (String) athlete.get("$ref");
                            String playerName = getPlayerName(athleteRef);
                            
                            leaders.addLeader(categoryName, shortDisplayName, playerName, displayValue, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing team leaders: " + e.getMessage());
        }
        
        return leaders;
    }
    
    private String getPlayerName(String athleteRef) {
        if (playerNameCache.containsKey(athleteRef)) {
            return playerNameCache.get(athleteRef);
        }
        
        try {
            String jsonResponse = APIController.getRawJsonFromUrl(athleteRef);
            if (jsonResponse != null) {
                JSONParser parser = new JSONParser();
                JSONObject athlete = (JSONObject) parser.parse(jsonResponse);
                
                String firstName = (String) athlete.get("firstName");
                String lastName = (String) athlete.get("lastName");
                
                String position = null;
                Object positionObj = athlete.get("position");
                if (positionObj instanceof JSONObject) {
                    JSONObject positionJson = (JSONObject) positionObj;
                    position = (String) positionJson.get("displayName");
                } else if (positionObj instanceof String) {
                    position = (String) positionObj;
                }
                
                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                String displayName = fullName.trim();
                
                if (position != null && !position.isEmpty()) {
                    displayName += " (" + position + ")";
                }
                
                playerNameCache.put(athleteRef, displayName);
                return displayName;
            }
        } catch (Exception e) {
            System.err.println("Error fetching player name: " + e.getMessage());
        }
        
        return "Unknown Player";
    }

    private MatchupPrediction calculatePrediction(String team1Name, String team2Name, 
                                                String year1, String year2, 
                                                TeamStats team1Stats, TeamStats team2Stats,
                                                TeamLeaders team1Leaders, TeamLeaders team2Leaders) {
        
        double team1OffenseRating = calculateOffensiveRating(team1Stats);
        double team2OffenseRating = calculateOffensiveRating(team2Stats);
        double team1DefenseRating = calculateDefensiveRating(team1Stats);
        double team2DefenseRating = calculateDefensiveRating(team2Stats);
        double team1SpecialRating = calculateSpecialTeamsRating(team1Stats);
        double team2SpecialRating = calculateSpecialTeamsRating(team2Stats);
        
        System.out.println("=== TEAM RATINGS DEBUG ===");
        System.out.println(team1Name + " (" + year1 + "):");
        System.out.println("  Offense: " + team1OffenseRating);
        System.out.println("  Defense: " + team1DefenseRating);
        System.out.println("  Special: " + team1SpecialRating);
        System.out.println(team2Name + " (" + year2 + "):");
        System.out.println("  Offense: " + team2OffenseRating);
        System.out.println("  Defense: " + team2DefenseRating);
        System.out.println("  Special: " + team2SpecialRating);
        
        System.out.println("=== KEY STATS DEBUG ===");
        System.out.println(team1Name + " stats:");
        System.out.println("  PPG: " + team1Stats.getStatValue("Total Points Per Game") + 
                          " (Rank: " + team1Stats.getStatRank("Total Points Per Game") + ")");
        System.out.println("  YPG: " + team1Stats.getStatValue("Yards Per Game") + 
                          " (Rank: " + team1Stats.getStatRank("Yards Per Game") + ")");
        System.out.println("  Sacks: " + team1Stats.getStatValue("Sacks") + 
                          " (Rank: " + team1Stats.getStatRank("Sacks") + ")");
        System.out.println(team2Name + " stats:");
        System.out.println("  PPG: " + team2Stats.getStatValue("Total Points Per Game") + 
                          " (Rank: " + team2Stats.getStatRank("Total Points Per Game") + ")");
        System.out.println("  YPG: " + team2Stats.getStatValue("Yards Per Game") + 
                          " (Rank: " + team2Stats.getStatRank("Yards Per Game") + ")");
        System.out.println("  Sacks: " + team2Stats.getStatValue("Sacks") + 
                          " (Rank: " + team2Stats.getStatRank("Sacks") + ")");

        int team1Score = calculateScore(team1OffenseRating, team2DefenseRating, team1SpecialRating);
        int team2Score = calculateScore(team2OffenseRating, team1DefenseRating, team2SpecialRating);

        double team1WinProb = calculateWinProbability(team1Score, team2Score, 
                                                    team1OffenseRating, team1DefenseRating, team1SpecialRating,
                                                    team2OffenseRating, team2DefenseRating, team2SpecialRating);

        List<String> insights = generateCreativeInsights(team1Name, team2Name, year1, year2, 
                                                       team1Stats, team2Stats, team1Score, team2Score,
                                                       team1OffenseRating, team1DefenseRating,
                                                       team2OffenseRating, team2DefenseRating);

        PlayerPrediction team1QB = predictTopPasser(team1Name, team1Leaders, team1Stats, team1Score);
        PlayerPrediction team1RB = predictTopRusher(team1Name, team1Leaders, team1Stats, team1Score);
        PlayerPrediction team1Def = predictTopDefender(team1Name, team1Leaders, team1Stats);
        
        PlayerPrediction team2QB = predictTopPasser(team2Name, team2Leaders, team2Stats, team2Score);
        PlayerPrediction team2RB = predictTopRusher(team2Name, team2Leaders, team2Stats, team2Score);
        PlayerPrediction team2Def = predictTopDefender(team2Name, team2Leaders, team2Stats);

        return new MatchupPrediction(
            team1Name, team2Name, year1, year2,
            team1Score, team2Score, team1WinProb,
            insights, team1QB, team1RB, team1Def, team2QB, team2RB, team2Def
        );
    }

    private double calculateOffensiveRating(TeamStats stats) {
        double rating = 0.0;
        
        System.out.println("=== OFFENSIVE RATING CALCULATION ===");
        
        rating += calculateRankBasedScore(stats, "Total Points Per Game", 0.4, false);
        
        rating += calculateRankBasedScore(stats, "Yards Per Game", 0.25, false);
        
        rating += calculateRankBasedScore(stats, "3rd down %", 0.2, false);
        
        rating += calculateRankBasedScore(stats, "Red Zone Touchdown Percentage", 0.15, false);

        System.out.println("  Total Offensive Rating: " + rating);
        return rating;
    }

    private double calculateDefensiveRating(TeamStats stats) {
        double rating = 0.0;
        
        System.out.println("=== DEFENSIVE RATING CALCULATION ===");
        
        rating += calculateRankBasedScore(stats, "Sacks", 0.4, false);
        
        rating += calculateRankBasedScore(stats, "Interceptions", 0.3, false);
        
        rating += calculateRankBasedScore(stats, "Forced Fumbles", 0.15, false);
        
        rating += calculateRankBasedScore(stats, "Passes Defended", 0.15, false);

        System.out.println("  Total Defensive Rating: " + rating);
        return rating;
    }
    
    private double calculateRankBasedScore(TeamStats stats, String statName, double weight, boolean lowerIsBetter) {
        StatData statData = stats.getStat(statName);
        if (statData == null || statData.rankDisplayValue == null) {
            System.out.println("  " + statName + ": No data available");
            return 0.0;
        }
        
        try {
            String rankStr = statData.rankDisplayValue.replaceAll("[^0-9]", "");
            if (rankStr.isEmpty()) {
                System.out.println("  " + statName + ": Invalid rank format");
                return 0.0;
            }
            
            int rank = Integer.parseInt(rankStr);
            
            double rankScore;
            if (lowerIsBetter) {
                rankScore = ((32 - rank) / 31.0) * 100.0;
            } else {
                rankScore = ((33 - rank) / 32.0) * 100.0;
            }
            
            double weightedScore = rankScore * weight;
            
            System.out.println("  " + statName + ": " + statData.displayValue + " (Rank " + rank + ") -> Score: " + 
                             String.format("%.1f", rankScore) + " * " + weight + " = " + String.format("%.1f", weightedScore));
            
            return weightedScore;
            
        } catch (NumberFormatException e) {
            System.out.println("  " + statName + ": Error parsing rank");
            return 0.0;
        }
    }
    
    private double calculateSpecialTeamsRating(TeamStats stats) {
        double rating = 0.0;
        
        System.out.println("=== SPECIAL TEAMS RATING CALCULATION ===");
        
        rating += calculateRankBasedScore(stats, "Field Goal Percentage", 0.5, false);
        
        rating += calculateRankBasedScore(stats, "Gross Average Punt Yards", 0.25, false);
        
        rating += calculateRankBasedScore(stats, "Yards Per Kick Return", 0.125, false);
        
        rating += calculateRankBasedScore(stats, "Yards Per Punt Return", 0.125, false);
        
        System.out.println("  Total Special Teams Rating: " + rating);
        return rating;
    }

    private int calculateScore(double offense, double opponentDefense, double specialTeams) {
        double offenseVsDefense = (offense - opponentDefense) / 10.0;
        double baseScore = 22.0 + offenseVsDefense;
        baseScore += (specialTeams / 100.0) * 3;
        double variance = ThreadLocalRandom.current().nextDouble(-0.2, 0.2);
        int score = (int) Math.round(baseScore * (1 + variance));
        return Math.max(3, Math.min(42, score));
    }

    private double calculateWinProbability(int score1, int score2, double off1, double def1, double spec1,
                                         double off2, double def2, double spec2) {
        double scoreDiff = score1 - score2;
        double scoreProb = 1.0 / (1.0 + Math.exp(-scoreDiff * 0.15));
        double team1Strength = (off1 + def1) / 2.0 + (spec1 * 0.1);
        double team2Strength = (off2 + def2) / 2.0 + (spec2 * 0.1);
        double strengthDiff = team1Strength - team2Strength;
        double strengthProb = 1.0 / (1.0 + Math.exp(-strengthDiff * 0.03));
        double finalProb = (scoreProb * 0.8) + (strengthProb * 0.2);
        return Math.max(0.05, Math.min(0.95, finalProb));
    }
    
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private List<String> generateCreativeInsights(String team1, String team2, String year1, String year2,
                                                TeamStats stats1, TeamStats stats2, int score1, int score2,
                                                double off1, double def1, double off2, double def2) {
        List<String> insights = new ArrayList<>();
        
        int yearDiff = Math.abs(Integer.parseInt(year1) - Integer.parseInt(year2));
        if (yearDiff > 10) {
            insights.add("🕰️ This epic cross-decade matchup spans " + yearDiff + " years! " +
                        (Integer.parseInt(year1) > Integer.parseInt(year2) ? team1 : team2) + 
                        " represents the modern era while their opponent brings old-school football.");
        } else if (yearDiff > 5) {
            insights.add("📅 A fascinating cross-era battle with " + yearDiff + " years between these teams - " +
                        "expect different philosophies and playing styles to clash!");
        }

        if (off1 > off2 * 1.3 && def1 > def2 * 1.3) {
            insights.add("🔥 " + team1 + " dominates on BOTH sides of the ball! This could be a complete dismantling.");
        } else if (off2 > off1 * 1.3 && def2 > def1 * 1.3) {
            insights.add("🔥 " + team2 + " dominates on BOTH sides of the ball! This could be a complete dismantling.");
        } else if (off1 > off2 * 1.4) {
            insights.add("⚡ " + team1 + "'s explosive offense could overwhelm " + team2 + "'s defense in a high-scoring affair!");
        } else if (off2 > off1 * 1.4) {
            insights.add("⚡ " + team2 + "'s explosive offense could overwhelm " + team1 + "'s defense in a high-scoring affair!");
        } else if (def1 > def2 * 1.4) {
            insights.add("🛡️ " + team1 + "'s fortress-like defense could shut down " + team2 + "'s offense completely!");
        } else if (def2 > def1 * 1.4) {
            insights.add("🛡️ " + team2 + "'s fortress-like defense could shut down " + team1 + "'s offense completely!");
        }

        if (Math.abs(score1 - score2) <= 3) {
            insights.add("⚖️ NAIL-BITER ALERT! This game is predicted to come down to the final possession - " +
                        "every play will matter in this instant classic!");
        } else if (Math.abs(score1 - score2) >= 14) {
            insights.add("💥 One-sided battle incoming! This could turn into a statement game with " +
                        (score1 > score2 ? team1 : team2) + " pulling away early.");
        }

        int totalPoints = score1 + score2;
        if (totalPoints > 55) {
            insights.add("🎯 SHOOTOUT SPECIAL! Expect fireworks as both offenses light up the scoreboard in this " +
                        "potential game-of-the-year candidate!");
        } else if (totalPoints < 35) {
            insights.add("🔒 DEFENSIVE SLUGFEST! This will be a battle of attrition where every yard is earned " +
                        "and field position is everything.");
        } else if (totalPoints >= 45) {
            insights.add("🏈 High-octane football ahead! Both teams should move the ball well in this entertaining matchup.");
        }

        String higherScoringTeam = score1 > score2 ? team1 : team2;
        insights.add("🏆 " + higherScoringTeam + " has the edge in this simulation, but in football, " +
                    "any given Sunday can produce magic - upset potential is always lurking!");

        return insights;
    }

    private PlayerPrediction predictTopPasser(String teamName, TeamLeaders leaders, TeamStats stats, int teamScore) {
        String player = leaders.getTopLeader("Passing Yards");
        if (player == null) {
            player = leaders.getTopLeader("Passing Touchdowns");
            if (player == null) player = leaders.getTopLeader("PYDS");
            if (player == null) player = teamName + " Starting QB";
        }
        
        String passingYardsPerGame = stats.getStatValue("Passing Yards Per Game");
        String completionPct = stats.getStatValue("Completion Percentage");
        
        int yards = 225;
        double completion = 62.0;
        int tds = 1;
        
        try {
            if (passingYardsPerGame != null) {
                yards = (int) parseDouble(passingYardsPerGame);
                yards = (int) (yards * 0.95);
            }
            if (completionPct != null) {
                completion = parseDouble(completionPct);
            }
            int totalTDs = Math.max(0, (teamScore - 6) / 7);
            tds = Math.max(0, Math.min(4, (int) (totalTDs * ThreadLocalRandom.current().nextDouble(0.4, 0.7))));
        } catch (NumberFormatException e) {}
        yards = (int) (yards * ThreadLocalRandom.current().nextDouble(0.7, 1.4));
        completion = Math.max(45, Math.min(80, completion * ThreadLocalRandom.current().nextDouble(0.85, 1.15)));
        tds = Math.max(0, Math.min(4, tds + ThreadLocalRandom.current().nextInt(-1, 2)));
        
        return new PlayerPrediction("QB", player, 
                                  yards + " YDS, " + String.format("%.1f", completion) + "%, " + tds + " TD");
    }
    
    private PlayerPrediction predictTopRusher(String teamName, TeamLeaders leaders, TeamStats stats, int teamScore) {
        String player = leaders.getTopLeader("Rushing Yards");
        if (player == null) {
            player = leaders.getTopLeader("Rushing Touchdowns");
            if (player == null) player = leaders.getTopLeader("RYDS");
            if (player == null) player = teamName + " Leading RB";
        }
        
        String rushingYardsPerGame = stats.getStatValue("Rushing Yards Per Game");
        
        int yards = 95;
        int tds = 0;
        
        try {
            if (rushingYardsPerGame != null) {
                yards = (int) parseDouble(rushingYardsPerGame);
                yards = (int) (yards * 0.7);
            }
            int totalTDs = Math.max(0, (teamScore - 6) / 7);
            tds = Math.max(0, Math.min(3, (int) (totalTDs * ThreadLocalRandom.current().nextDouble(0.3, 0.5))));
        } catch (NumberFormatException e) {}
        yards = (int) (yards * ThreadLocalRandom.current().nextDouble(0.5, 1.8));
        tds = Math.max(0, Math.min(3, tds + ThreadLocalRandom.current().nextInt(0, 2)));
        
        return new PlayerPrediction("RB", player, 
                                  yards + " YDS, " + tds + " TD");
    }

    private PlayerPrediction predictTopDefender(String teamName, TeamLeaders leaders, TeamStats stats) {
        String player = leaders.getTopLeader("Sacks");
        if (player == null) {
            player = leaders.getTopLeader("Tackles");
            if (player == null) {
                player = leaders.getTopLeader("Interceptions");
                if (player == null) player = teamName + " Defense";
            }
        }
        
        int tackles = ThreadLocalRandom.current().nextInt(4, 12);
        int gameSacks = 0;
        int gameInts = 0;
        
        String teamSacks = stats.getStatValue("Sacks");
        String teamInts = stats.getStatValue("Interceptions");
        
        try {
            if (teamSacks != null) {
                int seasonSacks = (int) parseDouble(teamSacks);
                if (seasonSacks > 35) {
                    gameSacks = ThreadLocalRandom.current().nextInt(0, 3);
                } else if (seasonSacks > 25) {
                    gameSacks = ThreadLocalRandom.current().nextInt(0, 2);
                } else {
                    gameSacks = ThreadLocalRandom.current().nextInt(0, 2) == 0 ? 1 : 0;
                }
            }
            
            if (teamInts != null) {
                int seasonInts = (int) parseDouble(teamInts);
                if (seasonInts > 15) {
                    gameInts = ThreadLocalRandom.current().nextInt(0, 2);
                } else {
                    gameInts = ThreadLocalRandom.current().nextInt(0, 2) == 0 ? 1 : 0;
                }
            }
        } catch (NumberFormatException e) {}
        
        return new PlayerPrediction("DEF", player, 
                                  tackles + " TCK, " + gameSacks + " SACK, " + gameInts + " INT");
    }

    private void displayPrediction(MatchupPrediction prediction) {
        team1Name.setText(prediction.team1Name + " (" + prediction.year1 + ")");
        team2Name.setText(prediction.team2Name + " (" + prediction.year2 + ")");
        team1Score.setText(String.valueOf(prediction.team1Score));
        team2Score.setText(String.valueOf(prediction.team2Score));

        int team1Prob = (int) (prediction.team1WinProbability * 100);
        int team2Prob = 100 - team1Prob;
        
        team1Probability.setText(team1Prob + "%");
        team2Probability.setText(team2Prob + "%");
        team1ProbabilityLabel.setText(prediction.team1Name);
        team2ProbabilityLabel.setText(prediction.team2Name);

        displayKeyPlayers(prediction);
        displayInsights(prediction.insights);
        displayKeyFactors(prediction);
        resultsScrollPane.setVisible(true);
    }

    private void displayKeyPlayers(MatchupPrediction prediction) {
        playersGrid.getChildren().clear();
        
        addPlayerCard(playersGrid, 0, 0, prediction.team1QB);
        addPlayerCard(playersGrid, 0, 1, prediction.team1RB);
        addPlayerCard(playersGrid, 0, 2, prediction.team1Def);
        
        addPlayerCard(playersGrid, 1, 0, prediction.team2QB);
        addPlayerCard(playersGrid, 1, 1, prediction.team2RB);
        addPlayerCard(playersGrid, 1, 2, prediction.team2Def);
    }

    private void addPlayerCard(GridPane grid, int row, int col, PlayerPrediction player) {
        VBox card = new VBox(8);
        card.getStyleClass().add("player-prediction-card");
        card.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label positionLabel = new Label(player.position);
        positionLabel.getStyleClass().add("player-position");
        
        Label nameLabel = new Label(player.name);
        nameLabel.getStyleClass().add("player-name");
        
        Label statsLabel = new Label(player.stats);
        statsLabel.getStyleClass().add("player-stats");
        
        card.getChildren().addAll(positionLabel, nameLabel, statsLabel);
        grid.add(card, col, row);
    }

    private void displayInsights(List<String> insights) {
        insightsContainer.getChildren().clear();
        
        for (String insight : insights) {
            Label insightLabel = new Label(insight);
            insightLabel.getStyleClass().add("insight-label");
            insightLabel.setWrapText(true);
            insightsContainer.getChildren().add(insightLabel);
        }
    }

    private void displayKeyFactors(MatchupPrediction prediction) {
        factorsGrid.getChildren().clear();
        
        addFactorCard(factorsGrid, 0, 0, "Offensive Battle", 
                     prediction.team1Name + " vs " + prediction.team2Name + " Defense");
        addFactorCard(factorsGrid, 0, 1, "Defensive Showdown", 
                     prediction.team2Name + " vs " + prediction.team1Name + " Defense");
        addFactorCard(factorsGrid, 1, 0, "Era Gap", 
                     Math.abs(Integer.parseInt(prediction.year1) - Integer.parseInt(prediction.year2)) + " years apart");
        addFactorCard(factorsGrid, 1, 1, "Total Points", 
                     (prediction.team1Score + prediction.team2Score) + " combined points");
    }

    private void addFactorCard(GridPane grid, int row, int col, String title, String value) {
        VBox card = new VBox(5);
        card.getStyleClass().add("factor-card");
        card.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("factor-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("factor-value");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        grid.add(card, col, row);
    }

    private void showLoading(boolean show) {
        loadingSection.setVisible(show);
        resultsScrollPane.setVisible(!show);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Matchup Predictor");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class TeamStats {
        private Map<String, StatData> stats = new HashMap<>();
        
        public void addStat(String name, String value, String rank) {
            stats.put(name, new StatData(name, value, rank));
        }
        
        public StatData getStat(String name) {
            return stats.get(name);
        }
        
        public String getStatValue(String name) {
            StatData stat = stats.get(name);
            return stat != null ? stat.displayValue : null;
        }
        
        public String getStatRank(String name) {
            StatData stat = stats.get(name);
            return stat != null ? stat.rankDisplayValue : null;
        }
    }
    
    private static class StatData {
        String displayName;
        String displayValue;
        String rankDisplayValue;
        
        public StatData(String displayName, String displayValue, String rankDisplayValue) {
            this.displayName = displayName;
            this.displayValue = displayValue;
            this.rankDisplayValue = rankDisplayValue;
        }
    }
    
    private static class TeamLeaders {
        private Map<String, String> topLeaders = new HashMap<>();
        
        public void addLeader(String category, String shortName, String playerName, String displayValue, Double value) {
            if (!topLeaders.containsKey(category)) {
                topLeaders.put(category, playerName);
            }
            if (!topLeaders.containsKey(shortName)) {
                topLeaders.put(shortName, playerName);
            }
        }
        
        public String getTopLeader(String category) {
            return topLeaders.get(category);
        }
    }

    private static class MatchupPrediction {
        String team1Name, team2Name, year1, year2;
        int team1Score, team2Score;
        double team1WinProbability;
        List<String> insights;
        PlayerPrediction team1QB, team1RB, team1Def, team2QB, team2RB, team2Def;

        public MatchupPrediction(String team1Name, String team2Name, String year1, String year2,
                               int team1Score, int team2Score, double team1WinProbability,
                               List<String> insights, PlayerPrediction team1QB, PlayerPrediction team1RB, 
                               PlayerPrediction team1Def, PlayerPrediction team2QB, PlayerPrediction team2RB, 
                               PlayerPrediction team2Def) {
            this.team1Name = team1Name;
            this.team2Name = team2Name;
            this.year1 = year1;
            this.year2 = year2;
            this.team1Score = team1Score;
            this.team2Score = team2Score;
            this.team1WinProbability = team1WinProbability;
            this.insights = insights;
            this.team1QB = team1QB;
            this.team1RB = team1RB;
            this.team1Def = team1Def;
            this.team2QB = team2QB;
            this.team2RB = team2RB;
            this.team2Def = team2Def;
        }
    }

    private static class PlayerPrediction {
        String position, name, stats;

        public PlayerPrediction(String position, String name, String stats) {
            this.position = position;
            this.name = name;
            this.stats = stats;
        }
    }
} 