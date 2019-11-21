package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.InputTextField;
import com.ur.urcap.api.ui.component.LabelComponent;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class HomingProgramNodeContribution implements ProgramNodeContribution {
	private static final String NAME = "name";

	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public HomingProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}

	@Input(id = "yourname3")
	private InputTextField nameTextField;

	@Label(id = "messagePreviewLabel3")
	private LabelComponent messagePreviewLabel;

	@Input(id = "yourname3")
	public void onInput(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setName(nameTextField.getText());
			updatePreview();
		}
	}

	@Override
	public void openView() {
		nameTextField.setText(getName());

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
		return getInstallation().isDefined() && !getName().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		// Note, alternatively plain sockets can be used.
		writer.assign("mydaemon_message_homing", getInstallation().getXMLRPCVariable() + ".get_message_homing(\"" + getName() + "\")");
		//writer.appendLine("popup(mydaemon_message_homing, False, False, blocking=True)");
		writer.writeChildren();
	}

	private void updatePreview() {
		String message = "";
		try {
			// Provide a real-time preview of the daemon state
			message = getInstallation().getXmlRpcDaemonInterface().getMessage_homing(getName());
		} catch (Exception e) {
			System.err.println("Could not retrieve essential data from the daemon process for the preview.");
			message = "<Daemon disconnected>";
		}


		messagePreviewLabel.setText(message);
	}

	private String getName() {
		return model.get(NAME, "");
	}

	private void setName(String name) {
		if ("".equals(name)){
			model.remove(NAME);
		}else{
			model.set(NAME, name);
		}
	}

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
