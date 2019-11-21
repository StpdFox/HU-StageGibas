package com.Gibas.FestoMotion.impl;

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
	private static final String NAME = "name";
	private static final String Velocity = "Vel";
	private static final String Position = "Pos";

	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public MyDaemonProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}
	// orgineel
	//@Input(id = "yourname")
	//private InputTextField nameTextField;
	// ! orgineel

	@Input(id = "Vel")
	private InputTextField VelTextField;

	@Input(id = "Pos")
	private InputTextField PosTextField;


	@Label(id = "titlePreviewLabel")
	private LabelComponent titlePreviewLabel;

	@Label(id = "messagePreviewLabel")
	private LabelComponent messagePreviewLabel;

    // orgineel
	//@Input(id = "yourname")
	//public void onInput(InputEvent event) {
	//	if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
	//		setName(nameTextField.getText());
	//		updatePreview();
	//	}
	//}
    // !orgineel

    @Input(id = "Vel")
    public void onInput(InputEvent event) {
        if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
            setVel(VelTextField.getText());
            updatePreview();
        }
    }

    @Input(id = "Pos")
    public void onInput1(InputEvent event) {
        if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
            setPos(PosTextField.getText());
            updatePreview();
        }
    }


	@Override
	public void openView() {
		VelTextField.setText(getVel());
        //VelTextField.setText(getVel());
        //PosTextField.setText(getPos());


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
	//public String getTitle() {
	//	return "MyDaemon: " + (model.isSet(NAME) ? getName() : "");
	//}
    public String getTitle() {
        return "MyDaemon: " + (model.isSet(Velocity) ? getVel() : "");
    }
    //public String getTitle() {
    //    return "MyDaemon: " + (model.isSet(Position) ? getName() : "");
    //}


	@Override
	public boolean isDefined() {
		return getInstallation().isDefined() && !getVel().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		// Note, alternatively plain sockets can be used.
		writer.assign("mydaemon_message", getInstallation().getXMLRPCVariable() + ".get_message(\"" + getVel() + "\")");
		writer.assign("mydaemon_title", getInstallation().getXMLRPCVariable() + ".get_title()");
		writer.appendLine("popup(mydaemon_message, mydaemon_title, False, False, blocking=True)");
		writer.writeChildren();
	}

	private void updatePreview() {
		String title = "";
		String message = "";
		try {
			// Provide a real-time preview of the daemon state
			title = getInstallation().getXmlRpcDaemonInterface().getTitle();
			message = getInstallation().getXmlRpcDaemonInterface().getMessage(getVel());
		} catch (Exception e) {
			System.err.println("Could not retrieve essential data from the daemon process for the preview.");
			title = message = "<Daemon disconnected>";
		}

		titlePreviewLabel.setText(title);
		messagePreviewLabel.setText(message);
	}

	//private String getName() {
	//	return model.get(NAME, "");
	//}
    private String getVel() {
        return model.get(Velocity, "");
    }
    private String getPos() {
        return model.get(Position, "");
    }

	//private void setName(String name) {
	//	if ("".equals(name)){
	//		model.remove(NAME);
	//	}else{
	//		model.set(NAME, name);
	//	}
	//}

    private void setVel(String Vel) {
        if ("".equals(Vel)){
            model.remove(Velocity);
        }else{
            model.set(Velocity, Vel);
        }
    }

    private void setPos(String Pos) {
        if ("".equals(Pos)){
            model.remove(Position);
        }else{
            model.set(Position, Pos);
        }
    }

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
