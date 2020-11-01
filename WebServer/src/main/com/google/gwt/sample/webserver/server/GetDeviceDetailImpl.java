package com.google.gwt.sample.webserver.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gwt.sample.webserver.client.GetDeviceDetailService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
@RemoteServiceRelativePath("details")
public class GetDeviceDetailImpl extends RemoteServiceServlet implements GetDeviceDetailService {
	final HashMap<String, String>[] result = new HashMap[1]; 
	
	Logger logger = java.util.logging.Logger.getLogger("deviceDetailsLogger");
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

	private HashMap<String, String> getDataFromDb(String id) {
		logger.info("Getting details of " + id);
		logger.info("Step 1");
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("details").child(id);
		logger.info("Step 2");
		ValueEventListener listener = new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				if (snapshot.exists()) {
				// TODO Auto-generated method stub
					logger.info("Data changed detected");
					logger.info(snapshot.getKey());
					result[0] = (HashMap<String, String>) snapshot.getValue();
					result[0].put("result", "success");
					logger.info(result[0].get("result"));				
				} else {
					result[0].put("result", "Not available");
				}
			}
			
			@Override
			public void onCancelled(DatabaseError error) {
				// TODO Auto-generated method stub
				logger.info("Data cancelled");
				result[0].put("result", error.getMessage());	
			}
		};
		ref.addValueEventListener(listener);
		logger.info("Listener : " + listener.toString());
		result[0].put("listener", listener.toString());
		return result[0];
	}
		
	@Override
	public HashMap<String, String> getDetails(String id) {
		// TODO Auto-generated method stub
		return getDataFromDb(id);
	}


//	@Override
//	public Boolean removeListener(ValueEventListener listener, String id) {
//		logger.info("Removing listener of " + id);
//		DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("details").child(id);
//		ref.removeEventListener(listener);
//		return true;
//	}

}
