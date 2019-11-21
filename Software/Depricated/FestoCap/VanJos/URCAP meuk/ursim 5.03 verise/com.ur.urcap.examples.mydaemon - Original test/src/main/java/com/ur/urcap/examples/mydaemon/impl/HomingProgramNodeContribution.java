package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.component.InputCheckBox;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.InputTextField;
import com.ur.urcap.api.ui.component.LabelComponent;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class HomingProgramNodeContribution implements ProgramNodeContribution {
	private static final String Input = "Input";


	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public HomingProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}


	// checkbox input
	@Input(id = "checkbox")
	private InputCheckBox checkbox;

	// set checkbox
	@Input(id = "checkbox")
	public void onInput(boolean status) {
		status = checkbox.isSelected();
		if (status){
			setHoming("1");
		}
		if (!status){
			setHoming("0");
		}
	}

	@Override
	public void openView() {
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

	@Override
	public String getTitle() {
		return "Festo-Homing";
	}

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined() && !getHoming().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls.
		writer.assign("mydaemon_message_homing", getInstallation().getXMLRPCVariable() + ".get_message_homing(\"" + getHoming() + "\")");
	}

	private void updatePreview() {
		//update message
		String message = "";
		try {
			// Provide a real-time preview of the daemon state
			message = getInstallation().getXmlRpcDaemonInterface().getMessage_homing(getHoming());
		} catch (Exception e) {
			System.err.println("Could not retrieve essential data from the daemon process for the preview.");
			message = "<Daemon disconnected>";
		}
	}
	// get checkbox variable
	private String getHoming() {
		return model.get(Input, "1");
	}

	// Set checkbox variable
	private void setHoming(String name) {
		if ("".equals(name)){
			model.remove(Input);
		}else{
			model.set(Input, name);
		}
	}

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
