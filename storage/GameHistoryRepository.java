package storage;

import model.GameResult;
import java.io.IOException;
import java.util.List;

public interface GameHistoryRepository {
    List<GameResult> load() throws IOException;
    void save(List<GameResult> history) throws IOException;
}
