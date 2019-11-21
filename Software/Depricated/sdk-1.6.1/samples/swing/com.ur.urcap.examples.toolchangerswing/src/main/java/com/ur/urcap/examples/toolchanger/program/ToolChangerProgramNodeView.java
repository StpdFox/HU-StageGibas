package com.ur.urcap.examples.toolchanger.program;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.tcp.TCP;
import com.ur.urcap.examples.toolchanger.common.UIComponentFactory;
import com.ur.urcap.examples.toolchanger.style.Style;
import com.ur.urcap.examples.toolchanger.common.TCPComboBox;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;


public class ToolChangerProgramNodeView implements SwingProgramNodeView<ToolChangerProgramNodeContribution> {

	private static final String INFO_TEXT = "<html>Change the tool of the robot.<br/><br/>" +
			"  The robot will move to the tool change position defined in the installation. " +
			"When the tool change operation has finished, the tool TCP will be applied.<html>";
	private static final String SET_TOOL_TCP_TEXT = "Set Tool TCP";
	private static final String TCP_PLACEHOLDER = "<TCP>";

	private final UIComponentFactory uiFactory;
	private final Style style;
	private TCPComboBox tcpsComboBox;

	private ContributionProvider<ToolChangerProgramNodeContribution> contributionProvider;


	public ToolChangerProgramNodeView(Style style) {
		this.style = style;
		this.uiFactory = new UIComponentFactory(style);
	}

	@Override
	public void buildUI(JPanel jPanel, final ContributionProvider<ToolChangerProgramNodeContribution> provider) {
		this.contributionProvider = provider;

		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
		jPanel.add(uiFactory.createInfoSection(INFO_TEXT));
		jPanel.add(uiFactory.createVerticalSpacing());
		jPanel.add(uiFactory.createVerticalSpacing());
		jPanel.add(uiFactory.createVerticalSpacing());
		jPanel.add(uiFactory.createHeaderSection(SET_TOOL_TCP_TEXT));
		jPanel.add(createInput());
	}

	public void updateView() {
		updateCombobox();
	}

	private Box createInput() {
		Box section = Box.createHorizontalBox();
		section.setAlignmentX(Component.LEFT_ALIGNMENT);

		tcpsComboBox = new TCPComboBox(style);
		tcpsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox comboBox = (JComboBox) e.getSource();
				Object selected = comboBox.getSelectedItem();
				if (selected instanceof TCP) {
					TCP tcp = (TCP) comboBox.getSelectedItem();
					contributionProvider.get().setSelectedTCP(tcp);
				} else {
					contributionProvider.get().setSelectedTCP(null);
				}
				updateCombobox();
			}
		});
		section.add(tcpsComboBox);

		return section;
	}

	private void updateCombobox() {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		ToolChangerProgramNodeContribution contribution = contributionProvider.get();
		TCP selected = contribution.getSelectedTCP();

		if (selected != null) {
			model.setSelectedItem(selected);
			model.addElement(TCP_PLACEHOLDER);
		} else {
			model.setSelectedItem(TCP_PLACEHOLDER);
		}

		Collection<TCP> tcps = contribution.getAllTCP();
		if (!tcps.contains(selected)) {
			model.addElement(selected);
		}
		for (TCP tcp : tcps) {
			if (tcp.isResolvable()) {
				model.addElement(tcp);
			}
		}

		tcpsComboBox.setModel(model);
	}

}
