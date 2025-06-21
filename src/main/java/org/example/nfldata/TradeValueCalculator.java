package org.example.nfldata;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class TradeValueCalculator {

    public static int calculatePlayerValue(JSONObject player) {
        if (player == null) return 0;
        
        String playerId = (String) player.get("id");
        if (playerId == null) return 25; // Default for players with no ID

        try {
            // Get player stats from the 2024 season
            JSONObject eventLog = APIController.getPlayerStats(playerId, "2024");
            System.out.println("\n--- TradeValueCalculator Debug ---");
            System.out.println("Player ID: " + playerId);
            System.out.println("Event Log: " + (eventLog != null ? "Found" : "null"));

            if (eventLog == null) return 50; // Base value if stats not found

            // Extract position from player data
            String position = "Unknown";
            Object positionObj = player.get("position");
            if (positionObj instanceof JSONObject) {
                JSONObject positionJson = (JSONObject) positionObj;
                Object abbreviation = positionJson.get("abbreviation");
                if (abbreviation instanceof String) {
                    position = (String) abbreviation;
                }
            }

            // Parse events to get statistics
            JSONObject events = (JSONObject) eventLog.get("events");
            if (events == null) return 50;

            JSONArray items = (JSONArray) events.get("items");
            if (items == null || items.isEmpty()) return 50;

            // Aggregate stats from all games
            int totalGames = 0;
            double totalValue = 0;
            int gamesStarted = 0;
            
            for (Object eventObj : items) {
                JSONObject event = (JSONObject) eventObj;
                Boolean played = (Boolean) event.get("played");
                
                if (played != null && played) {
                    totalGames++;
                    
                    Object statisticsObj = event.get("statistics");
                    JSONObject stats = null;
                    if (statisticsObj instanceof JSONObject) {
                        JSONObject statisticsJson = (JSONObject) statisticsObj;
                        if (statisticsJson.containsKey("$ref")) {
                            String statisticsUrl = (String) statisticsJson.get("$ref");
                            try {
                                stats = APIController.getStatisticsFromUrl(statisticsUrl);
                            } catch (Exception e) {
                                System.out.println("Error fetching stats from URL: " + e.getMessage());
                            }
                        } else {
                            stats = statisticsJson;
                        }
                    }
                    
                    if (stats != null) {
                        totalValue += calculateGameValue(stats, position);
                    }
                }
            }

            if (totalGames == 0) return 50; // No games played

            // Calculate average value per game
            double averageValue = totalValue / totalGames;
            
            // Positional value multipliers (based on NFL importance)
            double positionalMultiplier = getPositionalMultiplier(position);
            
            // Playing time adjustment (starters worth more than backups)
            double playingTimeMultiplier = getPlayingTimeMultiplier(totalGames, position);
            
            // Elite performance bonus (nonlinear scaling)
            double performanceMultiplier = getPerformanceMultiplier(averageValue, position);
            
            // Calculate final value with all multipliers
            int finalValue = (int) Math.round(averageValue * totalGames * positionalMultiplier * playingTimeMultiplier * performanceMultiplier);
            
            System.out.println("Position: " + position + ", Games: " + totalGames + ", Avg Value: " + averageValue + 
                             ", Pos Multiplier: " + positionalMultiplier + ", Playing Time: " + playingTimeMultiplier + 
                             ", Performance: " + performanceMultiplier + ", Final Value: " + finalValue);
            return finalValue;

        } catch (Exception e) {
            System.out.println("Error calculating player value: " + e.getMessage());
            return 50; // Default value on error
        }
    }

    private static double calculateGameValue(JSONObject stats, String position) {
        if (stats == null) return 0.0;
        
        double value = 0.0;
        
        // Parse the nested ESPN API structure
        JSONObject splits = (JSONObject) stats.get("splits");
        if (splits == null) return 0.0;
        
        JSONArray categories = (JSONArray) splits.get("categories");
        if (categories == null) return 0.0;
        
        // Extract stats based on position
        switch (position) {
            case "QB":
                // Passing stats with efficiency bonuses
                double passYards = getStatValueFromCategories(categories, "passingYards");
                double passTDs = getStatValueFromCategories(categories, "passingTouchdowns");
                double interceptions = getStatValueFromCategories(categories, "interceptions");
                double completions = getStatValueFromCategories(categories, "completions");
                double attempts = getStatValueFromCategories(categories, "passingAttempts");
                
                value += passYards * 0.04; // 4 points per 100 yards
                value += passTDs * 4.0; // 4 points per TD
                value -= interceptions * 2.0; // -2 points per INT
                
                // Efficiency bonus
                if (attempts > 0) {
                    double completionRate = completions / attempts;
                    if (completionRate >= 0.7) value += 3.0; // High completion rate bonus
                    else if (completionRate >= 0.65) value += 1.5;
                }
                
                // Rushing stats
                value += getStatValueFromCategories(categories, "rushingYards") * 0.1;
                value += getStatValueFromCategories(categories, "rushingTouchdowns") * 6.0;
                break;
                
            case "RB":
                // Rushing stats with efficiency
                double rushYards = getStatValueFromCategories(categories, "rushingYards");
                double rushAttempts = getStatValueFromCategories(categories, "rushingAttempts");
                double rushTDs = getStatValueFromCategories(categories, "rushingTouchdowns");
                
                value += rushYards * 0.1; // 10 points per 100 yards
                value += rushTDs * 6.0; // 6 points per TD
                
                // Efficiency bonus (yards per carry)
                if (rushAttempts > 0) {
                    double ypc = rushYards / rushAttempts;
                    if (ypc >= 5.0) value += 5.0; // Elite efficiency
                    else if (ypc >= 4.5) value += 3.0; // Good efficiency
                    else if (ypc >= 4.0) value += 1.0; // Above average
                }
                
                // Receiving stats
                value += getStatValueFromCategories(categories, "receptions") * 1.0;
                value += getStatValueFromCategories(categories, "receivingYards") * 0.1;
                value += getStatValueFromCategories(categories, "receivingTouchdowns") * 6.0;
                break;
                
            case "WR":
            case "TE":
                // Receiving stats with efficiency
                double recYards = getStatValueFromCategories(categories, "receivingYards");
                double receptions = getStatValueFromCategories(categories, "receptions");
                double recTDs = getStatValueFromCategories(categories, "receivingTouchdowns");
                
                value += recYards * 0.1; // 10 points per 100 yards
                value += recTDs * 6.0; // 6 points per TD
                value += receptions * 1.0; // 1 point per reception
                
                // Efficiency bonus (yards per reception)
                if (receptions > 0) {
                    double ypr = recYards / receptions;
                    if (ypr >= 15.0) value += 3.0; // Elite efficiency
                    else if (ypr >= 12.0) value += 1.5; // Good efficiency
                }
                break;
                
            case "PK":
                // Kicking stats - scaled down significantly since kickers aren't worth much
                value += getStatValueFromCategories(categories, "fieldGoalsMade") * 0.5;
                value += getStatValueFromCategories(categories, "extraPointsMade") * 0.1;
                
                double fgAttempts = getStatValueFromCategories(categories, "fieldGoalsAttempted");
                double fgMade = getStatValueFromCategories(categories, "fieldGoalsMade");
                if (fgAttempts > 0) {
                    double fgPercentage = fgMade / fgAttempts;
                    if (fgPercentage >= 0.9) value += 1.0;
                    else if (fgPercentage >= 0.85) value += 0.5;
                }
                break;
                
            case "P":
                // Punting stats - very low value since punters are worth almost nothing
                value += getStatValueFromCategories(categories, "punts") * 0.1;
                value += getStatValueFromCategories(categories, "puntYards") * 0.001;
                
                double puntYards = getStatValueFromCategories(categories, "puntYards");
                double punts = getStatValueFromCategories(categories, "punts");
                if (punts > 0) {
                    double avgPuntDistance = puntYards / punts;
                    if (avgPuntDistance >= 50) value += 0.5;
                    else if (avgPuntDistance >= 45) value += 0.2;
                }
                break;
                
            default:
                // Comprehensive defensive stats with improved valuation
                double tackles = getStatValueFromCategories(categories, "totalTackles");
                double sacks = getStatValueFromCategories(categories, "sacks");
                double defensiveInts = getStatValueFromCategories(categories, "interceptions");
                double passesDefended = getStatValueFromCategories(categories, "passesDefended");
                double forcedFumbles = getStatValueFromCategories(categories, "forcedFumbles");
                double fumbleRecoveries = getStatValueFromCategories(categories, "fumbleRecoveries");
                double defensiveTDs = getStatValueFromCategories(categories, "defensiveTouchdowns");
                double safety = getStatValueFromCategories(categories, "safeties");
                double tacklesForLoss = getStatValueFromCategories(categories, "tacklesForLoss");
                double quarterbackHits = getStatValueFromCategories(categories, "quarterbackHits");
                
                value += tackles * 0.8; // 0.8 points per tackle (reduced from 1.0)
                value += sacks * 4.0; // 4 points per sack (increased from 3.0)
                value += defensiveInts * 5.0; // 5 points per INT (increased from 4.0)
                value += passesDefended * 2.0; // 2 points per PD (increased from 1.5)
                value += forcedFumbles * 3.0; // 3 points per forced fumble (increased from 2.0)
                value += fumbleRecoveries * 2.0; // 2 points per fumble recovery
                value += defensiveTDs * 10.0; // 10 points per defensive TD (increased from 8.0)
                value += safety * 6.0; // 6 points per safety (increased from 5.0)
                value += tacklesForLoss * 2.5; // 2.5 points per TFL (new stat)
                value += quarterbackHits * 1.5; // 1.5 points per QB hit (new stat)
                break;
        }
        
        return value;
    }
    
    private static double getStatValueFromCategories(JSONArray categories, String statName) {
        for (Object categoryObj : categories) {
            JSONObject category = (JSONObject) categoryObj;
            JSONArray stats = (JSONArray) category.get("stats");
            if (stats != null) {
                for (Object statObj : stats) {
                    JSONObject stat = (JSONObject) statObj;
                    String name = (String) stat.get("name");
                    if (statName.equals(name)) {
                        Object value = stat.get("value");
                        if (value instanceof Number) {
                            return ((Number) value).doubleValue();
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    private static double getStatValue(JSONObject statistics, String statName, double multiplier) {
        try {
            Object value = statistics.get(statName);
            if (value instanceof Number) {
                return ((Number) value).doubleValue() * multiplier;
            }
        } catch (Exception e) {
            // Ignore missing stats
        }
        return 0;
    }

    public static int calculatePickValue(int pickNumber) {
        if (pickNumber <= 0) return 0;
        // More realistic draft value chart - first round picks are very valuable
        if (pickNumber <= 5) return 300; // Top 5 picks (elite prospects)
        else if (pickNumber <= 10) return 250; // Top 10 picks
        else if (pickNumber <= 20) return 200; // Mid-first round
        else if (pickNumber <= 32) return 150; // Late first round
        else if (pickNumber <= 48) return 100; // Early second round
        else if (pickNumber <= 64) return 75; // Mid-second round
        else if (pickNumber <= 80) return 50; // Late second round
        else if (pickNumber <= 96) return 35; // Third round
        else if (pickNumber <= 128) return 20; // Fourth round
        else if (pickNumber <= 160) return 12; // Fifth round
        else if (pickNumber <= 192) return 8; // Sixth round
        else return 5; // Seventh round
    }

    private static double getPositionalMultiplier(String position) {
        switch (position) {
            case "QB": return 1.3; // Most valuable position (reduced from 1.5)
            case "WR": return 0.9; // High value skill position (reduced from 1.1)
            case "RB": return 0.7; // Important but replaceable (reduced from 0.9)
            case "TE": return 0.8; // Valuable but less than WR (reduced from 0.9)
            case "CB": return 1.4; // Premium defensive position
            case "S": return 1.3; // Important defensive position
            case "LB": return 1.2; // Solid defensive position
            case "DE": return 1.4; // Premium pass rusher
            case "DT": return 1.3; // Important but less glamorous
            case "OL": return 0.7; // Important but hard to evaluate
            case "PK": return 0.2; // Low value
            case "P": return 0.1; // Very low value
            default: return 0.8; // Unknown position
        }
    }
    
    private static double getPlayingTimeMultiplier(int gamesPlayed, String position) {
        if (gamesPlayed >= 15) return 1.0; // Full season starter (reduced from 1.05)
        else if (gamesPlayed >= 10) return 0.95; // Regular contributor (reduced from 1.0)
        else if (gamesPlayed >= 5) return 0.8; // Part-time player
        else return 0.5; // Minimal playing time
    }
    
    private static double getPerformanceMultiplier(double averageValue, String position) {
        // Much more conservative performance bonuses for offensive players
        if (averageValue >= 40) return 1.2; // Elite (reduced from 1.3)
        else if (averageValue >= 30) return 1.1; // Very good (reduced from 1.15)
        else if (averageValue >= 20) return 1.0; // Good (reduced from 1.05)
        else if (averageValue >= 10) return 0.95; // Average (reduced from 1.0)
        else return 0.8; // Below average
    }
} 