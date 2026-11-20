package storage;

import java.io.IOException;
import java.util.Optional;

public interface GameSaveRepository {
    Optional<SavedGame> load() throws IOException;
    void save(SavedGame game) throws IOException;
    void clear() throws IOException;
}
