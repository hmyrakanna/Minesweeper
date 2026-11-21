package ui.components;

import model.Board;
import model.CellPosition;
import ui.theme.GameTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class BoardPanel extends RoundedPanel {
    public interface CellClickListener {
        void onCellClick(int row, int col, MouseEvent event);
    }

    private static final int GAP = 4;
    private static final int MIN_CELL = 32;
    private static final int MAX_CELL = 60;
    private final CellClickListener listener;
    private final JScrollPane scrollPane;
    private final JPanel grid = new JPanel();
    private GameTheme theme;
    private CellButton[][] buttons;
    private Board board;
    private final CardLayout cards = new CardLayout();
    private final JPanel pausePanel = new JPanel(new GridBagLayout());
    private final JLabel pauseTitle = new JLabel("Гру призупинено", SwingConstants.CENTER);
    private final JLabel pauseSubtitle = new JLabel("Поле приховано, час зупинено", SwingConstants.CENTER);
    private final RoundedButton resumeButton;
    private boolean paused;
    private CellPosition highlightedPosition;

    public BoardPanel(GameTheme theme, CellClickListener listener, Runnable resumeAction) {
        super(18, theme);
        this.theme = theme;
        this.listener = listener;
        setLayout(cards);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        grid.setOpaque(false);
        JPanel centered = new JPanel(new GridBagLayout());
        centered.setOpaque(false);
        centered.add(grid);
        scrollPane = new JScrollPane(centered);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(MIN_CELL + GAP);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(MIN_CELL + GAP);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { resizeGrid(); }
        });
        add(scrollPane, "board");
        pausePanel.setOpaque(false);
        JPanel message = new JPanel();
        message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        pauseTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        pauseSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pauseTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        message.add(pauseTitle);
        message.add(Box.createVerticalStrut(10));
        message.add(pauseSubtitle);
        message.add(Box.createVerticalStrut(24));
        resumeButton = new RoundedButton("▶ Продовжити", 12, theme);
        resumeButton.setPreferredSize(new Dimension(180, 44));
        resumeButton.setMaximumSize(new Dimension(180, 44));
        resumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resumeButton.setActive(true);
        resumeButton.addActionListener(e -> resumeAction.run());
        message.add(resumeButton);
        pausePanel.add(message);
        add(pausePanel, "paused");
        setTheme(theme);
    }

    public void setBoard(Board board) {
        this.board = board;
        highlightedPosition = null;
        grid.removeAll();
        grid.setLayout(new GridLayout(board.getHeight(), board.getWidth(), GAP, GAP));
        buttons = new CellButton[board.getHeight()][board.getWidth()];
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                CellButton button = new CellButton(board.getCell(row, col), theme);
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!paused) {
                            listener.onCellClick(button.getRow(), button.getCol(), e);
                        }
                    }
                });
                buttons[row][col] = button;
                grid.add(button);
            }
        }
        refresh(board);
        resizeGrid();
        grid.revalidate();
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
    }

    public void refresh(Board board) {
        if (this.board != board || buttons == null) {
            setBoard(board);
            return;
        }
        for (CellButton[] row : buttons) {
            for (CellButton button : row) {
                button.sync(board.getGameState(), theme);
            }
        }
        repaint();
    }

    public void setPaused(boolean value, boolean canResume) {
        paused = value;
        resumeButton.setEnabled(canResume);
        cards.show(this, value ? "paused" : "board");
    }

    public void highlight(CellPosition position) {
        if (buttons == null) return;
        for (CellButton[] row : buttons) {
            for (CellButton button : row) {
                button.setHighlighted(position != null && button.getRow() == position.row()
                        && button.getCol() == position.col());
            }
        }
        if (position != null && !paused && !position.equals(highlightedPosition)) {
            CellButton button = buttons[position.row()][position.col()];
            SwingUtilities.invokeLater(() -> grid.scrollRectToVisible(button.getBounds()));
        }
        highlightedPosition = paused ? null : position;
    }

    @Override
    public void setTheme(GameTheme theme) {
        super.setTheme(theme);
        this.theme = theme;
        pauseTitle.setForeground(theme.getAccentPrimary());
        pauseSubtitle.setForeground(theme.getTextMuted());
        resumeButton.setTheme(theme);
        if (board != null) refresh(board);
    }

    private void resizeGrid() {
        if (board == null) return;
        int rows = board.getHeight(), cols = board.getWidth();
        Dimension area = scrollPane.getViewport().getExtentSize();
        int width = (area.width - (cols - 1) * GAP) / cols;
        int height = (area.height - (rows - 1) * GAP) / rows;
        int cellSize = Math.max(MIN_CELL, Math.min(MAX_CELL, Math.min(width, height)));
        Dimension size = new Dimension(cols * cellSize + (cols - 1) * GAP,
                rows * cellSize + (rows - 1) * GAP);
        if (!size.equals(grid.getPreferredSize())) {
            grid.setPreferredSize(size);
            grid.revalidate();
        }
    }
}
