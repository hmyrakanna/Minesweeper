package controller;

import model.*;
import storage.SavedGame;
import java.util.EnumSet;

public class GameController {
    private final GameSession session;
    private final GameTimer timer;
    private final HintStrategy hintStrategy;
    private final EnumSet<PauseReason> pauseReasons = EnumSet.noneOf(PauseReason.class);
    private GameEventListener eventListener;

    public interface GameEventListener {
        void onTimerTick(int seconds);
        void onGameStatusChanged(GameState state);
        default void onSessionChanged() {}
    }

    public GameController(GameConfig config) {
        this(new GameSession(config), 0, false, new SafeCellHint());
    }
    public GameController(SavedGame saved) {
        this(saved.restore(), saved.elapsedMillis(), true, new SafeCellHint());
    }
    public GameController(GameSession session, long elapsedMillis, boolean paused, HintStrategy hintStrategy) {
        this.session = session;
        this.hintStrategy = hintStrategy;
        timer = new GameTimer(this::publishTime);
        timer.reset(elapsedMillis);
        if (paused) pauseReasons.add(PauseReason.MANUAL);
        updateTimerState();
    }

    public void setEventListener(GameEventListener listener) {
        eventListener = listener;
        publishTime();
        publishSession();
    }
    public GameSession getSession() { return session; }
    public Board getBoard() { return session.getBoard(); }
    public int getSecondsElapsed() { return timer.elapsedSeconds(); }
    public boolean isPaused() { return !pauseReasons.isEmpty(); }
    public boolean isManuallyPaused() { return pauseReasons.contains(PauseReason.MANUAL); }
    public boolean isPlaying() { return getBoard().getGameState() == GameState.PLAYING; }
    public boolean hasStarted() { return getBoard().hasStarted(); }

    public boolean addPause(PauseReason reason) {
        if (!isPlaying() || !pauseReasons.add(reason)) return false;
        updateTimerState();
        publishSession();
        return true;
    }
    public void removePause(PauseReason reason) {
        if (pauseReasons.remove(reason)) {
            updateTimerState();
            publishSession();
        }
    }
    public void togglePause() {
        if (isManuallyPaused()) removePause(PauseReason.MANUAL);
        else addPause(PauseReason.MANUAL);
    }
    public boolean canHint() {
        return isPlaying() && !isPaused() && hasStarted() && session.getHintsRemaining() > 0 && session.getHint() == null;
    }
    public boolean requestHint() {
        if (!canHint()) return false;
        CellPosition position = hintStrategy.findHint(getBoard());
        if (!session.useHint(position)) return false;
        publishSession();
        return true;
    }
    public void handleCellClick(int row, int col) {
        if (!isPlaying() || isPaused()) return;
        Cell cell = getBoard().getCell(row, col);
        if (!cell.isClosed()) return;
        if (!hasStarted()) timer.start();
        getBoard().openCell(row, col);
        afterMove();
    }
    public void handleRightClick(int row, int col) {
        if (!isPlaying() || isPaused()) return;
        getBoard().toggleFlag(row, col);
        afterMove();
    }
    public void handleChordClick(int row, int col) {
        if (!isPlaying() || isPaused()) return;
        getBoard().chord(row, col);
        afterMove();
    }
    private void afterMove() {
        session.refreshHint();
        updateTimerState();
        publishSession();
        if (!isPlaying() && eventListener != null) eventListener.onGameStatusChanged(getBoard().getGameState());
    }
    private void updateTimerState() {
        if (isPlaying() && hasStarted() && !isPaused()) timer.start();
        else timer.pause();
        publishTime();
    }
    private void publishTime() { if (eventListener != null) eventListener.onTimerTick(getSecondsElapsed()); }
    private void publishSession() { if (eventListener != null) eventListener.onSessionChanged(); }
    public SavedGame snapshot() { return SavedGame.from(session, timer.elapsedMillis()); }
    public void dispose() { timer.pause(); eventListener = null; }
}
