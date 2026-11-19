package ui.theme;

import java.awt.Color;

public interface GameTheme {
    String getName();

    Color getWindowBackground();
    Color getPanelBackground();
    Color getPanelBorder();

    Color getHiddenCellBg();
    Color getHiddenCellHover();
    Color getHiddenCellBorder();

    Color getRevealedCellBg();
    Color getRevealedCellBorder();

    Color getAccentPrimary();
    Color getAccentSecondary();
    Color getTextPrimary();
    Color getTextMuted();

    Color getFlagColor();
    Color getMineColor();
    Color getNumberColor(int number);
}
