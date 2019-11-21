package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class WaitProgramNodeService implements ProgramNodeService {

	public WaitProgramNodeService() {
	}

	@Override
	public String getId() {
		return "WaitNode";
	}

	@Override
	public String getTitle() {
		return "Festo position achieved";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/examples/mydaemon/impl/Waitprogramnode.html");
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
		return new WaitProgramNodeContribution(api, model);
	}
}
