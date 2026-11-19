package ui;

import controller.GameController;
import model.Board;
import model.Difficulty;
import model.GameState;
import record.FileScoreRecord;
import record.ScoreRecord;
import ui.components.*;
import ui.dialogs.GameDialogs;
import ui.dialogs.GameResultDialog;
import ui.theme.GameTheme;
import ui.theme.TwilightTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class GameFrame extends JFrame implements GameController.GameEventListener {
    private final ScoreRecord scoreRecord = new FileScoreRecord();
    private GameTheme theme = new TwilightTheme();
    private Difficulty currentDifficulty = Difficulty.BEGINNER;
    private GameController controller;
    private JPanel rootPanel;
    private RoundedPanel navigation;
    private JLabel titleLabel;
    private RoundedButton recordsButton, settingsButton;
    private GameHeader gameHeader;
    private DifficultyPanel difficultyPanel;
    private BoardPanel boardPanel;

    public GameFrame() {
        super("Сапер");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        initUi();
        selectDifficulty(Difficulty.BEGINNER);
        setSize(1080, 760);
        setLocationRelativeTo(null);
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

        recordsButton = new RoundedButton("Рекорди", 12, theme);
        recordsButton.setPreferredSize(new Dimension(124, 40));
        recordsButton.addActionListener(e -> GameDialogs.records(this, theme, scoreRecord));
        settingsButton = new RoundedButton("Налаштування", 12, theme);
        settingsButton.setPreferredSize(new Dimension(146, 40));
        settingsButton.addActionListener(e -> GameDialogs.settings(this, theme).ifPresent(newTheme -> {
            theme = newTheme;
            applyTheme();
        }));
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        menu.setOpaque(false);
        menu.add(recordsButton);
        menu.add(settingsButton);
        navigation.add(menu, BorderLayout.EAST);
        rootPanel.add(navigation, BorderLayout.NORTH);

        gameHeader = new GameHeader(theme, this::restartGame);
        boardPanel = new BoardPanel(theme, this::handleCellClick);
        difficultyPanel = new DifficultyPanel(theme, this::selectDifficulty);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(gameHeader, BorderLayout.NORTH);
        center.add(boardPanel, BorderLayout.CENTER);
        rootPanel.add(center, BorderLayout.CENTER);
        rootPanel.add(difficultyPanel, BorderLayout.EAST);
        applyTheme();
    }

    private void selectDifficulty(Difficulty difficulty) {
        if (difficulty == Difficulty.CUSTOM) {
            GameDialogs.customBoard(this, theme).ifPresent(config ->
                    startGame(difficulty, config.width(), config.height(), config.mines()));
        } else {
            startGame(difficulty, difficulty.getWidth(), difficulty.getHeight(), difficulty.getMines());
        }
    }

    private void startGame(Difficulty difficulty, int width, int height, int mines) {
        if (controller != null) controller.dispose();
        currentDifficulty = difficulty;
        controller = new GameController(width, height, mines);
        difficultyPanel.setSelected(difficulty);
        boardPanel.setBoard(controller.getBoard());
        controller.setEventListener(this);
        revalidate();
        repaint();
    }

    private void restartGame() {
        controller.initNewGame();
    }

    private void handleCellClick(int row, int col, MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event)) {
            controller.handleRightClick(row, col);
        } else if (SwingUtilities.isLeftMouseButton(event)) {
            if (event.isShiftDown() || event.getClickCount() == 2) {
                controller.handleChordClick(row, col);
            } else {
                controller.handleCellClick(row, col);
            }
        }
        refreshBoard();
    }

    private void refreshBoard() {
        Board board = controller.getBoard();
        boardPanel.refresh(board);
        gameHeader.updateMines(board.getRemainingMines());
    }

    private void applyTheme() {
        rootPanel.setBackground(theme.getWindowBackground());
        navigation.setTheme(theme);
        titleLabel.setForeground(theme.getAccentPrimary());
        recordsButton.setTheme(theme);
        settingsButton.setTheme(theme);
        gameHeader.setTheme(theme);
        difficultyPanel.setTheme(theme);
        boardPanel.setTheme(theme);
        repaint();
    }

    @Override
    public void onTimerTick(int seconds) {
        gameHeader.updateTime(seconds);
    }

    @Override
    public void onGameStatusChanged(GameState state) {
        refreshBoard();
        if (state == GameState.PLAYING) return;
        Board board = controller.getBoard();
        int seconds = controller.getSecondsElapsed();
        boolean newRecord = state == GameState.WON && scoreRecord.saveIfBest(currentDifficulty, seconds);
        GameResultDialog dialog = new GameResultDialog(this, theme, state, seconds,
                board.getRevealedCellsCount(), board.getWidth() * board.getHeight() - board.getTotalMines(),
                newRecord, this::restartGame);
        dialog.showDialog();
    }

    @Override
    public void dispose() {
        if (controller != null) controller.dispose();
        super.dispose();
    }
}
