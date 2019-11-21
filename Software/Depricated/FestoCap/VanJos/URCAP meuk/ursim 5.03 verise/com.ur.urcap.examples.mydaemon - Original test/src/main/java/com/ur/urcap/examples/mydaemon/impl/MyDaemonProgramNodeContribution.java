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
import com.ur.urcap.api.ui.component.SelectDropDownList;
import com.ur.urcap.api.ui.component.SelectEvent;

import java.awt.*;
import java.util.*;
import static com.ur.urcap.api.ui.component.SelectEvent.EventType.ON_SELECT;

public class MyDaemonProgramNodeContribution implements ProgramNodeContribution {
	public static final String SELECTED_VELOCITY = "SELECTED_VELOCITY";
	public static final String SELECTED_POSITION = "SELECTED_POSITION";
	private static final String DONEMOVING = "DoneMoving";
	private static final String Position = "position";

	private final DataModel model;
	private final URCapAPI api;
	private final VariableFactory variableFactory;
	private Timer uiTimer;

	// velocity variable
	@Select(id = "VelocityVariables")
	private SelectDropDownList VelocityVariables;

    // position variable
	@Select(id = "PositionVariables")
	private SelectDropDownList PositionVariables;




	public MyDaemonProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
		variableFactory = api.getVariableModel().getVariableFactory();
		Collection<Variable> variables = api.getVariableModel().getAll();
	}

    @Override
	public void openView() {
		updateComboBox();

		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		uiTimer = new Timer(true);
		uiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updatePreview();
					}
				});
			}
		}, 0, 1000);
	}

	@Override
	public void closeView() {
    	uiTimer.cancel();
	}

    // drop down menu to select the velocity variable
	@Select(id = "VelocityVariables")
	private void selectComboVariablesVelocity(SelectEvent event) {
		if (event.getEvent() == ON_SELECT) {
			Object selectedItem = VelocityVariables.getSelectedItem();
			if (selectedItem instanceof Variable) {
				model.set(SELECTED_VELOCITY, (Variable) selectedItem);
			} else {
				model.remove(SELECTED_VELOCITY);
			}
		}
	}

    // drop down menu to select the position variable
	@Select(id = "PositionVariables")
	private void selectComboVariablesPosition(SelectEvent event) {
		if (event.getEvent() == ON_SELECT) {
			Object selectedItem = PositionVariables.getSelectedItem();
			if (selectedItem instanceof Variable) {
				model.set(SELECTED_POSITION, (Variable) selectedItem);
			} else {
				model.remove(SELECTED_POSITION);
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

		// update and set variables in the velocity tab
		VelocityVariables.setItems(items);

		Variable selectedVar = getSelectedVariableVelocity();
		if (selectedVar != null) {
			VelocityVariables.selectItem(selectedVar);
		}

        // update and set variables in the position tab
		PositionVariables.setItems(items);

		Variable selectedVar2 = getSelectedVariablePosition();
		if (selectedVar2 != null) {
			PositionVariables.selectItem(selectedVar2);
		}

	}

	@Override
	public String getTitle() {
		return "Festo-Linear-Axis-Motion";
	}

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
	    // function not necessary
    	writer.assign("mydaemon_title", getInstallation().getXMLRPCVariable() + ".get_title()");

        // send velocity variable to the python code via XMLRPC protocol.
		Variable var_velocity = getSelectedVariableVelocity();
		if (var_velocity != null) {
		    // get value of variable, instead of sending the name
			String resolvedVariableName = writer.getResolvedVariableName(var_velocity);

			//sending it.
			writer.assign("mydaemon_message_vel",
					getInstallation().getXMLRPCVariable() + ".get_message_vel(" + resolvedVariableName + ")");

		} else {
			// Handle this null somehow
		}

        // send velocity variable to the python code via XMLRPC protocol.
		Variable var_position = getSelectedVariablePosition();
		if (var_position != null) {
			String resolvedVariableName = writer.getResolvedVariableName(var_position);

			writer.assign("mydaemon_message_pos",
					getInstallation().getXMLRPCVariable() + ".get_message_pos(" + resolvedVariableName + ")");
		} else {
			// Handle this null somehow
		}
        writer.appendLine("global machine_finished = \"0\"");
		writer.appendLine("sleep(1)");

	}

	private void updatePreview() {

	}

	private Variable getSelectedVariableVelocity() {
		return model.get(SELECTED_VELOCITY, (Variable) null);
	}

	private Variable getSelectedVariablePosition() {
		return model.get(SELECTED_POSITION, (Variable) null);
	}

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}



}





