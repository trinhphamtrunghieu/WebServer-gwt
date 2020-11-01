package com.google.gwt.sample.webserver.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gwt.sample.webserver.client.GetDeviceListService;
import com.google.gwt.sample.webserver.client.User;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.type.Date;
import com.google.type.DateTime;

@SuppressWarnings("serial")
@RemoteServiceRelativePath("getDevice")
public class GetAllDeviceImpl extends RemoteServiceServlet implements GetDeviceListService {
	
	Logger logger = java.util.logging.Logger.getLogger("getAllDeviceLogger");
	public AtomicBoolean wasRun = new AtomicBoolean(false);
	public void loadStuff() {
		if (!wasRun.getAndSet(true)) {
			try {
				FileInputStream serviceAccount = new FileInputStream("../credential/account.json");
				GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(credentials)
						.setDatabaseUrl("https://database-383ce.firebaseio.com/")
						.build();
				FirebaseApp.initializeApp(options);
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
				logger.log(Level.WARNING, e.getMessage());
				logger.log(Level.WARNING, e.getCause().toString());
			}
		}
	}
	
	private HashMap<String,String> sendToFb(User u) {
		logger.info("TEST : " + u.getUsername());
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("devices").child(u.getUsername());
		final HashMap<String, String>[] result = new HashMap[2];
		CountDownLatch countdown = new CountDownLatch(1);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				// TODO Auto-generated method stub
				result[0] = (HashMap<String, String>) snapshot.getValue();
				countdown.countDown();
			}
			
			@Override
			public void onCancelled(DatabaseError error) {
				// TODO Auto-generated method stub
				logger.log(Level.WARNING, error.getDetails());
				countdown.countDown();
			}
		});
		try {
			countdown.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.log(Level.WARNING, "GOT DATA. " + result[0].toString());
		return result[0];
	}
	
	@Override
	public HashMap<String,String> getAllDevice(User user) {
		// TODO Auto-generated method stub
		return sendToFb(user);
	}

}
