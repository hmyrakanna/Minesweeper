package ui.dialogs;

import ui.components.RoundedButton;
import ui.theme.GameTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class StyledDialog extends JDialog {
    protected final GameTheme theme;
    protected final JPanel body = new JPanel();

    public StyledDialog(JFrame owner, String title, String subtitle, GameTheme theme) {
        super(owner, title, true);
        this.theme = theme;
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(theme.getPanelBackground());
        body.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        setContentPane(body);
        getRootPane().setBorder(BorderFactory.createLineBorder(theme.getPanelBorder(), 1));
        if (!title.isEmpty()) {
            body.add(label(title, 24, Font.BOLD, theme.getAccentPrimary()));
            body.add(Box.createVerticalStrut(6));
        }
        if (!subtitle.isEmpty()) {
            body.add(label(subtitle, 13, Font.PLAIN, theme.getTextMuted()));
            body.add(Box.createVerticalStrut(20));
        }
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    protected JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    public JPanel getBody() {
        return body;
    }

    public RoundedButton actionButton(String text, boolean primary, Runnable action) {
        RoundedButton button = new RoundedButton(text, 12, theme);
        button.setActive(primary);
        button.setPreferredSize(new Dimension(166, 42));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    public void addActions(RoundedButton... buttons) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (RoundedButton button : buttons) actions.add(button);
        body.add(Box.createVerticalStrut(18));
        body.add(actions);
    }

    public void showDialog() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
}
