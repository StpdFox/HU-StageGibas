package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class PositionProgramNodeService implements ProgramNodeService {

	public PositionProgramNodeService() {
	}

	@Override
	public String getId() {
		return "PositionNode";
	}

	@Override
	public String getTitle() {
		return "Festo position achieved";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("com/ur/urcap/examples/mydaemon/impl/Positionprogramnode.html");
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
		return new PositionProgramNodeContribution(api, model);
	}
}
