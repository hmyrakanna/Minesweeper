package service;

import model.*;
import storage.GameHistoryRepository;
import java.io.IOException;
import java.util.*;

public class StatisticsService {
    private final GameHistoryRepository repository;
    private final List<GameResult> history;
    public StatisticsService(GameHistoryRepository repository) throws IOException {
        this.repository = repository;
        history = new ArrayList<>(repository.load());
    }
    public boolean contains(UUID id) { return history.stream().anyMatch(r -> r.id().equals(id)); }
    public void record(GameResult result) throws IOException {
        if (contains(result.id())) return;
        List<GameResult> updated = new ArrayList<>(history);
        updated.add(result);
        repository.save(updated);
        history.add(result);
    }
    public List<GameResult> history() {
        return history.stream().sorted(Comparator.comparing(GameResult::date).reversed()).toList();
    }
    public List<GameResult> records(Difficulty difficulty) {
        return history.stream().filter(r -> r.qualifiesForRecord() && r.config().difficulty() == difficulty)
                .sorted(Comparator.comparingInt(GameResult::seconds).thenComparing(GameResult::date)).limit(10).toList();
    }
    public boolean isNewRecord(GameResult result) {
        return result.qualifiesForRecord() && records(result.config().difficulty()).stream()
                .noneMatch(r -> r.seconds() <= result.seconds());
    }
    public Summary summary() {
        int wins = 0, losses = 0, abandoned = 0, streak = 0, bestStreak = 0, hints = 0;
        long seconds = 0;
        for (GameResult r : history.stream().sorted(Comparator.comparing(GameResult::date)).toList()) {
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
