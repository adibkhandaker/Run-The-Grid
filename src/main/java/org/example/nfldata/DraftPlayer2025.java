package org.example.nfldata;

public class DraftPlayer2025 {
    private String round;
    private String pick;
    private String team;
    private String name;
    private String height;
    private String position;
    private String college;
    private String year;

    public DraftPlayer2025(String year, String round, String pick, String team, String name, String position, String height, String college) {
        this.year = year;
        this.round = round;
        this.pick = pick;
        this.team = team;
        this.name = name;
        this.height = height;
        this.position = position;
        this.college = college;
    }

    public String getYear() {
        return year;
    }

    public String getRound() {
        return round;
    }

    public String getPick() {
        return pick;
    }

    public String getTeam() {
        return team;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getHeight() {
        return height;
    }

    public String getCollege() {
        return college;
    }
}
