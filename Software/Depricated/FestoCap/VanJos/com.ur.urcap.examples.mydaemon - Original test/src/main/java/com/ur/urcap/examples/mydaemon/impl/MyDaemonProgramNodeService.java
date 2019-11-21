package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class MyDaemonProgramNodeService implements ProgramNodeService {

	public MyDaemonProgramNodeService() {
	}

	@Override
	public String getId() {
		return "LinearAxisNode";
	}

	@Override
	public String getTitle() {
		return "Festo-Linear-Axis-Motion";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/examples/mydaemon/impl/programnode.html");
		return is;
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean isChildrenAllowed() {
		return false;
	}

	@Override
	public ProgramNodeContribution createNode(URCapAPI api, DataModel model) {
		return new MyDaemonProgramNodeContribution(api, model);
	}
}
