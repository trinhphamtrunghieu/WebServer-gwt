package com.google.gwt.sample.webserver.client;

import com.google.firebase.database.ValueEventListener;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Device implements IsSerializable {
	
	ValueEventListener listener;
	String deviceId;
	String deviceName;
	String totalMes;
	String lastestTemp;
	String lastestMeas;
	String lastestDangerTemp;
	String lastestDangerTime;
	private Device() {
		
	}
}
