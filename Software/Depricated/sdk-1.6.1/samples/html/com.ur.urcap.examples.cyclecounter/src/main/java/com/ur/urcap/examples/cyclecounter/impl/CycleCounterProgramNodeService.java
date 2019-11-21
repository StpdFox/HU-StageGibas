package com.ur.urcap.examples.cyclecounter.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class CycleCounterProgramNodeService implements ProgramNodeService {
	@Override
	public String getId() {
		return "CycleCounter";
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean isChildrenAllowed() {
		return true;
	}

	@Override
	public String getTitle() {
		return "Cycle Counter";
	}

	@Override
	public InputStream getHTML() {
		return this.getClass().getResourceAsStream("/com/ur/urcap/examples/cyclecounter/impl/CycleCounterProgramNode.html");
	}

	@Override
	public ProgramNodeContribution createNode(URCapAPI api, DataModel model) {
		return new CycleCounterProgramNodeContribution(api, model);
	}
}
