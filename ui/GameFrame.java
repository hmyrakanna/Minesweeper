package ui;

import controller.*;
import model.*;
import service.StatisticsService;
import storage.*;
import ui.components.*;
import ui.dialogs.*;
import ui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;

public class GameFrame extends JFrame implements GameController.GameEventListener {
    private final GameStorage storage = new GameStorage(Path.of("data"));
    private StatisticsService statistics;
    private GameTheme theme = new TwilightTheme();
    private GameController controller;
    private JPanel rootPanel;
    private RoundedPanel navigation;
    private JLabel titleLabel;
    private RoundedButton statsButton, settingsButton, saveButton;
    private GameHeader gameHeader;
    private DifficultyPanel difficultyPanel;
    private BoardPanel boardPanel;

    public GameFrame() {
        super("Сапер");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(950, 650));
        setSize(1120, 780);
        loadSettingsAndHistory();
        initUi();
        installWindowEvents();
        restoreOrStart();
        setLocationRelativeTo(null);
    }

    private void loadSettingsAndHistory() {
        try {
            if (storage.loadTheme().equals("pastel")) theme = new PastelLilacTheme();
        } catch (IOException e) {
            laterError("Не вдалося завантажити тему");
        }
        try {
            statistics = new StatisticsService(storage);
        } catch (IOException e) {
            laterError("Не вдалося прочитати історію. Файл залишено без змін.");
        }
    }

    private void initUi() {
        rootPanel = new JPanel(new BorderLayout(14, 14));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(rootPanel);
        navigation = new RoundedPanel(18, theme);
        navigation.setLayout(new BorderLayout(12, 0));
        navigation.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 14));
        titleLabel = new JLabel("✦ САПЕР");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        navigation.add(titleLabel, BorderLayout.WEST);

        saveButton = menuButton("Зберегти", 110, this::saveGame);
        statsButton = menuButton("Статистика", 128, this::showStatistics);
        settingsButton = menuButton("Налаштування", 146, this::showSettings);
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        menu.setOpaque(false);
        menu.add(saveButton);
        menu.add(statsButton);
        menu.add(settingsButton);
        navigation.add(menu, BorderLayout.EAST);
        rootPanel.add(navigation, BorderLayout.NORTH);

        gameHeader = new GameHeader(theme, this::restartGame, this::togglePause, this::requestHint);
        boardPanel = new BoardPanel(theme, this::handleCellClick, this::togglePause);
        difficultyPanel = new DifficultyPanel(theme, this::selectDifficulty);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(gameHeader, BorderLayout.NORTH);
        center.add(boardPanel, BorderLayout.CENTER);
        rootPanel.add(center, BorderLayout.CENTER);
        rootPanel.add(difficultyPanel, BorderLayout.EAST);
        applyTheme();
    }

    private RoundedButton menuButton(String title, int width, Runnable action) {
        RoundedButton button = new RoundedButton(title, 12, theme);
        button.setPreferredSize(new Dimension(width, 40));
        button.addActionListener(e -> action.run());
        return button;
    }

    private void installWindowEvents() {
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { closeGame(); }
            @Override public void windowIconified(WindowEvent e) {
                if (controller != null) controller.addPause(PauseReason.MINIMIZED);
            }
            @Override public void windowDeiconified(WindowEvent e) {
                if (controller != null) controller.removePause(PauseReason.MINIMIZED);
            }
        });
    }

    private void restoreOrStart() {
        try {
            SavedGame saved = storage.loadGame();
            if (saved != null) {
                if (statistics == null || !statistics.contains(saved.id())) {
                    installController(new GameController(saved));
                    return;
                }
                storage.clearGame();
            }
        } catch (IOException e) {
            laterError("Не вдалося відновити збережену гру");
        }
        installController(new GameController(GameConfig.preset(Difficulty.BEGINNER)));
    }

    private void openDialog(StyledDialog dialog) {
        GameController current = controller;
        boolean addedPause = current.addPause(PauseReason.DIALOG);
        try {
            dialog.showDialog();
        } finally {
            if (addedPause) current.removePause(PauseReason.DIALOG);
        }
    }
    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(this, theme);
        openDialog(dialog);
        if (dialog.getSelectedTheme() != null) {
            theme = dialog.getSelectedTheme();
            applyTheme();
            try {
                storage.saveTheme(theme instanceof PastelLilacTheme ? "pastel" : "twilight");
            } catch (IOException e) { message("Помилка", "Не вдалося зберегти тему"); }
        }
    }
    private void showStatistics() {
        if (statistics == null) {
            message("Статистика", "Історія недоступна. Перевірте файл data/history.csv.");
            return;
        }
        openDialog(new StatisticsDialog(this, theme, statistics));
    }
    private void selectDifficulty(Difficulty difficulty) {
        if (difficulty == Difficulty.CUSTOM) {
            CustomBoardDialog dialog = new CustomBoardDialog(this, theme, controller.getSession().getConfig());
            openDialog(dialog);
            if (dialog.getConfig() != null) startNewGame(dialog.getConfig());
        } else startNewGame(GameConfig.preset(difficulty));
    }
    private void restartGame() { startNewGame(controller.getSession().getConfig()); }
    private boolean hasProgress() {
        Board board = controller.getBoard();
        return controller.hasStarted() || board.getRemainingMines() != board.getTotalMines();
    }
    private void startNewGame(GameConfig config) {
        if (controller.isPlaying() && hasProgress()
                && !confirm("Нова гра", "Поточна партія буде завершена як перервана.", "Почати нову")) return;
        try {
            storage.clearGame();
        } catch (IOException e) {
            message("Помилка", "Не вдалося видалити попереднє збереження.");
            return;
        }
        if (controller.isPlaying() && controller.hasStarted()) {
            recordResult(GameResult.from(controller.getSession(), GameOutcome.ABANDONED, controller.getSecondsElapsed()));
        }
        installController(new GameController(config));
    }
    private void installController(GameController next) {
        if (controller != null) controller.dispose();
        controller = next;
        difficultyPanel.setSelected(next.getSession().getConfig().difficulty());
        boardPanel.setBoard(next.getBoard());
        controller.setEventListener(this);
        revalidate();
        repaint();
    }
    private void togglePause() { controller.togglePause(); }
    private void requestHint() {
        if (!controller.requestHint()) {
            message("Підказка", "Немає доступних безпечних клітинок без прапорців.");
        }
    }
    private void handleCellClick(int row, int col, MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) controller.handleRightClick(row, col);
        else if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isShiftDown() || e.getClickCount() == 2) controller.handleChordClick(row, col);
            else controller.handleCellClick(row, col);
        }
    }
    private void saveGame() {
        if (!controller.isPlaying()) return;
        boolean addedPause = controller.addPause(PauseReason.DIALOG);
        try {
            storage.saveGame(controller.snapshot());
            message("Гру збережено", "Після наступного запуску можна продовжити партію.");
        } catch (IOException e) {
            message("Помилка", "Не вдалося зберегти гру.");
        } finally {
            if (addedPause) controller.removePause(PauseReason.DIALOG);
        }
    }
    private void closeGame() {
        if (controller.isPlaying()) {
            controller.addPause(PauseReason.DIALOG);
            try {
                if (hasProgress()) {
                    storage.saveGame(controller.snapshot());
                } else {
                    storage.clearGame();
                }
            } catch (IOException e) {
                if (!confirm("Помилка збереження", "Закрити гру без збереження?", "Закрити")) {
                    controller.removePause(PauseReason.DIALOG);
                    return;
                }
            }
        }
        dispose();
    }
    private boolean confirm(String title, String text, String accept) {
        StyledDialog dialog = new StyledDialog(this, title, text, theme);
        dialog.addConfirmation(accept);
        openDialog(dialog);
        return dialog.isConfirmed();
    }
    private void message(String title, String text) {
        StyledDialog dialog = new StyledDialog(this, title, text, theme);
        dialog.addActions(dialog.actionButton("Добре", true, dialog::dispose));
        openDialog(dialog);
    }
    private void laterError(String text) { SwingUtilities.invokeLater(() -> message("Помилка", text)); }
    private boolean recordResult(GameResult result) {
        if (statistics == null) return false;
        boolean personalBest = statistics.isNewRecord(result);
        try {
            statistics.record(result);
            return personalBest;
        } catch (IOException e) {
            message("Помилка", "Не вдалося зберегти результат гри.");
            return false;
        }
    }
    private void applyTheme() {
        rootPanel.setBackground(theme.getWindowBackground());
        navigation.setTheme(theme);
        titleLabel.setForeground(theme.getAccentPrimary());
        statsButton.setTheme(theme);
        settingsButton.setTheme(theme);
        saveButton.setTheme(theme);
        gameHeader.setTheme(theme);
        difficultyPanel.setTheme(theme);
        boardPanel.setTheme(theme);
        repaint();
    }
    @Override public void onTimerTick(int seconds) { gameHeader.updateTime(seconds); }
    @Override public void onSessionChanged() {
        Board board = controller.getBoard();
        GameSession session = controller.getSession();
        boardPanel.refresh(board);
        boardPanel.setPaused(controller.isPaused(), controller.isManuallyPaused());
        boardPanel.highlight(session.getHint());
        gameHeader.updateMines(board.getRemainingMines());
        gameHeader.updateActions(controller.isPlaying(), controller.isPaused(), controller.canHint(), session.getHintsRemaining());
        saveButton.setEnabled(controller.isPlaying());
    }
    @Override public void onGameStatusChanged(GameState state) {
        if (state == GameState.PLAYING) return;
        Board board = controller.getBoard();
        GameResult result = GameResult.from(controller.getSession(),
                state == GameState.WON ? GameOutcome.WON : GameOutcome.LOST, controller.getSecondsElapsed());
        boolean newRecord = recordResult(result);
        try { storage.clearGame(); }
        catch (IOException e) { message("Помилка", "Не вдалося очистити збереження завершеної гри."); }
        new GameResultDialog(this, theme, state, result.seconds(), board.getRevealedCellsCount(),
                result.config().safeCells(), newRecord, result.hintsUsed(), this::restartGame).showDialog();
    }
    @Override public void dispose() {
        if (controller != null) controller.dispose();
        super.dispose();
    }
}
