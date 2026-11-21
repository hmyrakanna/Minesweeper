package service;

import model.*;
import storage.GameStorage;
import java.io.IOException;
import java.util.*;

public class StatisticsService {
    private final GameStorage storage;
    private final List<GameResult> history;
    private final Map<Difficulty, Integer> legacyRecords;

    public StatisticsService(GameStorage storage) throws IOException {
        this.storage = storage;
        history = storage.loadHistory();
        legacyRecords = storage.loadLegacyRecords();
    }
    public boolean contains(UUID id) {
        for (GameResult result : history) {
            if (result.id().equals(id)) return true;
        }
        return false;
    }
    public void record(GameResult result) throws IOException {
        if (contains(result.id())) return;
        List<GameResult> updated = new ArrayList<>(history);
        updated.add(result);
        storage.saveHistory(updated);
        history.add(result);
    }
    public List<GameResult> history() {
        List<GameResult> result = new ArrayList<>(history);
        result.sort(Comparator.comparing(GameResult::date).reversed());
        return result;
    }
    public List<GameResult> records(Difficulty difficulty) {
        List<GameResult> result = new ArrayList<>();
        for (GameResult game : history) {
            if (game.qualifiesForRecord() && game.config().difficulty() == difficulty) result.add(game);
        }
        result.sort(Comparator.comparingInt(GameResult::seconds).thenComparing(GameResult::date));
        return result.subList(0, Math.min(10, result.size()));
    }
    public boolean isNewRecord(GameResult result) {
        if (!result.qualifiesForRecord()) return false;
        Difficulty difficulty = result.config().difficulty();
        Integer legacy = legacyRecords.get(difficulty);
        if (legacy != null && legacy <= result.seconds()) return false;
        List<GameResult> top = records(difficulty);
        return top.isEmpty() || result.seconds() < top.get(0).seconds();
    }
    public Integer getLegacyRecord(Difficulty difficulty) { return legacyRecords.get(difficulty); }
    public Summary summary() {
        int wins = 0, losses = 0, abandoned = 0, streak = 0, bestStreak = 0, hints = 0;
        long seconds = 0;
        List<GameResult> ordered = new ArrayList<>(history);
        ordered.sort(Comparator.comparing(GameResult::date));
        for (GameResult r : ordered) {
            seconds += r.seconds();
            hints += r.hintsUsed();
            switch (r.outcome()) {
                case WON -> { wins++; streak++; bestStreak = Math.max(bestStreak, streak); }
                case LOST -> { losses++; streak = 0; }
                case ABANDONED -> { abandoned++; streak = 0; }
            }
        }
        return new Summary(wins, losses, abandoned, bestStreak, hints, seconds);
    }
    public record Summary(int wins, int losses, int abandoned, int bestStreak, int hints, long seconds) {
        public int completed() { return wins + losses; }
        public double winRate() { return completed() == 0 ? 0 : 100.0 * wins / completed(); }
    }
}
