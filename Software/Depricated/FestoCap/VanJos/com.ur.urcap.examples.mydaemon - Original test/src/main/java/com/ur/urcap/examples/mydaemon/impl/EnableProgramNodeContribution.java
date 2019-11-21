package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.component.LabelComponent;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.variable.*;
import com.ur.urcap.api.domain.variable.VariableFactory;

import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.annotation.Select;
import com.ur.urcap.api.ui.component.*;


import java.util.Collection;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.ur.urcap.api.ui.component.SelectEvent.EventType.ON_SELECT;

public class EnableProgramNodeContribution implements ProgramNodeContribution {
	public static final String SELECTED_VAR3 = "selectedVar";

	private final DataModel model;
	private final URCapAPI api;
	private final VariableFactory variableFactory;
	private Timer uiTimer;

	@Label(id = "errorLabel")
	public LabelComponent errorLabel;

	@Select(id = "comboVariables")
	private SelectDropDownList comboVariables;

	public EnableProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
		variableFactory = api.getVariableModel().getVariableFactory();
		Collection<Variable> variables = api.getVariableModel().getAll();


	}

	@Label(id = "messagePreviewLabel2")
	private LabelComponent messagePreviewLabel2;

	@Override
	public void openView() {
		updateComboBox();
		clearErrors();
		updatePreview();
	}


	@Override
	public void closeView() {

	}


	@Select(id = "comboVariables")
	private void selectComboVariables(SelectEvent event) {
		if (event.getEvent() == ON_SELECT) {
			Object selectedItem = comboVariables.getSelectedItem();
			if (selectedItem instanceof Variable) {
				model.set(SELECTED_VAR3, (Variable) selectedItem);
			} else {
				model.remove(SELECTED_VAR3);
			}
		}
	}

	private void updateComboBox() {
		ArrayList<Object> items = new ArrayList<Object>();
		items.addAll(api.getVariableModel().get(new Filter<Variable>() {
			@Override
			public boolean accept(Variable element) {
				return element.getType().equals(Variable.Type.GLOBAL) || element.getType().equals(Variable.Type.VALUE_PERSISTED);
			}
		}));

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
		items.add(0, "Select variable");

		comboVariables.setItems(items);

		Variable selectedVar = getSelectedVariable();
		if (selectedVar != null) {
			comboVariables.selectItem(selectedVar);
		}
	}

	@Override
	public String getTitle() {
		return "Festo-Wait";
	}

    @Override
    public boolean isDefined() {
        return getInstallation().isDefined() && !getSelectedVariable().toString().isEmpty();
    }

    @Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		// Note, alternatively plain sockets can be used.

		//writer.assign("mydaemon_message_enable", getInstallation().getXMLRPCVariable() + ".get_message_enable(\"" + getPos() + "\")");
		//writer.assign("mydaemon_title", getInstallation().getXMLRPCVariable() + ".get_title()");
		//writer.appendLine("popup(mydaemon_message_vel, mydaemon_message_pos, mydaemon_title, False, False, blocking=True)");
		//writer.writeChildren();

		Variable variable = getSelectedVariable();
		if (variable != null) {
			String resolvedVariableName = writer.getResolvedVariableName(variable);
			messagePreviewLabel2.setText(resolvedVariableName);
			writer.assign("mydaemon_message_enable",
					getInstallation().getXMLRPCVariable() + ".get_message_enable(" + resolvedVariableName + ")");
            writer.writeChildren();
		} else {
			// Handle this null somehow
		}

	}

	private void updatePreview() {
        String message = "";
        try {
            // Provide a real-time preview of the daemon state
            message = getInstallation().getXmlRpcDaemonInterface().getMessage_enable(getSelectedVariable().toString());
        } catch (Exception e) {
            System.err.println("Could not retrieve essential data from the daemon process for the preview.");
            message = "<Daemon disconnected>";
        }
        //messagePreviewLabel2.setText(message);
    }

	private Variable getSelectedVariable() {
		return model.get(SELECTED_VAR3, (Variable) null);
	}

	private void clearErrors() {
		if (errorLabel != null) {
			errorLabel.setVisible(false);
		}
	}

	private void setError(final String message) {
		if (errorLabel != null) {
			errorLabel.setText("<html>Error: Could not create variable<br>" + message + "</html>");
			errorLabel.setVisible(true);
		}
	}

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
