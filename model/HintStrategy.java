package model;

import java.util.Optional;

public interface HintStrategy {
    Optional<CellPosition> findHint(Board board);
}
