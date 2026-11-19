package record;

import model.Difficulty;
import java.util.Optional;

public interface ScoreRecord {
    Optional<Integer> getBestTime(Difficulty difficulty);
    boolean saveIfBest(Difficulty difficulty, int seconds);
}
