package com.google.gwt.sample.webserver.client;

import java.util.HashMap;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GetDeviceListServiceAsync {
	void  getAllDevice(User user, AsyncCallback<HashMap<String,String>> callback) throws IllegalArgumentException;

}
