package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class HomingProgramNodeService implements ProgramNodeService {

	public HomingProgramNodeService() {
	}

	@Override
	public String getId() {
		return "HomingNode";
	}

	@Override
	public String getTitle() {
		return "Festo-Homing";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/examples/mydaemon/impl/Homingprogramnode.html");
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
		return new HomingProgramNodeContribution(api, model);
	}
}
