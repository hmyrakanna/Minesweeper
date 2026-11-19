package ui.components;

import model.Board;
import model.GameState;
import ui.theme.GameTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends RoundedPanel {
    public interface CellClickListener {
        void onCellClick(int row, int col, MouseEvent event);
    }

    private static final int GAP = 4;
    private static final int MIN_CELL = 32;
    private static final int MAX_CELL = 60;
    private final CellClickListener listener;
    private final JScrollPane scrollPane;
    private final GridPanel grid = new GridPanel();
    private GameTheme theme;
    private CellButton[][] buttons;
    private Board board;

    public BoardPanel(GameTheme theme, CellClickListener listener) {
        super(18, theme);
        this.theme = theme;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        grid.setOpaque(false);
        scrollPane = new JScrollPane(grid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(MIN_CELL + GAP);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(MIN_CELL + GAP);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setBoard(Board board) {
        this.board = board;
        grid.removeAll();
        grid.setLayout(new SquareGridLayout(board.getHeight(), board.getWidth()));
        buttons = new CellButton[board.getHeight()][board.getWidth()];
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                CellButton button = new CellButton(row, col, board.getCell(row, col), theme);
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (BoardPanel.this.board.getGameState() == GameState.PLAYING) {
                            listener.onCellClick(button.getRow(), button.getCol(), e);
                        }
                    }
                });
                buttons[row][col] = button;
                grid.add(button);
            }
        }
        refresh(board);
        grid.revalidate();
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
    }

    public void refresh(Board board) {
        this.board = board;
        if (buttons == null || buttons.length != board.getHeight()
                || buttons[0].length != board.getWidth()) {
            setBoard(board);
            return;
        }
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                buttons[row][col].sync(board.getCell(row, col), board.getGameState(), theme);
            }
        }
        repaint();
    }

    @Override
    public void setTheme(GameTheme theme) {
        super.setTheme(theme);
        this.theme = theme;
        if (board != null) refresh(board);
    }

    private static class GridPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return MIN_CELL + GAP;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getParent() != null && getParent().getWidth() >= getPreferredSize().width;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() >= getPreferredSize().height;
        }
    }

    private static class SquareGridLayout implements LayoutManager {
        private final int rows;
        private final int cols;

        SquareGridLayout(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {}
        @Override
        public void removeLayoutComponent(Component comp) {}
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(cols * MIN_CELL + (cols - 1) * GAP,
                    rows * MIN_CELL + (rows - 1) * GAP);
        }
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            int cellSize = Math.max(MIN_CELL, Math.min(MAX_CELL, Math.min(
                    (parent.getWidth() - (cols - 1) * GAP) / cols,
                    (parent.getHeight() - (rows - 1) * GAP) / rows)));
            int startX = Math.max(0, (parent.getWidth() - cols * cellSize - (cols - 1) * GAP) / 2);
            int startY = Math.max(0, (parent.getHeight() - rows * cellSize - (rows - 1) * GAP) / 2);
            for (int index = 0; index < parent.getComponentCount(); index++) {
                parent.getComponent(index).setBounds(
                        startX + (index % cols) * (cellSize + GAP),
                        startY + (index / cols) * (cellSize + GAP), cellSize, cellSize);
            }
        }
    }
}
