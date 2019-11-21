package com.ur.urcap.examples.cyclecounterswing;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.variable.GlobalVariable;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.examples.style.Style;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CycleCounterProgramNodeView implements SwingProgramNodeView<CycleCounterProgramNodeContribution> {

    public static final String INFO_1 = "<html><body><p>Each time this node and its children are run, the counting variable will increment by one.<br>\n" +
            " Select an existing variable or create your own.</p><br>" +
            " <p>The variable is visible in the Variables tab, and can be used to trigger actions<br>" +
            " elsewhere in the program.</p></body></html>";

    public static final String INFO_2 = "Input variable name and press the \"Create New\" button";

    private final Style style;

    private JTextField txtNewVariable = new JTextField();
    private JButton btnNewVariable = new JButton("Create new");
    private JComboBox cmbVariables = new JComboBox();
    private JLabel errorLabel = new JLabel();
    private final ImageIcon errorIcon;

    public CycleCounterProgramNodeView(Style style) {
        this.style = style;
        this.errorIcon = getErrorImage();
    }

    @Override
    public void buildUI(JPanel jPanel, ContributionProvider<CycleCounterProgramNodeContribution> contributionProvider) {
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        jPanel.add(createInfo(INFO_1));
        jPanel.add(createVerticalSpacing());
        jPanel.add(createComboBox(contributionProvider));
        jPanel.add(createInfo(INFO_2));
        jPanel.add(createButtonBox(contributionProvider));
        jPanel.add(createVerticalSpacing());
        jPanel.add(createErrorLabel());
        jPanel.add(Box.createVerticalGlue());
    }

    private Box createErrorLabel() {
        Box infoBox = Box.createHorizontalBox();
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        infoBox.add(errorLabel);
        return infoBox;
    }

    private Box createButtonBox(final ContributionProvider<CycleCounterProgramNodeContribution> provider) {
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNewVariable.setFocusable(false);
        txtNewVariable.setPreferredSize(style.getInputfieldDimension());
        txtNewVariable.setMaximumSize(txtNewVariable.getPreferredSize());
        txtNewVariable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                KeyboardTextInput keyboardInput = provider.get().getKeyboardForInput();
                keyboardInput.show(txtNewVariable, provider.get().getCallbackForInput());
            }
        });

        horizontalBox.add(txtNewVariable);
        horizontalBox.add(createHorizontalSpacing());

        btnNewVariable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clearErrors();
                //Create a global variable with an initial value and store it in the data model to make it available to all program nodes.
                GlobalVariable variable = provider.get().createGlobalVariable(txtNewVariable.getText());
                provider.get().setVariable(variable);
                updateComboBox(provider.get());
            }
        });

        horizontalBox.add(btnNewVariable);
        return horizontalBox;
    }

    private Box createInfo(String info) {
        Box infoBox = Box.createHorizontalBox();
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel();
        label.setText(info);
        label.setSize(label.getPreferredSize());
        infoBox.add(label);
        return infoBox;
    }

    private Component createVerticalSpacing() {
        return Box.createRigidArea(new Dimension(0, style.getVerticalSpacing()));
    }

    private Component createHorizontalSpacing() {
        return Box.createRigidArea(new Dimension(style.getHorizontalSpacing(), 0));
    }

    private Box createComboBox(final ContributionProvider<CycleCounterProgramNodeContribution> provider) {
        Box inputBox = Box.createHorizontalBox();
        inputBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbVariables.setFocusable(false);
        cmbVariables.setPreferredSize(style.getComboBoxDimension());
        cmbVariables.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    if (itemEvent.getItem() instanceof Variable) {
                        provider.get().setVariable(((Variable) itemEvent.getItem()));
                    } else {
                        provider.get().removeVariable();
                    }
                }
            }
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(cmbVariables, BorderLayout.CENTER);

        inputBox.add(panel);
        return inputBox;
    }

    public void setNewVariable(String value) {
        txtNewVariable.setText(value);
    }

    private ImageIcon getErrorImage() {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/com/ur/urcap/examples/cyclecounterswing/warning-bigger.png"));
            return new ImageIcon(image);
        } catch (IOException e) {
            // Should not happen.
            throw new RuntimeException("Unexpected exception while loading icon.", e);
        }
    }

    public void setError(final String message) {
        errorLabel.setText("<html>Error: Could not create variable<br>" + message + "</html>");
        errorLabel.setIcon(errorIcon);
        errorLabel.setVisible(true);
    }

    private void clearInputVariableName() {
        txtNewVariable.setText("");
    }

    private void clearErrors() {
        errorLabel.setVisible(false);
    }

    private void updateComboBox(CycleCounterProgramNodeContribution contribution) {
        List<Object> items = new ArrayList<Object>();
        items.addAll(contribution.getGlobalVariables());

        Collections.sort(items, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase()) == 0) {
                    //Sort lowercase/uppercase consistently
                    return o1.toString().compareTo(o2.toString());
                } else {
                    return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
                }
            }
        });

        //Insert at top after sorting
        items.add(0, "Select counting variable");

        cmbVariables.setModel(new DefaultComboBoxModel(items.toArray()));

        Variable selectedVar = contribution.getSelectedVariable();
        if (selectedVar != null) {
            cmbVariables.setSelectedItem(selectedVar);
        }
    }

    public void update(CycleCounterProgramNodeContribution contribution) {
        clearInputVariableName();
        clearErrors();
        updateComboBox(contribution);
    }
}
