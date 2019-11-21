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

public class MyDaemonProgramNodeContribution implements ProgramNodeContribution {
	private static final String Velocity = "velocity";
	private static final String Position = "position";

	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public MyDaemonProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}

	@Input(id = "Velocity")
	private InputTextField VelTextField;

	@Input(id = "Position")
	private InputTextField PosTextField;

	@Label(id = "titlePreviewLabel")
	private LabelComponent titlePreviewLabel;

	@Label(id = "messageVelPreviewLabel")
	private LabelComponent messageVelPreviewLabel;

    @Label(id = "messagePosPreviewLabel")
    private LabelComponent messagePosPreviewLabel;


	@Input(id = "Velocity")
	public void onInput(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setVel(VelTextField.getText());
			updatePreview();
		}
	}

    @Input(id = "Position")
    public void onInput1(InputEvent event) {
        if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
            setPos(PosTextField.getText());
            updatePreview();
        }
    }

	@Override
	public void openView() {
        VelTextField.setText(getVel());
        PosTextField.setText(getPos());

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
		return "MyDaemon: " + (model.isSet(Velocity) ? getVel() : "");
	}

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined() && !getVel().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		// Note, alternatively plain sockets can be used.
		writer.assign("mydaemon_message_vel", getInstallation().getXMLRPCVariable() + ".get_message_vel(\"" + getVel() + "\")");
        writer.assign("mydaemon_message_pos", getInstallation().getXMLRPCVariable() + ".get_message_pos(\"" + getPos() + "\")");
		writer.assign("mydaemon_title", getInstallation().getXMLRPCVariable() + ".get_title()");
		writer.appendLine("popup(mydaemon_message_vel, mydaemon_message_pos, mydaemon_title, False, False, blocking=True)");
		writer.writeChildren();
	}

	private void updatePreview() {
		String title = "";
        String message_vel = "";
        String message_pos = "";
		try {
			// Provide a real-time preview of the daemon state
			title = getInstallation().getXmlRpcDaemonInterface().getTitle();
			message_vel = getInstallation().getXmlRpcDaemonInterface().getMessage_vel(getVel());
            message_pos = getInstallation().getXmlRpcDaemonInterface().getMessage_pos(getPos());
		} catch (Exception e) {
			System.err.println("Could not retrieve essential data from the daemon process for the preview.");
			title = message_vel = message_pos = "<Daemon disconnected>";
		}

		titlePreviewLabel.setText(title);
		messageVelPreviewLabel.setText(message_vel);
        messagePosPreviewLabel.setText(message_pos);
	}

	private String getVel() {
		return model.get(Velocity, "");
	}

    private String getPos() {
        return model.get(Position, "");
    }

	private void setVel(String velocity) {
		if ("".equals(velocity)){
			model.remove(Velocity);
		}else{
			model.set(Velocity, velocity);
		}
	}

    private void setPos(String position) {
        if ("".equals(position)){
            model.remove(Position);
        }else{
            model.set(Position, position);
        }
    }

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
