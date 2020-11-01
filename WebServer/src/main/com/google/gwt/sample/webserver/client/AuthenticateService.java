package com.google.gwt.sample.webserver.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("authenticate")
public interface AuthenticateService extends RemoteService {
	String authenticate(User user) throws IllegalArgumentException;
	String register(User user) throws IllegalArgumentException;
}
