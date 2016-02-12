package ui;

import javax.swing.*;
import java.awt.*;

public class IconListRenderer extends DefaultListCellRenderer {

	private final IconFactory iconFactory;

	public interface IconFactory {
		Icon getIcon(Object item);
	}
	
	public IconListRenderer(IconFactory iconFactory) {
		this.iconFactory = iconFactory;
	}
	
	@Override
	public Component getListCellRendererComponent(
		JList list, Object value, int index, 
		boolean isSelected, boolean cellHasFocus) {

		JLabel label = (JLabel) super.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);

		Icon icon = iconFactory.getIcon(value);
		label.setIcon(icon);
		return label;
	}
}