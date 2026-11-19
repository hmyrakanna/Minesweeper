package ui;

import controller.GameController;
import model.Board;
import model.Difficulty;
import model.GameState;
import record.FileScoreRecord;
import record.ScoreRecord;
import ui.components.CellButton;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.theme.GameTheme;
import ui.theme.PastelLilacTheme;
import ui.theme.TwilightTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

public class GameFrame extends JFrame implements GameController.GameEventListener {
    private final ScoreRecord scoreRecord = new FileScoreRecord();
    private GameTheme theme = new TwilightTheme();
    private Difficulty currentDifficulty = Difficulty.BEGINNER;
    private GameController controller;

    private JPanel rootPanel;
    private RoundedPanel leftSidebar;
    private RoundedPanel rightSidebar;
    private RoundedPanel topBar;
    private RoundedPanel boardContainer;
    private JPanel gridPanel;

    private JLabel titleLabel;
    private JLabel diffTitle;
    private JLabel minesLabel;
    private JLabel timerLabel;
    private JLabel minesTitle;
    private JLabel timerTitle;
    private RoundedButton faceButton;

    private final Map<Difficulty, RoundedButton> difficultyButtons = new EnumMap<>(Difficulty.class);
    private CellButton[][] cellButtons;

    public GameFrame() {
        super("Сапер");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));

        controller = new GameController(
                currentDifficulty.getWidth(),
                currentDifficulty.getHeight(),
                currentDifficulty.getMines()
        );

        initUi();
        rebuildBoardGrid();
        applyTheme();
        controller.setEventListener(this);
        pack();
        setLocationRelativeTo(null);
    }

    private void initUi() {
        rootPanel = new JPanel(new BorderLayout(14, 14));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(rootPanel);

        leftSidebar = new RoundedPanel(20, theme);
        leftSidebar.setLayout(new BoxLayout(leftSidebar, BoxLayout.Y_AXIS));
        leftSidebar.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        leftSidebar.setPreferredSize(new Dimension(220, 0));

        titleLabel = new JLabel("✦ Сапер ✦", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLabel.setText("✦ Сапер ✦");
        titleLabel.setPreferredSize(new Dimension(190, 24));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        RoundedButton newGameBtn = new RoundedButton("Нова гра", 14, theme);
        newGameBtn.setPreferredSize(new Dimension(162, 42));
        newGameBtn.setMinimumSize(new Dimension(162, 42));
        newGameBtn.setMaximumSize(new Dimension(162, 42));
        newGameBtn.addActionListener(e -> restartGame());

        RoundedButton recordsBtn = new RoundedButton("Рекорди", 14, theme);
        recordsBtn.setPreferredSize(new Dimension(162, 42));
        recordsBtn.setMinimumSize(new Dimension(162, 42));
        recordsBtn.setMaximumSize(new Dimension(162, 42));
        recordsBtn.addActionListener(e -> showRecordsDialog());

        RoundedButton settingsBtn = new RoundedButton("Налаштування", 14, theme);
        settingsBtn.setPreferredSize(new Dimension(162, 42));
        settingsBtn.setMinimumSize(new Dimension(162, 42));
        settingsBtn.setMaximumSize(new Dimension(162, 42));
        settingsBtn.addActionListener(e -> showSettingsDialog());

        leftSidebar.add(titleLabel);
        leftSidebar.add(Box.createVerticalStrut(18));
        leftSidebar.add(newGameBtn);
        leftSidebar.add(Box.createVerticalStrut(10));
        leftSidebar.add(recordsBtn);
        leftSidebar.add(Box.createVerticalStrut(10));
        leftSidebar.add(settingsBtn);

        rightSidebar = new RoundedPanel(20, theme);
        rightSidebar.setLayout(new BoxLayout(rightSidebar, BoxLayout.Y_AXIS));
        rightSidebar.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        rightSidebar.setPreferredSize(new Dimension(210, 0));

        diffTitle = new JLabel("Складність", SwingConstants.CENTER);
        diffTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        diffTitle.setForeground(theme.getAccentPrimary());
        diffTitle.setAlignmentX(CENTER_ALIGNMENT);

        rightSidebar.add(diffTitle);
        rightSidebar.add(Box.createVerticalStrut(14));

        addDifficultyButton(Difficulty.BEGINNER, "Початківець 9×9");
        addDifficultyButton(Difficulty.INTERMEDIATE, "Умілець 16×16");
        addDifficultyButton(Difficulty.EXPERT, "Експерт 30×16");
        addDifficultyButton(Difficulty.CUSTOM, "Власне поле...");

        updateDifficultyHighlight();

        topBar = new RoundedPanel(18, theme);
        topBar.setLayout(new BorderLayout(18, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));

        minesLabel = new JLabel("010");
        minesLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        minesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        minesLabel.setForeground(theme.getAccentPrimary());

        minesTitle = new JLabel("МІНИ", SwingConstants.CENTER);
        minesTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        minesTitle.setForeground(theme.getTextMuted());

        JPanel minesBlock = new JPanel(new BorderLayout());
        minesBlock.setOpaque(false);
        minesBlock.setPreferredSize(new Dimension(112, 46));
        minesBlock.add(minesTitle, BorderLayout.NORTH);
        minesBlock.add(minesLabel, BorderLayout.CENTER);

        faceButton = new RoundedButton("☺", 14, theme);
        faceButton.setPreferredSize(new Dimension(44, 38));
        faceButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        faceButton.addActionListener(e -> restartGame());

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setForeground(theme.getAccentSecondary());

        timerTitle = new JLabel("ЧАС", SwingConstants.CENTER);
        timerTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        timerTitle.setForeground(theme.getTextMuted());

        JPanel timerBlock = new JPanel(new BorderLayout());
        timerBlock.setOpaque(false);
        timerBlock.setPreferredSize(new Dimension(112, 46));
        timerBlock.add(timerTitle, BorderLayout.NORTH);
        timerBlock.add(timerLabel, BorderLayout.CENTER);

        JPanel faceBlock = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        faceBlock.setOpaque(false);
        faceBlock.add(faceButton);

        topBar.add(minesBlock, BorderLayout.WEST);
        topBar.add(faceBlock, BorderLayout.CENTER);
        topBar.add(timerBlock, BorderLayout.EAST);

        boardContainer = new RoundedPanel(20, theme);
        boardContainer.setLayout(new BorderLayout());
        boardContainer.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridPanel, BorderLayout.CENTER);
        boardContainer.add(gridWrapper, BorderLayout.CENTER);

        JPanel centerArea = new JPanel(new BorderLayout(0, 12));
        centerArea.setOpaque(false);
        centerArea.add(topBar, BorderLayout.NORTH);
        centerArea.add(boardContainer, BorderLayout.CENTER);

        rootPanel.add(leftSidebar, BorderLayout.WEST);
        rootPanel.add(centerArea, BorderLayout.CENTER);
        rootPanel.add(rightSidebar, BorderLayout.EAST);
    }

    private void addDifficultyButton(Difficulty diff, String label) {
        RoundedButton btn = new RoundedButton(label, 14, theme);
        btn.setPreferredSize(new Dimension(182, 44));
        btn.setMinimumSize(new Dimension(182, 44));
        btn.setMaximumSize(new Dimension(182, 44));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            if (diff == Difficulty.CUSTOM) {
                showCustomBoardDialog();
            } else {
                startPresetGame(diff);
            }
        });
        difficultyButtons.put(diff, btn);
        rightSidebar.add(btn);
        rightSidebar.add(Box.createVerticalStrut(8));
    }

    private void startPresetGame(Difficulty diff) {
        currentDifficulty = diff;
        controller = new GameController(diff.getWidth(), diff.getHeight(), diff.getMines());
        updateDifficultyHighlight();
        rebuildBoardGrid();
        applyTheme();
        controller.setEventListener(this);
        revalidate();
        repaint();
    }

    private void showCustomBoardDialog() {
        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(12, 5, 36, 1));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(12, 5, 24, 1));
        JSpinner minesSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 200, 1));
        styleSpinner(widthSpinner);
        styleSpinner(heightSpinner);
        styleSpinner(minesSpinner);

        JDialog dialog = createStyledDialog(
                "Власне поле", "Налаштуйте розмір і кількість мін", 560, 360
        );
        JPanel content = (JPanel) dialog.getContentPane();
        JPanel form = new JPanel(new GridLayout(3, 2, 12, 12));
        form.setOpaque(false);
        form.add(createDialogLabel("Ширина (5–36):"));
        form.add(widthSpinner);
        form.add(createDialogLabel("Висота (5–24):"));
        form.add(heightSpinner);
        form.add(createDialogLabel("Кількість мін:"));
        form.add(minesSpinner);
        content.add(form);
        content.add(Box.createVerticalStrut(18));

        JPanel buttons = createDialogButtons(480);
        RoundedButton cancelButton = new RoundedButton("Скасувати", 12, theme);
        cancelButton.setPreferredSize(new Dimension(150, 44));
        cancelButton.setMinimumSize(new Dimension(150, 44));
        cancelButton.setMaximumSize(new Dimension(150, 44));
        RoundedButton createButton = new RoundedButton("Створити", 12, theme);
        createButton.setPreferredSize(new Dimension(150, 44));
        createButton.setMinimumSize(new Dimension(150, 44));
        createButton.setMaximumSize(new Dimension(150, 44));
        createButton.setActive(true);
        cancelButton.addActionListener(e -> dialog.dispose());
        createButton.addActionListener(e -> {
            int w = (int) widthSpinner.getValue();
            int h = (int) heightSpinner.getValue();
            int maxMines = Math.max(1, w * h - 9);
            int m = Math.min((int) minesSpinner.getValue(), maxMines);
            currentDifficulty = Difficulty.CUSTOM;
            controller = new GameController(w, h, m);
            updateDifficultyHighlight();
            rebuildBoardGrid();
            applyTheme();
            controller.setEventListener(this);
            revalidate();
            repaint();
            dialog.dispose();
        });
        buttons.add(cancelButton);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(createButton);
        content.add(buttons);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showRecordsDialog() {
        JDialog dialog = createStyledDialog("Рекорди", "Найкращий час за складністю", 560, 390);
        JPanel content = (JPanel) dialog.getContentPane();
        JPanel records = new JPanel(new GridLayout(3, 1, 10, 10));
        records.setOpaque(false);
        records.setPreferredSize(new Dimension(500, 210));

        for (Difficulty d : new Difficulty[]{Difficulty.BEGINNER, Difficulty.INTERMEDIATE, Difficulty.EXPERT}) {
            String best = scoreRecord.getBestTime(d)
                    .map(sec -> sec + " сек")
                    .orElse("Рекорд ще не встановлено");
            RoundedPanel row = new RoundedPanel(12, theme);
            row.setLayout(new BorderLayout());
            row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            row.setPreferredSize(new Dimension(500, 62));
            JLabel name = new JLabel(d.getTitle());
            name.setForeground(theme.getTextPrimary());
            name.setFont(new Font("SansSerif", Font.BOLD, 15));
            JLabel time = new JLabel(best);
            time.setForeground(theme.getAccentPrimary());
            time.setFont(new Font("SansSerif", Font.BOLD, 15));
            row.add(name, BorderLayout.WEST);
            row.add(time, BorderLayout.EAST);
            records.add(row);
        }
        content.add(records);
        content.add(Box.createVerticalStrut(18));

        JPanel buttons = createDialogButtons(500);
        RoundedButton closeButton = new RoundedButton("Закрити", 12, theme);
        closeButton.setActive(true);
        closeButton.setPreferredSize(new Dimension(150, 44));
        closeButton.setMinimumSize(new Dimension(150, 44));
        closeButton.setMaximumSize(new Dimension(150, 44));
        closeButton.addActionListener(e -> dialog.dispose());
        buttons.add(closeButton);
        content.add(buttons);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSettingsDialog() {
        String[] themes = {new TwilightTheme().getName(), new PastelLilacTheme().getName()};
        JComboBox<String> themeBox = new JComboBox<>(themes);
        themeBox.setSelectedItem(theme.getName());
        styleComboBox(themeBox);

        JDialog dialog = createStyledDialog("Налаштування", "Оберіть оформлення гри", 560, 300);
        JPanel content = (JPanel) dialog.getContentPane();
        JPanel form = new JPanel(new GridLayout(2, 1, 10, 12));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(500, 92));
        form.add(createDialogLabel("Тема оформлення:"));
        form.add(themeBox);
        content.add(form);
        content.add(Box.createVerticalStrut(18));

        JPanel buttons = createDialogButtons(500);
        RoundedButton cancelButton = new RoundedButton("Скасувати", 12, theme);
        cancelButton.setPreferredSize(new Dimension(150, 44));
        cancelButton.setMinimumSize(new Dimension(150, 44));
        cancelButton.setMaximumSize(new Dimension(150, 44));
        RoundedButton applyButton = new RoundedButton("Застосувати", 12, theme);
        applyButton.setActive(true);
        applyButton.setPreferredSize(new Dimension(150, 44));
        applyButton.setMinimumSize(new Dimension(150, 44));
        applyButton.setMaximumSize(new Dimension(150, 44));
        cancelButton.addActionListener(e -> dialog.dispose());
        applyButton.addActionListener(e -> {
            String selected = (String) themeBox.getSelectedItem();
            theme = new PastelLilacTheme().getName().equals(selected)
                    ? new PastelLilacTheme()
                    : new TwilightTheme();
            applyTheme();
            dialog.dispose();
        });
        buttons.add(cancelButton);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(applyButton);
        content.add(buttons);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void restartGame() {
        controller.initNewGame();
        faceButton.setText("☺");
        refreshBoard();
    }

    private void rebuildBoardGrid() {
        Board board = controller.getBoard();
        int rows = board.getHeight();
        int cols = board.getWidth();

        gridPanel.removeAll();
        gridPanel.setLayout(new SquareGridLayout(rows, cols, 3));
        cellButtons = new CellButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton btn = new CellButton(r, c, board.getCell(r, c), theme);
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (controller.getBoard().getGameState() != GameState.PLAYING) {
                            return;
                        }
                        if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown()) {
                            controller.handleChordClick(btn.getRow(), btn.getCol());
                        } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                            controller.handleChordClick(btn.getRow(), btn.getCol());
                        } else if (e.getButton() == MouseEvent.BUTTON1) {
                            controller.handleCellClick(btn.getRow(), btn.getCol());
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            controller.handleRightClick(btn.getRow(), btn.getCol());
                        }
                        refreshBoard();
                    }
                });
                cellButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        refreshBoard();
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void refreshBoard() {
        Board board = controller.getBoard();
        int rows = board.getHeight();
        int cols = board.getWidth();
        int flagsCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                var cell = board.getCell(r, c);
                if (cell.isFlagged()) {
                    flagsCount++;
                }
                cellButtons[r][c].sync(cell, board.getGameState(), theme);
            }
        }

        int remainingMines = Math.max(0, board.getTotalMines() - flagsCount);
        minesLabel.setText(String.format("%03d", remainingMines));
    }

    private void updateDifficultyHighlight() {
        difficultyButtons.forEach((diff, btn) -> btn.setActive(diff == currentDifficulty));
    }

    private void applyTheme() {
        rootPanel.setBackground(theme.getWindowBackground());
        leftSidebar.setTheme(theme);
        rightSidebar.setTheme(theme);
        topBar.setTheme(theme);
        boardContainer.setTheme(theme);
        faceButton.setTheme(theme);

        titleLabel.setForeground(theme.getAccentPrimary());
        diffTitle.setForeground(theme.getAccentPrimary());
        minesLabel.setForeground(theme.getAccentPrimary());
        timerLabel.setForeground(theme.getAccentSecondary());
        minesTitle.setForeground(theme.getTextMuted());
        timerTitle.setForeground(theme.getTextMuted());

        leftSidebar.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        rightSidebar.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        for (var entry : difficultyButtons.entrySet()) {
            entry.getValue().setTheme(theme);
        }

        refreshBoard();
        repaint();
    }

    @Override
    public void onTimerTick(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
    }

    @Override
    public void onGameStatusChanged(GameState state) {
        refreshBoard();
        if (state == GameState.WON) {
            faceButton.setText("★");
            boolean isNewRecord = scoreRecord.saveIfBest(currentDifficulty, controller.getSecondsElapsed());
            String msg = "Перемога! Час: " + controller.getSecondsElapsed() + " сек"
                    + (isNewRecord ? "\nНовий рекорд!" : "");
            showStyledMessage("Перемога", msg, false);
        } else if (state == GameState.LOST) {
            faceButton.setText("✖");
            showStyledMessage("Поразка", " Ви натрапили на міну.", true);
        }
    }

    private JDialog createStyledDialog(String title, String subtitle) {
        return createStyledDialog(title, subtitle, 460, 300);
    }

    private JDialog createStyledDialog(String title, String subtitle, int width, int height) {
        JDialog dialog = new JDialog(this, title, false);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setUndecorated(true);

        RoundedPanel content = new RoundedPanel(20, theme);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        content.setPreferredSize(new Dimension(width, height));
        content.setMinimumSize(new Dimension(width, height));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(theme.getAccentPrimary());
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(theme.getTextMuted());
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(18));

        dialog.setContentPane(content);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(theme.getPanelBorder(), 1, true));
        return dialog;
    }

    private JLabel createDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(theme.getTextPrimary());
        return label;
    }

    private JPanel createDialogButtons() {
        return createDialogButtons(372);
    }

    private JPanel createDialogButtons(int width) {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(CENTER_ALIGNMENT);
        buttons.setPreferredSize(new Dimension(width, 44));
        return buttons;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("SansSerif", Font.BOLD, 13));
        spinner.setPreferredSize(new Dimension(112, 34));
        spinner.setBackground(theme.getHiddenCellBg());
        spinner.setForeground(theme.getTextPrimary());
        spinner.setBorder(BorderFactory.createLineBorder(theme.getPanelBorder(), 1, true));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("SansSerif", Font.BOLD, 15));
        comboBox.setPreferredSize(new Dimension(500, 44));
        comboBox.setBackground(theme.getHiddenCellBg());
        comboBox.setForeground(theme.getTextPrimary());
        comboBox.setBorder(BorderFactory.createLineBorder(theme.getPanelBorder(), 1, true));
    }

    private void showStyledMessage(String title, String message, boolean warning) {
        JDialog dialog = createStyledDialog(title, warning ? "Гру завершено" : "Результат гри");
        JPanel content = (JPanel) dialog.getContentPane();
        JLabel messageLabel = new JLabel("<html><div style='text-align:center;'>" + message.replace("\n", "<br>")
                + "</div></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        messageLabel.setForeground(theme.getTextPrimary());
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(messageLabel);
        content.add(Box.createVerticalStrut(18));

        JPanel buttons = createDialogButtons();
        RoundedButton closeButton = new RoundedButton("Добре", 12, theme);
        closeButton.setActive(true);
        closeButton.setPreferredSize(new Dimension(110, 38));
        closeButton.setMinimumSize(new Dimension(150, 44));
        closeButton.setMaximumSize(new Dimension(150, 44));
        closeButton.addActionListener(e -> dialog.dispose());
        buttons.add(closeButton);
        content.add(buttons);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static class SquareGridLayout implements LayoutManager {
        private final int rows;
        private final int cols;
        private final int gap;

        SquareGridLayout(int rows, int cols, int gap) {
            this.rows = rows;
            this.cols = cols;
            this.gap = gap;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(cols * 34 + (cols - 1) * gap,
                    rows * 34 + (rows - 1) * gap);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            if (parent.getComponentCount() == 0) {
                return;
            }

            int availableWidth = parent.getWidth();
            int availableHeight = parent.getHeight();
            int cellSize = Math.max(20, Math.min(
                    (availableWidth - (cols - 1) * gap) / cols,
                    (availableHeight - (rows - 1) * gap) / rows
            ));
            int gridWidth = cols * cellSize + (cols - 1) * gap;
            int gridHeight = rows * cellSize + (rows - 1) * gap;
            int startX = (availableWidth - gridWidth) / 2;
            int startY = (availableHeight - gridHeight) / 2;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int index = row * cols + col;
                    if (index >= parent.getComponentCount()) {
                        return;
                    }
                    Component component = parent.getComponent(index);
                    component.setBounds(
                            startX + col * (cellSize + gap),
                            startY + row * (cellSize + gap),
                            cellSize,
                            cellSize
                    );
                }
            }
        }
    }
}