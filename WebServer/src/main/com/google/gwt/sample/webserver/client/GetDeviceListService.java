package com.google.gwt.sample.webserver.client;

import java.util.HashMap;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("getDevice")
public interface GetDeviceListService extends RemoteService {
	HashMap<String,String> getAllDevice(User user);
}
