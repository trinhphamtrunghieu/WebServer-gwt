package com.google.gwt.sample.webserver.client;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

public interface GetDeviceDetailServiceAsync {
	void getDetails(String s, AsyncCallback<HashMap<String,String>> callback) throws IllegalArgumentException;
}
