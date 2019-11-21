package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class EnableProgramNodeService implements ProgramNodeService {

	public EnableProgramNodeService() {
	}

	@Override
	public String getId() {
		return "EnableNode";
	}

	@Override
	public String getTitle() {
		return "Festo-Wait";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/examples/mydaemon/impl/Enableprogramnode.html");
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
		return new EnableProgramNodeContribution(api, model);
	}
}
