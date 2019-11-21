package com.ur.urcap.examples.mydaemon.impl;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.DaemonService;

import java.net.MalformedURLException;
import java.net.URL;


public class MyDaemonDaemonService implements DaemonService {

	private DaemonContribution daemonContribution;

	public MyDaemonDaemonService() {
	}

	@Override
	public void init(DaemonContribution daemonContribution) {
		this.daemonContribution = daemonContribution;
		try {
			daemonContribution.installResource(new URL("file:com/ur/urcap/examples/mydaemon/impl/daemon/"));
		} catch (MalformedURLException e) {	}
	}


	@Override
	public URL getExecutable() {
		try {
			// reference to the python file:
			return new URL("file:com/ur/urcap/examples/mydaemon/impl/daemon/Festo.py"); // Python executable

		} catch (MalformedURLException e) {
			return null;
	    }
	}


	public DaemonContribution getDaemon() {
		return daemonContribution;
	}

}
