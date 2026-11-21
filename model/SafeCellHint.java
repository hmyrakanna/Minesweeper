package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SafeCellHint implements HintStrategy {
    private final Random random = new Random();
    @Override
    public CellPosition findHint(Board board) {
        if (!board.hasStarted() || board.getGameState() != GameState.PLAYING) return null;
        List<CellPosition> safe = new ArrayList<>(), empty = new ArrayList<>();
        for (int r = 0; r < board.getHeight(); r++) {
            for (int c = 0; c < board.getWidth(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isSafeClosed()) {
                    CellPosition p = new CellPosition(r, c);
                    safe.add(p);
                    if (cell.getAdjacentMines() == 0) empty.add(p);
                }
            }
        }
        List<CellPosition> candidates = empty.isEmpty() ? safe : empty;
        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }
}
