package model;

import java.time.Instant;
import java.util.UUID;

public record GameResult(UUID id, Instant date, GameConfig config, GameOutcome outcome,
                         int seconds, int hintsUsed, int revealed) {
    public GameResult {
        if (id == null || date == null || config == null || outcome == null || seconds < 0
                || hintsUsed < 0 || hintsUsed > GameSession.MAX_HINTS || revealed < 0 || revealed > config.safeCells()) {
            throw new IllegalArgumentException("Некоректний результат гри");
        }
    }
    public static GameResult from(GameSession session, GameOutcome outcome, int seconds) {
        return new GameResult(session.getId(), Instant.now(), session.getConfig(), outcome,
                seconds, session.getHintsUsed(), session.getBoard().getRevealedCellsCount());
    }
    public boolean qualifiesForRecord() {
        return outcome == GameOutcome.WON && hintsUsed == 0 && config.difficulty() != Difficulty.CUSTOM;
    }
}
