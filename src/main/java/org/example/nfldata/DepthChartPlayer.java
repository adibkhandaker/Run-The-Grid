package org.example.nfldata;

public class DepthChartPlayer {
    private String name;
    private long age;
    private double weight;
    private String position;
    private long rank;
    private String DOB;
    private long debut;

    public DepthChartPlayer(String name, long age, double weight, String position, long rank, String DOB, long debut) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.position = position;
        this.rank = rank;
        this.DOB = DOB;
        this.debut = debut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }
    public String getDOB() {
        return DOB;
    }
    public void setDOB(String DOB) {
        this.DOB = DOB;
    }
    public long getDebut() {
        return debut;
    }
    public void setDebut(long debut) {
        this.debut = debut;
    }
}
