package com.google.gwt.sample.webserver.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface AuthenticateServiceAsync {
	void authenticate(User user, AsyncCallback<String> callback) throws IllegalArgumentException;
	void register(User user, AsyncCallback<String> callback) throws IllegalArgumentException;
}
