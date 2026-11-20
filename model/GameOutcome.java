package model;

public enum GameOutcome {
    WON("Перемога"), LOST("Поразка"), ABANDONED("Перервано");
    private final String title;
    GameOutcome(String title) { this.title = title; }
    public String getTitle() { return title; }
}
