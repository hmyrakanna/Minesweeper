package storage;

import model.*;
import java.util.UUID;

public record SavedGame(UUID id, GameConfig config, boolean started, String cells,
                        long elapsedMillis, int hintsUsed, CellPosition hint) {
    public SavedGame {
        if (elapsedMillis < 0 || elapsedMillis > Long.MAX_VALUE / 1_000_000L
                || !started && elapsedMillis != 0) throw new IllegalArgumentException("Некоректний час");
    }
    public static SavedGame from(GameSession session, long elapsedMillis) {
        return new SavedGame(session.getId(), session.getConfig(), session.getBoard().hasStarted(),
                session.getBoard().snapshot(), elapsedMillis, session.getHintsUsed(), session.getHint());
    }
    public GameSession restore() {
        return GameSession.restore(id, config, started, cells, hintsUsed, hint);
    }
}
