package se.kits.awslog;

import javax.swing.*;
import java.awt.*;

public class LogCellRenderer extends DefaultListCellRenderer {
    public Color[][] palette = {
            {Color.LIGHT_GRAY, Color.WHITE},
            {Color.RED, Color.ORANGE},
            {Color.BLUE, Color.MAGENTA}
    };

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Alternate the background color
        if ((index/3) % 2 == 0) {
            if (value instanceof EventRow) {
                component.setBackground(palette[((EventRow) value).colorIndex()][1]);
            } else {
                component.setBackground(Color.LIGHT_GRAY);
            }
        } else {
            if (value instanceof EventRow) {
                component.setBackground(palette[((EventRow) value).colorIndex()][0]);
            } else {
                component.setBackground(Color.WHITE);
            }
        }

        // Maintain the selection and focus colors
        if (isSelected) {
            component.setBackground(list.getSelectionBackground());
            component.setForeground(list.getSelectionForeground());
        }

        return component;
    }
}

