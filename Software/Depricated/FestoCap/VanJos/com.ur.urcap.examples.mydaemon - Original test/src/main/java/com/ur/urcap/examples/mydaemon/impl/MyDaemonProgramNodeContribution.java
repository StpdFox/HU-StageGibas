package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.component.LabelComponent;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.variable.*;
import com.ur.urcap.api.domain.variable.VariableFactory;
import com.ur.urcap.api.ui.annotation.Select;
import com.ur.urcap.api.ui.component.*;

import java.util.Collection;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.ur.urcap.api.ui.component.SelectEvent.EventType.ON_SELECT;

public class MyDaemonProgramNodeContribution implements ProgramNodeContribution {
	public static final String SELECTED_VAR = "selectedVar";
    public static final String SELECTED_VAR2 = "selectedVar2";
    public static final String Waarde_Vel = "Waarde_Vel";
    private static final String DONEMOVING = "DoneMoving";
	private static final String Velocity = "velocity";
	private static final String Position = "position";
    private String message_Done = "0";


	private final DataModel model;
	private final URCapAPI api;
	private final VariableFactory variableFactory;
	private Timer uiTimer;

    @Label(id = "errorLabel")
    public LabelComponent errorLabel;

	@Select(id = "comboVariables")
	private SelectDropDownList comboVariables;

    @Select(id = "comboVariables2")
    private SelectDropDownList comboVariables2;

	public MyDaemonProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
		variableFactory = api.getVariableModel().getVariableFactory();
		Collection<Variable> variables = api.getVariableModel().getAll();


	}

	@Label(id = "titlePreviewLabel")
	private LabelComponent titlePreviewLabel;

	@Label(id = "messageVelPreviewLabel")
	private LabelComponent messageVelPreviewLabel;

    @Label(id = "messagePosPreviewLabel")
    private LabelComponent messagePosPreviewLabel;

	@Override
	public void openView() {
        updateComboBox();
        clearErrors();
        updatePreview();
    }

    @Override
    public void closeView() {
        while(message_Done.equals("0")){
            updatePreview();
        }
	}


	@Select(id = "comboVariables")
	private void selectComboVariables(SelectEvent event) {
		if (event.getEvent() == ON_SELECT) {
			Object selectedItem = comboVariables.getSelectedItem();
			if (selectedItem instanceof Variable) {
				model.set(SELECTED_VAR, (Variable) selectedItem);
			} else {
				model.remove(SELECTED_VAR);
			}
		}
	}

    @Select(id = "comboVariables2")
    private void selectComboVariables2(SelectEvent event) {
        if (event.getEvent() == ON_SELECT) {
            Object selectedItem = comboVariables2.getSelectedItem();
            if (selectedItem instanceof Variable) {
                model.set(SELECTED_VAR2, (Variable) selectedItem);
            } else {
                model.remove(SELECTED_VAR2);
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
        comboVariables2.setItems(items);

        Variable selectedVar2 = getSelectedVariable2();
        if (selectedVar2 != null) {
            comboVariables2.selectItem(selectedVar2);
        }
	}

    @Override
	public String getTitle() {
		return "Festo-Linear-Axis-Motion";
	}

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined() && !getSelectedVariable().toString().isEmpty() && !getSelectedVariable2().toString().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		// Note, alternatively plain sockets can be used.

		writer.assign("mydaemon_title", getInstallation().getXMLRPCVariable() + ".get_title()");
        writer.assign("mydaemon_Done", getInstallation().getXMLRPCVariable() + ".get_message_enable()");

        Variable variable = getSelectedVariable();
        if (variable != null) {
            String resolvedVariableName = writer.getResolvedVariableName(variable);
            messageVelPreviewLabel.setText(resolvedVariableName);
            //model.set(Waarde_Vel, resolvedVariableName);
            writer.assign("mydaemon_message_vel",
                    getInstallation().getXMLRPCVariable() + ".get_message_vel(" + resolvedVariableName + ")");

        } else {
            // Handle this null somehow
        }
        Variable variable2 = getSelectedVariable2();
        if (variable2 != null) {
            String resolvedVariableName = writer.getResolvedVariableName(variable2);
            messagePosPreviewLabel.setText(resolvedVariableName);

            writer.assign("mydaemon_message_pos",
                    getInstallation().getXMLRPCVariable() + ".get_message_pos(" + resolvedVariableName + ")");
        } else {
            // Handle this null somehow

        }
        writer.writeChildren();


    }

	private void updatePreview() {
		String title = "";
        //String message_Done = "0";


		try {
			// Provide a real-time preview of the daemon state
			title = getInstallation().getXmlRpcDaemonInterface().getTitle();
			message_Done = getInstallation().getXmlRpcDaemonInterface().getDone();


		} catch (Exception e) {
			System.err.println("Could not retrieve essential data from the daemon process for the preview.");
			title = "<Daemon disconnected>";
		}

		titlePreviewLabel.setText(title);

	}

    private String getPos() {
        return model.get(Position, "0");
    }

    private Variable getSelectedVariable() {
        return model.get(SELECTED_VAR, (Variable) null);
    }

    private Variable getSelectedVariable2() {
        return model.get(SELECTED_VAR2, (Variable) null);
    }

	private void setWait(String velocity) {
		if ("".equals(velocity)){
			model.remove(DONEMOVING);
		}else{
			model.set(DONEMOVING, velocity);
		}
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
