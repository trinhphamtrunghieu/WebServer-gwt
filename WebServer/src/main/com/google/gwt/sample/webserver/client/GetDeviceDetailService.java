package com.google.gwt.sample.webserver.client;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("details")
public interface GetDeviceDetailService extends RemoteService {
	HashMap<String, String> getDetails(String id);
}
