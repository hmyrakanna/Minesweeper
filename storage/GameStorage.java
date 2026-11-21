package storage;

import model.*;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public class GameStorage {
    private final Path savedGame, historyFile, themeFile, legacyFile;
    private static final String HEADER = "id,date,difficulty,width,height,mines,outcome,seconds,hints,revealed";

    public GameStorage(Path directory) {
        savedGame = directory.resolve("saved-game.properties");
        historyFile = directory.resolve("history.csv");
        themeFile = directory.resolve("theme.txt");
        legacyFile = directory.resolveSibling("records.properties");
    }

    public SavedGame loadGame() throws IOException {
        if (!Files.exists(savedGame)) return null;
        try {
            Properties p = readProperties(savedGame);
            if (!"1".equals(p.getProperty("version"))) throw new IllegalArgumentException("Невідома версія");
            GameConfig config = new GameConfig(Difficulty.valueOf(p.getProperty("difficulty")),
                    number(p, "width"), number(p, "height"), number(p, "mines"));
            CellPosition hint = null;
            if (p.containsKey("hintRow")) {
                hint = new CellPosition(number(p, "hintRow"), number(p, "hintCol"));
            }
            SavedGame game = new SavedGame(UUID.fromString(p.getProperty("id")), config,
                    Boolean.parseBoolean(p.getProperty("started")), p.getProperty("cells"),
                    Long.parseLong(p.getProperty("elapsed")), number(p, "hints"), hint);
            game.restore();
            return game;
        } catch (RuntimeException e) {
            throw new IOException("Не вдалося прочитати збережену гру", e);
        }
    }

    public void saveGame(SavedGame game) throws IOException {
        Properties p = new Properties();
        p.setProperty("version", "1");
        p.setProperty("id", game.id().toString());
        p.setProperty("difficulty", game.config().difficulty().name());
        p.setProperty("width", String.valueOf(game.config().width()));
        p.setProperty("height", String.valueOf(game.config().height()));
        p.setProperty("mines", String.valueOf(game.config().mines()));
        p.setProperty("started", String.valueOf(game.started()));
        p.setProperty("cells", game.cells());
        p.setProperty("elapsed", String.valueOf(game.elapsedMillis()));
        p.setProperty("hints", String.valueOf(game.hintsUsed()));
        if (game.hint() != null) {
            p.setProperty("hintRow", String.valueOf(game.hint().row()));
            p.setProperty("hintCol", String.valueOf(game.hint().col()));
        }
        StringWriter writer = new StringWriter();
        p.store(writer, "Minesweeper");
        write(savedGame, writer.toString());
    }

    public void clearGame() throws IOException {
        Files.deleteIfExists(savedGame);
    }

    public List<GameResult> loadHistory() throws IOException {
        List<GameResult> results = new ArrayList<>();
        if (!Files.exists(historyFile)) return results;
        Set<UUID> ids = new HashSet<>();
        for (String line : Files.readAllLines(historyFile)) {
            if (line.isBlank() || line.equals(HEADER)) continue;
            try {
                String[] f = line.split(",", -1);
                if (f.length != 10) throw new IllegalArgumentException("Неправильний рядок");
                GameConfig config = new GameConfig(Difficulty.valueOf(f[2]), Integer.parseInt(f[3]),
                        Integer.parseInt(f[4]), Integer.parseInt(f[5]));
                GameResult result = new GameResult(UUID.fromString(f[0]), Instant.parse(f[1]), config,
                        GameOutcome.valueOf(f[6]), Integer.parseInt(f[7]), Integer.parseInt(f[8]), Integer.parseInt(f[9]));
                if (ids.add(result.id())) results.add(result);
            } catch (RuntimeException e) {
                throw new IOException("Пошкоджений файл історії", e);
            }
        }
        return results;
    }

    public void saveHistory(List<GameResult> history) throws IOException {
        StringBuilder text = new StringBuilder(HEADER).append('\n');
        for (GameResult r : history) {
            GameConfig c = r.config();
            text.append(r.id()).append(',').append(r.date()).append(',')
                    .append(c.difficulty()).append(',').append(c.width()).append(',').append(c.height()).append(',')
                    .append(c.mines()).append(',').append(r.outcome()).append(',').append(r.seconds()).append(',')
                    .append(r.hintsUsed()).append(',').append(r.revealed()).append('\n');
        }
        write(historyFile, text.toString());
    }

    public String loadTheme() throws IOException {
        if (Files.exists(themeFile) && Files.readString(themeFile).strip().equals("pastel")) return "pastel";
        return "twilight";
    }

    public void saveTheme(String theme) throws IOException {
        write(themeFile, theme + "\n");
    }

    public Map<Difficulty, Integer> loadLegacyRecords() {
        Map<Difficulty, Integer> records = new EnumMap<>(Difficulty.class);
        if (!Files.exists(legacyFile)) return records;
        try {
            Properties p = readProperties(legacyFile);
            for (Difficulty difficulty : Difficulty.values()) {
                if (difficulty == Difficulty.CUSTOM || !p.containsKey(difficulty.name())) continue;
                try {
                    int time = number(p, difficulty.name());
                    if (time >= 0) records.put(difficulty, time);
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Не вдалося прочитати старі рекорди: " + e.getMessage());
        }
        return records;
    }

    private Properties readProperties(Path file) throws IOException {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) { p.load(reader); }
        return p;
    }

    private int number(Properties p, String key) {
        return Integer.parseInt(p.getProperty(key));
    }

    private void write(Path file, String content) throws IOException {
        Path destination = file.toAbsolutePath();
        Files.createDirectories(destination.getParent());
        Path temp = Files.createTempFile(destination.getParent(), "minesweeper-", ".tmp");
        try {
            Files.writeString(temp, content);
            try {
                Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
