package com.ur.urcap.examples.mydaemon.impl;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class XmlRpcMyDaemonInterface {

	private final XmlRpcClient client;

	public XmlRpcMyDaemonInterface(String host, int port) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setEnabledForExtensions(true);
		try {
			config.setServerURL(new URL("http://" + host + ":" + port + "/RPC2"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		config.setConnectionTimeout(1000); //1s
		client = new XmlRpcClient();
		client.setConfig(config);
	}

	public boolean isReachable() {
		try {
			client.execute("get_title", new ArrayList<String>());
			return true;
		} catch (XmlRpcException e) {
			return false;
		}
	}

	public String getTitle() throws XmlRpcException, UnknownResponseException {
		Object result = client.execute("get_title", new ArrayList<String>());
		return processString(result);
	}

	public String getDone() throws XmlRpcException, UnknownResponseException {
		Object result = client.execute("get_Done", new ArrayList<String>());
		return processString(result);
	}

	public String setTitle(String title) throws XmlRpcException, UnknownResponseException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(title);
		Object result = client.execute("set_title", args);
		return processString(result);
	}

	public String getMessage_vel(String Vel) throws XmlRpcException, UnknownResponseException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(Vel);
		Object result = client.execute("get_message_vel", args);
		return processString(result);
	}

	public String getMessage_pos(String Pos) throws XmlRpcException, UnknownResponseException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(Pos);
		Object result = client.execute("get_message_pos", args);
		return processString(result);
	}

	public String getMessage_enable(String Pos) throws XmlRpcException, UnknownResponseException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(Pos);
		Object result = client.execute("get_message_enable", args);
		return processString(result);
	}

	public String getMessage_homing(String Pos) throws XmlRpcException, UnknownResponseException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(Pos);
		Object result = client.execute("get_message_homing", args);
		return processString(result);
	}

	private boolean processBoolean(Object response) throws UnknownResponseException {
		if (response instanceof Boolean) {
			Boolean val = (Boolean) response;
			return val.booleanValue();
		} else {
			throw new UnknownResponseException();
		}
	}

	private String processString(Object response) throws UnknownResponseException {
		if (response instanceof String) {
			return (String) response;
		} else {
			throw new UnknownResponseException();
		}
	}
}