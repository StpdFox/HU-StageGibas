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

public class WaitProgramNodeContribution implements ProgramNodeContribution {

	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public WaitProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}

	@Label(id = "wait")
	private LabelComponent wait;

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
		return "Festo-wait-for-completion";
	}

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// delay the ur program node with URscript.
		writer.appendLine("sleep(2)");
		writer.appendLine("global machine_finished = \"0\"");
		writer.appendLine("while machine_finished == \"0\":");
		writer.appendLine("    machine_finished = " + getInstallation().getXMLRPCVariable() + ".get_Done()");
		writer.appendLine("    sleep(0.05)");
		writer.appendLine("end");
	}

	private void updatePreview() {

		String message = "";
		try {
			// Provide a real-time preview of the daemon state

			message = getInstallation().getXmlRpcDaemonInterface().getDone();
		} catch (Exception e) {
			System.err.println("Could not retrieve essential data from the daemon process for the preview.");
			// title = message = "<Daemon disconnected>";
		}
		wait.setText(message);
	}

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
