package storage;

import model.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public class FileGameHistoryRepository implements GameHistoryRepository {
    private final Path path;
    private static final String HEADER = "id,date,difficulty,width,height,mines,outcome,seconds,hints,revealed";
    public FileGameHistoryRepository(Path path) { this.path = path; }
    public List<GameResult> load() throws IOException {
        List<GameResult> results = new ArrayList<>();
        if (!Files.exists(path)) return results;
        Set<UUID> ids = new HashSet<>();
        for (String line : Files.readAllLines(path)) {
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
    public void save(List<GameResult> history) throws IOException {
        StringBuilder content = new StringBuilder(HEADER).append('\n');
        for (GameResult r : history) {
            GameConfig c = r.config();
            content.append(String.join(",", r.id().toString(), r.date().toString(), c.difficulty().name(),
                    "" + c.width(), "" + c.height(), "" + c.mines(), r.outcome().name(),
                    "" + r.seconds(), "" + r.hintsUsed(), "" + r.revealed())).append('\n');
        }
        AtomicFiles.write(path, content.toString());
    }
}
