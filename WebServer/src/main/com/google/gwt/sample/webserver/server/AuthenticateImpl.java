package com.google.gwt.sample.webserver.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gwt.sample.webserver.client.AuthenticateService;
import com.google.gwt.sample.webserver.client.User;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.Base64Utils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
@RemoteServiceRelativePath("authenticate")
public class AuthenticateImpl extends RemoteServiceServlet implements AuthenticateService {
	Logger logger = java.util.logging.Logger.getLogger("authenticateLogger");
	DatabaseReference ref = null;
	AtomicBoolean wasRun = new AtomicBoolean(false);
	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}
	
	
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
	
	private String readFromFb(User user) {
		logger.info("Getting user data from Firebase...");
		ref = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUsername());
		String[] result = new String[2];
		CountDownLatch countdown = new CountDownLatch(1);
		user.setPassword(Base64Utils.toBase64(user.getPassword().getBytes()));
		logger.info(user.getUsername() + " " + user.getPassword());
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				// TODO Auto-generated method stub
				if (!snapshot.exists()) {
					result[0] = "User does not exists";
					countdown.countDown();
				} else {
					User u = snapshot.getValue(User.class);
					if (user.getPassword().equals(u.getPassword())) {
						logger.info("User exists");
						result[0] =  "User already exists";
						countdown.countDown();
					} else {
						result[0] = "User does not exists";
						countdown.countDown();
					}
				}
			}
			
			@Override
			public void onCancelled(DatabaseError error) {
				// TODO Auto-generated method stub
				logger.log(Level.WARNING, "Error in getting user data");
				result[0] = "Error in connecting to Firebase";
			}
		});
		try {
			countdown.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result[0];
	}
	
	@Override
	public String authenticate(User user) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		loadStuff();
		return String.valueOf(readFromFb(user));
	}

	@Override
	public String register(User user) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		logger.log(Level.SEVERE, "Going to write to Firebase....");
		loadStuff();
		ref = FirebaseDatabase.getInstance().getReference();
		logger.info("HEY");
		DatabaseReference usersRef = ref.child("users");
		logger.info("HEYYY");
		user.setPassword(Base64Utils.toBase64(user.getPassword().getBytes()));
		String s = authenticate(user);
		if (!s.contains("User does not exists")) return s;
		if (usersRef.child(user.getUsername()).setValueAsync(user).isDone()) {
			return "Fail to register. Internal service error";
		};
		return "Register successfully. Now you can login";
	}
	
}
