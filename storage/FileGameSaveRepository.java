package storage;

import model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileGameSaveRepository implements GameSaveRepository {
    private final Path path;
    public FileGameSaveRepository(Path path) { this.path = path; }

    public Optional<SavedGame> load() throws IOException {
        if (!Files.exists(path)) return Optional.empty();
        try (Reader reader = Files.newBufferedReader(path)) {
            Properties p = new Properties();
            p.load(reader);
            if (!"1".equals(p.getProperty("version"))) throw new IllegalArgumentException("Невідома версія");
            GameConfig config = new GameConfig(Difficulty.valueOf(p.getProperty("difficulty")),
                    Integer.parseInt(p.getProperty("width")), Integer.parseInt(p.getProperty("height")),
                    Integer.parseInt(p.getProperty("mines")));
            CellPosition hint = p.containsKey("hintRow") ? new CellPosition(
                    Integer.parseInt(p.getProperty("hintRow")), Integer.parseInt(p.getProperty("hintCol"))) : null;
            SavedGame game = new SavedGame(UUID.fromString(p.getProperty("id")), config,
                    Boolean.parseBoolean(p.getProperty("started")), p.getProperty("cells"),
                    Long.parseLong(p.getProperty("elapsed")), Integer.parseInt(p.getProperty("hints")), hint);
            game.restore(); // Validate the entire snapshot before returning.
            return Optional.of(game);
        } catch (RuntimeException e) {
            throw new IOException("Не вдалося прочитати збережену гру", e);
        }
    }
    public void save(SavedGame game) throws IOException {
        Properties p = new Properties();
        p.setProperty("version", "1");
        p.setProperty("id", game.id().toString());
        p.setProperty("difficulty", game.config().difficulty().name());
        p.setProperty("width", "" + game.config().width());
        p.setProperty("height", "" + game.config().height());
        p.setProperty("mines", "" + game.config().mines());
        p.setProperty("started", "" + game.started());
        p.setProperty("cells", game.cells());
        p.setProperty("elapsed", "" + game.elapsedMillis());
        p.setProperty("hints", "" + game.hintsUsed());
        if (game.hint() != null) {
            p.setProperty("hintRow", "" + game.hint().row());
            p.setProperty("hintCol", "" + game.hint().col());
        }
        StringWriter writer = new StringWriter();
        p.store(writer, "Minesweeper");
        AtomicFiles.write(path, writer.toString());
    }
    public void clear() throws IOException { Files.deleteIfExists(path); }
}
