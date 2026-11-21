package ui.dialogs;

import model.*;
import service.StatisticsService;
import ui.components.RoundedPanel;
import ui.theme.GameTheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatisticsDialog extends StyledDialog {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private final StatisticsService statistics;

    public StatisticsDialog(JFrame owner, GameTheme theme, StatisticsService statistics) {
        super(owner, "Статистика", "Твої результати та історія ігор", theme);
        this.statistics = statistics;
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.setBackground(theme.getHiddenCellBg());
        tabs.setForeground(theme.getTextPrimary());
        tabs.addTab("Загальна", overview());
        tabs.addTab("Рекорди", records());
        tabs.addTab("Історія", history());
        tabs.setPreferredSize(new Dimension(690, 390));
        body.add(tabs);
        addActions(actionButton("Закрити", true, this::dispose));
    }
    private JPanel tabPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(theme.getPanelBackground());
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        return panel;
    }
    private JPanel overview() {
        StatisticsService.Summary s = statistics.summary();
        JPanel panel = tabPanel();
        JPanel grid = new JPanel(new GridLayout(3, 2, 12, 12));
        grid.setOpaque(false);
        grid.add(metric("Завершені ігри", "" + s.completed()));
        grid.add(metric("Відсоток перемог", String.format("%.1f%%", s.winRate())));
        grid.add(metric("Перемоги / поразки", s.wins() + " / " + s.losses()));
        grid.add(metric("Найкраща серія перемог", "" + s.bestStreak()));
        grid.add(metric("Використано підказок", "" + s.hints()));
        grid.add(metric("Загальний час", duration(s.seconds())));
        panel.add(grid, BorderLayout.CENTER);
        panel.add(label("Перервані партії: " + s.abandoned() + "  •  Не враховуються у % перемог",
                12, Font.PLAIN, theme.getTextMuted()), BorderLayout.SOUTH);
        return panel;
    }
    private RoundedPanel metric(String caption, String value) {
        RoundedPanel panel = new RoundedPanel(12, theme);
        panel.setLayout(new GridBagLayout());
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(label(caption, 13, Font.PLAIN, theme.getTextMuted()));
        inner.add(Box.createVerticalStrut(6));
        inner.add(label(value, 23, Font.BOLD, theme.getAccentPrimary()));
        panel.add(inner);
        return panel;
    }
    private JPanel records() {
        JPanel panel = tabPanel();
        Difficulty[] levels = {Difficulty.BEGINNER, Difficulty.INTERMEDIATE, Difficulty.EXPERT};
        JComboBox<String> filter = new JComboBox<>(new String[]{levels[0].getTitle(), levels[1].getTitle(), levels[2].getTitle()});
        styleFilter(filter);
        DefaultTableModel model = readOnlyModel("Місце", "Дата", "Час", "Поле");
        JLabel note = label("", 12, Font.PLAIN, theme.getTextMuted());
        Runnable fill = () -> {
            Difficulty d = levels[filter.getSelectedIndex()];
            model.setRowCount(0);
            List<GameResult> top = statistics.records(d);
            for (int i = 0; i < top.size(); i++) {
                GameResult r = top.get(i);
                model.addRow(new Object[]{i + 1, DATE.format(r.date()), duration(r.seconds()), r.config().sizeLabel()});
            }
            Integer old = statistics.getLegacyRecord(d);
            String legacy = old == null ? "" : "  •  Старий рекорд: " + duration(old);
            note.setText("Тільки перемоги без підказок" + legacy + (top.isEmpty() ? "  •  Немає нових рекордів" : ""));
        };
        filter.addActionListener(e -> fill.run());
        panel.add(filter, BorderLayout.NORTH);
        panel.add(table(model), BorderLayout.CENTER);
        panel.add(note, BorderLayout.SOUTH);
        fill.run();
        return panel;
    }
    private JPanel history() {
        JPanel panel = tabPanel();
        JComboBox<String> filter = new JComboBox<>(new String[]{"Усі складності", "Початківець", "Умілець", "Експерт", "Власне поле"});
        styleFilter(filter);
        DefaultTableModel model = readOnlyModel("Дата", "Складність", "Поле / міни", "Результат", "Час", "Підказки");
        Runnable fill = () -> {
            model.setRowCount(0);
            int selected = filter.getSelectedIndex();
            for (GameResult r : statistics.history()) {
                if (selected > 0 && r.config().difficulty() != Difficulty.values()[selected - 1]) continue;
                model.addRow(new Object[]{DATE.format(r.date()), r.config().difficulty().getTitle(),
                        r.config().sizeLabel() + " / " + r.config().mines(), r.outcome().getTitle(),
                        duration(r.seconds()), r.hintsUsed()});
            }
        };
        filter.addActionListener(e -> fill.run());
        panel.add(filter, BorderLayout.NORTH);
        JScrollPane scroll = table(model);
        JTable list = (JTable) scroll.getViewport().getView();
        int[] widths = {130, 100, 85, 85, 60, 60};
        for (int i = 0; i < widths.length; i++) list.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(label("Історія зберігається автоматично", 12, Font.PLAIN, theme.getTextMuted()), BorderLayout.SOUTH);
        fill.run();
        return panel;
    }
    private void styleFilter(JComboBox<String> box) {
        box.setBackground(theme.getHiddenCellBg());
        box.setForeground(theme.getTextPrimary());
        box.setFont(new Font("SansSerif", Font.BOLD, 13));
    }
    private DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    private JScrollPane table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.setBackground(theme.getRevealedCellBg());
        table.setForeground(theme.getTextPrimary());
        table.setGridColor(theme.getPanelBorder());
        table.setSelectionBackground(theme.getHiddenCellBg());
        table.setSelectionForeground(theme.getTextPrimary());
        table.getTableHeader().setBackground(theme.getHiddenCellBg());
        table.getTableHeader().setForeground(theme.getTextPrimary());
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(theme.getRevealedCellBg());
        scroll.setBorder(BorderFactory.createLineBorder(theme.getPanelBorder()));
        return scroll;
    }
    private static String duration(long seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }
}
