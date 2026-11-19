package record;
import model.Difficulty;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class FileScoreRecord implements ScoreRecord {
    private final Path filePath;
    private final Map<Difficulty, Integer> bestTimes = new EnumMap<>(Difficulty.class);

    public FileScoreRecord() {
        this(Path.of("records.properties"));
    }

    public FileScoreRecord(String fileName) {
        this(Path.of(fileName));
    }

    public FileScoreRecord(Path filePath) {
        this.filePath = filePath;
        loadfromFile();
    }

    @Override
    public Optional<Integer> getBestTime(Difficulty difficulty) {
        if (difficulty == Difficulty.CUSTOM) {
            return Optional.empty();
        }
        return Optional.ofNullable(bestTimes.get(difficulty));
    }

    @Override
    public boolean saveIfBest(Difficulty difficulty, int seconds) {
        if (difficulty == Difficulty.CUSTOM || seconds < 0 || difficulty == null) {
            return false;
        }

        Integer currentBest = bestTimes.get(difficulty);
        if (currentBest == null || seconds < currentBest) {
            bestTimes.put(difficulty, seconds);
            if (saveToFile()) {
                return true;
            }
            if (currentBest == null) {
                bestTimes.remove(difficulty);
            } else {
                bestTimes.put(difficulty, currentBest);
            }
        }
        return false;
    }

    private void loadfromFile() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || !line.contains("=")) {
                        continue;
                    }
                    String[] parts = line.split("=", 2);
                    try {
                        Difficulty difficulty = Difficulty.valueOf(parts[0].trim());
                        int time = Integer.parseInt(parts[1].trim());
                        if (difficulty != Difficulty.CUSTOM && time >= 0) {
                            bestTimes.put(difficulty, time);
                        }
                    } catch (IllegalArgumentException e) {}
                }
            } catch (IOException e) {
            System.err.println("Error reading score record file: " + e.getMessage());
        }
    }

    private boolean saveToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (Map.Entry<Difficulty, Integer> entry : bestTimes.entrySet()) {
                writer.write(entry.getKey().name() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing score record file: " + e.getMessage());
            return false;
        }
        return true;
    }
}
