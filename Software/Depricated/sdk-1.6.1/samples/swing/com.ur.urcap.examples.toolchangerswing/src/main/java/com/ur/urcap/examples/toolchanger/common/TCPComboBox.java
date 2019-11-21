package com.ur.urcap.examples.toolchanger.common;

import com.ur.urcap.api.domain.tcp.TCP;
import com.ur.urcap.examples.toolchanger.style.Style;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * This combobox supports displaying unresolved TCPs the same way as in built-in PolyScope program nodes. A TCP can become
 * unresolved if, e.g. a different installation is used or the TCP has been removed from the installation.<br>
 *
 * If a unresolved TCP is selected in the combo box, the item will be shown in italics with a yellow border around the item.
 */
public class TCPComboBox extends JComboBox {

	// Combo box border used when the TCP is unresolved
	private Border errorBorder;
	private Border originalBorder;
	private Color originalBackground;


	public TCPComboBox(Style style) {
		super();

		initialize(style);
		setRenderer(new ToolCellRenderer(style));
	}

	private void initialize(Style style) {
		setPreferredSize(style.getInputFieldSize());
		setMaximumSize(style.getInputFieldSize());
		setMinimumSize(style.getInputFieldSize());

		originalBorder = getBorder();
		originalBackground = getBackground();

		Border insets = new EmptyBorder(new Insets(1, 1, 1, 1));
		if (originalBorder != null) {
			insets = new CompoundBorder(insets, originalBorder);
		}
		errorBorder = new CompoundBorder(new LineBorder(style.getUndefinedWarningColor(), 2), insets);

		addPropertyChangeListener("model", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateBorder();
			}
		});
	}

	private void updateBorder() {
		Object selected = getSelectedItem();

		if (selected instanceof TCP) {
			if (!((TCP) selected).isResolvable()) {
				setErrorBorder();
			} else {
				setOriginalBorder();
			}
		} else {
			setOriginalBorder();
		}
	}

	private void setOriginalBorder() {
		setFont(getFont().deriveFont(Font.PLAIN));
		setBorder(originalBorder);
		setBackground(originalBackground);
	}

	private void setErrorBorder() {
		setFont(getFont().deriveFont(Font.ITALIC));
		setBorder(errorBorder);
	}

	private static class ToolCellRenderer implements ListCellRenderer {
		private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private final Color undefinedWarningColor;

		private ToolCellRenderer(final Style style) {
			undefinedWarningColor = style.getUndefinedWarningColor();
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			if (!(value instanceof TCP)) {
				return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}

			TCP tcp = (TCP) value;
			Component cell = defaultRenderer.getListCellRendererComponent(list, tcp.getDisplayName(), index, isSelected, cellHasFocus);

			if (!tcp.isResolvable()) {
				cell.setBackground(undefinedWarningColor);
				cell.setFont(cell.getFont().deriveFont(Font.ITALIC));
			} else {
				cell.setFont(cell.getFont().deriveFont(Font.PLAIN));
			}
			return cell;
		}
	}
}
