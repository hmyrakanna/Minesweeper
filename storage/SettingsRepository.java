package storage;

import java.io.IOException;
import java.nio.file.*;

public class SettingsRepository {
    private final Path path;
    public SettingsRepository(Path path) { this.path = path; }
    public String loadTheme() throws IOException {
        return Files.exists(path) && Files.readString(path).strip().equals("pastel") ? "pastel" : "twilight";
    }
    public void saveTheme(String theme) throws IOException { AtomicFiles.write(path, theme + "\n"); }
}
