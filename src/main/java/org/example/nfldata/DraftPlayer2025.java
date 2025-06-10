package org.example.nfldata;

public class DraftPlayer2025 {
    private String round;
    private String pick;
    private String team;
    private String name;
    private String age;
    private String position;
    private String college;

    public DraftPlayer2025(String round, String pick, String team, String name, String position, String age, String college) {
        this.round = round;
        this.pick = pick;
        this.team = team;
        this.name = name;
        this.age = age;
        this.position = position;
        this.college = college;
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

    public String getAge() {
        return age;
    }

    public String getCollege() {
        return college;
    }
}
