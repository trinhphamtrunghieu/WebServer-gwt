package com.google.gwt.sample.webserver.client;

import com.google.gwt.user.client.rpc.IsSerializable;


@SuppressWarnings("serial")
public class User implements IsSerializable{
	private String username;
	private String password;
	private String deviceId;
	
	public User() {
		
	}
	
	public User(String u, String p){
		username = u;
		password = p;
	}
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String p) {
		password = p;
	}
	public void setDeviceId(String d) {
		deviceId = d;
	}
	
}
