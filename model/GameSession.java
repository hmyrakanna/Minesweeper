package model;

import java.util.UUID;

public class GameSession {
    public static final int MAX_HINTS = 3;
    private final UUID id;
    private final GameConfig config;
    private final Board board;
    private int hintsUsed;
    private CellPosition hint;

    public GameSession(GameConfig config) {
        this(UUID.randomUUID(), config, new Board(config.width(), config.height(), config.mines(), new FirstClick()), 0, null);
    }

    private GameSession(UUID id, GameConfig config, Board board, int hintsUsed, CellPosition hint) {
        this.id = id;
        this.config = config;
        this.board = board;
        this.hintsUsed = hintsUsed;
        this.hint = hint;
    }

    public static GameSession restore(UUID id, GameConfig config, boolean started, String cells,
                                      int hintsUsed, CellPosition hint) {
        Board board = Board.restore(config, started, cells);
        if (hintsUsed < 0 || hintsUsed > MAX_HINTS || !started && hintsUsed != 0
                || hint != null && (hintsUsed == 0 || !isSafeTarget(board, hint))) {
            throw new IllegalArgumentException("Некоректні підказки");
        }
        return new GameSession(id, config, board, hintsUsed, hint);
    }
    public UUID getId() { return id; }
    public GameConfig getConfig() { return config; }
    public Board getBoard() { return board; }
    public int getHintsUsed() { return hintsUsed; }
    public int getHintsRemaining() { return MAX_HINTS - hintsUsed; }
    public CellPosition getHint() { return hint; }
    public boolean useHint(CellPosition position) {
        if (!board.hasStarted() || board.getGameState() != GameState.PLAYING
                || hintsUsed >= MAX_HINTS || hint != null || !isSafeTarget(board, position)) return false;
        hint = position;
        hintsUsed++;
        return true;
    }
    public void refreshHint() {
        if (hint != null && (board.getGameState() != GameState.PLAYING || !isSafeTarget(board, hint))) hint = null;
    }
    private static boolean isSafeTarget(Board board, CellPosition p) {
        if (p == null || p.row() < 0 || p.row() >= board.getHeight()
                || p.col() < 0 || p.col() >= board.getWidth()) return false;
        Cell cell = board.getCell(p.row(), p.col());
        return !cell.isMine() && !cell.isFlagged() && !cell.isRevealed();
    }
}
