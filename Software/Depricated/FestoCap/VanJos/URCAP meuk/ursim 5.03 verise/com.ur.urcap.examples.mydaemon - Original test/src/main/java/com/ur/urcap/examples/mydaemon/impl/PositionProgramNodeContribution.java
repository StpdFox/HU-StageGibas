package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.annotation.Select;
import com.ur.urcap.api.ui.component.*;

import java.awt.*;
import java.util.*;

import static com.ur.urcap.api.ui.component.SelectEvent.EventType.ON_SELECT;

public class PositionProgramNodeContribution implements ProgramNodeContribution {
	public static final String SELECTED_CurPos = "SELECTED_CurPos";
	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public PositionProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}


	// position variable
	@Select(id = "CurPositionVariables")
	private SelectDropDownList CurPositionVariables;


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
		}, 0, 100);
	}

	@Override
	public void closeView() {
		uiTimer.cancel();
	}

	@Select(id = "VelocityVariables")
	private void selectComboVariablesVelocity(SelectEvent event) {
		if (event.getEvent() == ON_SELECT) {
			Object selectedItem = CurPositionVariables.getSelectedItem();
			if (selectedItem instanceof Variable) {
				model.set(SELECTED_CurPos, (Variable) selectedItem);
			} else {
				model.remove(SELECTED_CurPos);
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
		CurPositionVariables.setItems(items);

		Variable selectedVar = getSelectedVariableCurPosition();
		if (selectedVar != null) {
			CurPositionVariables.selectItem(selectedVar);
		}

	}
	@Override
	public String getTitle() {
		return "Festo-wait-for-completion";
	}

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined();
	}

	@Override
	public void generateScript(ScriptWriter writer) {

		writer.assign("mydaemon_position", getInstallation().getXMLRPCVariable() + ".get_actual_pos()");

		// send velocity variable to the python code via XMLRPC protocol.
		Variable var_curpos = getSelectedVariableCurPosition();
		if (var_curpos != null) {
			// get value of variable, instead of sending the name
			String resolvedVariableName = writer.getResolvedVariableName(var_curpos);

			//sending it.
			//writer.assign("mydaemon_message_vel",
			//		getInstallation().getXMLRPCVariable() + ".get_message_vel(" + resolvedVariableName + ")");

			writer.appendLine("global " + resolvedVariableName + " = " + getInstallation().getXMLRPCVariable() + ".get_actual_pos()");
			writer.appendLine("sleep(0.1)");

		} else {
			// Handle this null somehow
		}



	}

	private void updatePreview() {

	}

	private Variable getSelectedVariableCurPosition() {
		return model.get(SELECTED_CurPos, (Variable) null);
	}

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
