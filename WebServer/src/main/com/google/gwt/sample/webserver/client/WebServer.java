package com.google.gwt.sample.webserver.client;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.firebase.database.ValueEventListener;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.server.Base64Utils;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebServer implements EntryPoint {
	Logger logger = java.util.logging.Logger.getLogger("Logger");

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network " + "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final AuthenticateServiceAsync authenticateService = GWT.create(AuthenticateService.class);
	private final GetDeviceListServiceAsync getDeviceService = GWT.create(GetDeviceListService.class);
	
	private HashMap<String, String> deviceList = new HashMap<>();
	private final boolean[] afterSelect = new boolean[1];
	private int oldDeviceId = -1;
	
	private User user;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		logger.log(Level.SEVERE, "CALLED");
		loginPopUp();
	}
	private static class LoginPopup extends PopupPanel {
		public LoginPopup(Widget w, boolean clickToExit) {
			super(clickToExit);
			setWidget(w);
		}		
	}
	@SuppressWarnings("deprecation")
	public void loginPopUp() {
		final FlexTable flexTable = new FlexTable(); 
		final Label passwordLabel = new Label("Password");
		final Label usernameLabel = new Label("Username");
		final TextBox passwordField = new PasswordTextBox();
		final TextBox usernameField = new TextBox();
		final Button clearButton = new Button("Clear all");
		final Button loginButton = new Button("Login");
		final Button registerButton = new Button("Register");
		flexTable.setWidget(0, 0, usernameLabel);
		flexTable.setWidget(0, 1, usernameField);
		flexTable.setPixelSize(Window.getClientWidth()/5, Window.getClientHeight()/5);
		flexTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		flexTable.getFlexCellFormatter().setColSpan(0, 1, 4);
		
		flexTable.setWidget(1, 0, passwordLabel);
		flexTable.setWidget(1, 1, passwordField);
		flexTable.getFlexCellFormatter().setColSpan(1, 0, 2);
		flexTable.getFlexCellFormatter().setColSpan(1, 1, 4);
		
		flexTable.setWidget(2, 0, clearButton);
		flexTable.setWidget(2, 1, loginButton);
		flexTable.setWidget(2, 2, registerButton);
		flexTable.getFlexCellFormatter().setColSpan(2, 0, 2);
		flexTable.getFlexCellFormatter().setColSpan(2, 1, 2);
		flexTable.getFlexCellFormatter().setColSpan(2, 2, 2);
	
		
		for (int r = 0; r < flexTable.getRowCount(); r++){
			flexTable.getRowFormatter().setVerticalAlign(r, HasVerticalAlignment.ALIGN_MIDDLE);
			for (int c = 0; c < flexTable.getCellCount(r); c++) {
				flexTable.getCellFormatter().setHorizontalAlignment(r, c, HasHorizontalAlignment.ALIGN_RIGHT);
			}
		}

		
		final LoginPopup loginPopup = new LoginPopup(flexTable, false);
		loginPopup.getElement().setId("popup");
		loginPopup.center();
		WindowResizeListener listener = new WindowResizeListener() {
			@Override
			public void onWindowResized(int width, int height) {
				// TODO Auto-generated method stub
				loginPopup.center();
			}
		};
		Window.addWindowResizeListener(listener);
		loginPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				// TODO Auto-generated method stub
				int left = (Window.getClientWidth() - offsetWidth) / 2;
				int top = (Window.getClientHeight() - offsetHeight) / 4;
				loginPopup.setPopupPosition(left, top);
			}
		});
		
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				usernameField.setText("");
				passwordField.setText("");
			}
		});
		
		loginButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				if (checkFieldEmpty(passwordField) || checkFieldEmpty(usernameField)) {
					LoginPopup newPopup = showProcessingPopup("Username or password fields can not be empty", true);
					loginPopup.addStyleName("popup");
					newPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
						
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							// TODO Auto-generated method stub
							passwordField.setFocus(true);
							usernameField.setFocus(true);
							loginPopup.removeStyleName("popup");
							newPopup.clear();
						}
					});
				} else {
					User u = new User(usernameField.getText(), passwordField.getText());
					logger.info("TEST : " + u.getUsername() + " " + u.getPassword());
					LoginPopup processingPopup = showProcessingPopup("Processing....", false);
					loginPopup.addStyleName("popup");
					authenticateService.authenticate(u, new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							processingPopup.clear();
							loginPopup.removeStyleName("popup");
							logger.log(Level.WARNING, "failed to call to authenticate service");
							logger.log(Level.INFO, caught.getMessage());
							Window.alert("Failed to authenticate. Internal service error...");
						}

						@Override
						public void onSuccess(String result) {
							// TODO Auto-generated method stub
							processingPopup.removeStyleName("warnining-popup");
							processingPopup.clear();
							loginPopup.removeStyleName("popup");
							logger.log(Level.INFO, "Call to authenticate service succeed");
							logger.log(Level.INFO, "Result : " + result);
							Window.alert(result);
							if (result.contains("User already exists")) {
								Window.removeWindowResizeListener(listener);
								user = u;
								mainPanel();
								return;
							}
						}						
					});
				}
			}
		});
		
		registerButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				if (checkFieldEmpty(passwordField) || checkFieldEmpty(usernameField)) {
					LoginPopup newPopup = showProcessingPopup("Username or password fields can not be empty", true);
					loginPopup.addStyleName("popup");
					newPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
						
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							// TODO Auto-generated method stub
							passwordField.setFocus(true);
							usernameField.setFocus(true);
							loginPopup.removeStyleName("popup");
							newPopup.clear();
						}
					});
				} else {
					User u = new User(usernameField.getText(), passwordField.getText());
					LoginPopup processingPopup = showProcessingPopup("Processing....", false);
					loginPopup.addStyleName("popup");
					authenticateService.register(u, new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							logger.log(Level.WARNING, "failed to call to authenticate service");
							logger.log(Level.INFO, caught.getMessage());
							processingPopup.clear();
							loginPopup.removeStyleName("popup");
							Window.alert("Failed to call to register service");
						}

						@Override
						public void onSuccess(String result) {
							// TODO Auto-generated method stub
							logger.log(Level.INFO, "Call to authenticate service succeed");
							logger.log(Level.INFO, "Result : " + result);
							processingPopup.removeStyleName("warnining-popup");
							processingPopup.clear();
							loginPopup.removeStyleName("popup");
							if (result.contains("Register successfully")) {
								Window.alert(result);
							} else {
								LoginPopup errorPopup = showProcessingPopup(result, true);
								errorPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
									@Override
									public void onClose(CloseEvent<PopupPanel> event) {
										// TODO Auto-generated method stub
										passwordField.setText(" ");
										usernameField.setText(" ");
									}
									
								});
							}
						}
					});
				}
			}
		});
	}
	
	private boolean checkFieldEmpty(TextBox w) {
		return w.getText().isEmpty();
	}
	
	@SuppressWarnings("deprecation")
	private LoginPopup showProcessingPopup(String mess, boolean autoClose) {
		LoginPopup popup = new LoginPopup(new Label(mess), autoClose);
		popup.addStyleName("warning-popup");
		popup.center();
		popup.show();
		return popup;
	}
	
	private void mainPanel() {
		RootPanel.get().clear();
		final HorizontalPanel mainPanel = new HorizontalPanel();
		final FlexTable detailTable = new FlexTable();
		final FlexTable userTable = new FlexTable();
		
		mainPanel.add(selectDeviceTable());
		mainPanel.add(new DeviceData().getInstance().setUp());
		RootPanel.get("mainPanel").add(mainPanel);
	}
	
	private FlexTable selectDeviceTable() {
		
		final FlexTable deviceTable = new FlexTable();		
		final ListBox listBox = new ListBox();
		
		deviceTable.setWidget(0, 0, new Label("Choose device : "));
		deviceTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		deviceTable.setWidget(0, 1, listBox);
		ClickHandler updateListBoxHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				if (!afterSelect[0]) {
					HashMap<String, String> items = getAllDevice();
					logger.info("Got all devices");
					if (listBox.getItemCount() > 0) {
						for (int i = listBox.getItemCount() - 1; i >=0; i--) {
							listBox.removeItem(i);
						}
					}
					listBox.addItem("<Select an item>");
					if (!items.isEmpty()) {
						for (String s : items.keySet()) {
							logger.info("Adding : " + s);
							listBox.addItem(s);
						}
						listBox.setVisibleItemCount(1);				
					} else if (!deviceList.isEmpty()) {
						for (String s : deviceList.keySet()) {
							logger.info("Adding data from cache to list : " + s);
							listBox.addItem(s);
						}
						logger.info("Total : " + deviceList.size() + " items");
						listBox.setVisibleItemCount(1);
					}
				} else {
					afterSelect[0] = false;
				}
			}
		};
		
		final HandlerRegistration updateListBoxRegistrator[] = new HandlerRegistration[1];
		updateListBoxRegistrator[0] = listBox.addClickHandler(updateListBoxHandler);
		ChangeHandler selecItemListBoxHandler = new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				// TODO Auto-generated method stub
				//remove click handler to avoid duplicate
				afterSelect[0] = true;
				
				logger.info("Item selection caught");
				int index = listBox.getSelectedIndex();
				String item = listBox.getSelectedValue();
				logger.info("Item selected " + item + " at " + index);
				//remove already selected data
				listBox.removeItem(index);
				logger.info("Removed item at " + index);
				String[] listData = new String[listBox.getItemCount() - 1];
				//maintain default data
				for (int i = 0; i < listBox.getItemCount(); i++) {
					listData[i] = listBox.getItemText(i);
				}
				//remove all data
				for (int i = listBox.getItemCount() - 1; i >= 0; i--) {
					listBox.removeItem(i);
				}
				//add selected data to top
				listBox.insertItem(item, 0);
				logger.info("Added item " + item +" to index 0");
				//add other data
				for (int i = 0; i < listData.length; i++) {
					listBox.addItem(listData[i]);
				}
				
				if (item.equals("<Select an item>")) {
					DeviceData.getInstance().setDeviceId(null);
					oldDeviceId = -1;
					//stop getting update
				} else {
//					if (oldDeviceId > 0) DeviceData.getInstance().stopCall();
					DeviceData.getInstance().setDeviceId(deviceList.get(item));
					DeviceData.getInstance().updatingData();
					oldDeviceId = 1;
				}
			}
		};		
		HandlerRegistration selectItemListBoxRegistrator[] = new HandlerRegistration[1]; 
		selectItemListBoxRegistrator[0] = listBox.addChangeHandler(selecItemListBoxHandler);
		return deviceTable;
	}
	
	private HashMap<String, String> getAllDevice() {
		HashMap<String, String> devices = new HashMap<>();

		getDeviceService.getAllDevice(user, new AsyncCallback<HashMap<String, String>> (){

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				Window.alert("Can not get all device. Internal service error");
				logger.info(caught.getMessage().toString());
			}

			@Override
			public void onSuccess(HashMap<String, String> result) {
				// TODO Auto-generated method stub
				logger.info("Caught devices : ");
				if (!result.isEmpty()) {
					for (String s : result.keySet()) {
						devices.put(s, result.get(s));
						logger.info("Putting : " + s);
						deviceList.put(s,  result.get(s));
					}
				}
			}
		});
		return devices;
	};
	
	private static class DeviceData {
		private static DeviceData instance = new DeviceData();
		private final GetDeviceDetailServiceAsync getDetailService = GWT.create(GetDeviceDetailService.class);
		private Logger logger = java.util.logging.Logger.getLogger("DeviceDataLogger");

		private String deviceId = null;
		private ValueEventListener oldDeviceListener = null;
		private String oldDeviceId = null;
		
		final Label deviceNameLabel  = new Label("Device name :");
		final Label totalMeasurement = new Label("Total measurement :");
		final Label lastTemperature  = new Label("Lastest temperature :");
		final Label lastMeasureTime  = new Label("Lastest measure time :");
		final Label lastDangerTemperature = new Label("Lastest danger temperature :");
		final Label lastDangerTime   = new Label("Lastest danger time :");
		final Label currentTime      = new Label("Current time :");
		
		final Label deviceName  = new Label("Not available");
		final Label totalMeas   = new Label("Not available");
		final Label temperature = new Label("Not available");
		final Label measureTime = new Label("Not available");
		final Label dangerTemp  = new Label("Not available");
		final Label dangerTime  = new Label("Not available");
		final Label current     = new Label("Not available");
		
		private DeviceData() {
		}
		
		public FlexTable setUp() {
			final FlexTable dataTable = new FlexTable();		
			
			dataTable.setWidget(0, 0, deviceNameLabel);
			dataTable.setWidget(0, 1, deviceName);
			dataTable.setWidget(1, 0, totalMeasurement);
			dataTable.setWidget(1, 1, totalMeas);
			dataTable.setWidget(2, 0, lastTemperature);
			dataTable.setWidget(2, 1, temperature);
			dataTable.setWidget(3, 0, lastMeasureTime);
			dataTable.setWidget(3, 1, measureTime);
			dataTable.setWidget(4, 0, lastDangerTemperature);
			dataTable.setWidget(4, 1, dangerTemp);
			dataTable.setWidget(5, 0, lastDangerTime);
			dataTable.setWidget(5, 1, dangerTime);
			dataTable.setWidget(6, 1, currentTime);
			dataTable.setWidget(6, 1, current);

			return dataTable;
		}
		
		private boolean updateData(HashMap<String, String> data) {
			deviceName.setText(data.get("deviceName"));
			dangerTime.setText(data.get("dangerTime"));
			dangerTemp.setText(data.get("dangerTemp"));
			measureTime.setText(data.get("measureTime"));
			temperature.setText(data.get("temperature"));
			totalMeas.setText(data.get("total"));
			return true;
		}
		
		public void setDeviceId(String id) {
			deviceId = id;
		}
		
		public static DeviceData getInstance() {
			return instance;
		}
		
		public void updatingData() {
			logger.info("Getting data of " + deviceId);
			getDetailService.getDetails(deviceId, new AsyncCallback<HashMap<String,String>>() {

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					Window.alert("Can not get detail of device " + deviceId + " . Internal server error");
					logger.info(caught.getMessage());
				}

				@Override
				public void onSuccess(HashMap<String, String> result) {
					// TODO Auto-generated method stub
					if (!(result == null) && !result.get("result").equals("success")) {
						Window.alert(result.get("result"));
					} else if (!(result == null) && result.get("result").equals("Not availale")) {
						stopUpdatingData();
					} else if (!(result == null)){
//						oldDeviceListener = result.get("listener");
						updateData(result);
					}
				}
			});
		}
		
//		public void stopCall() {
//			logger.info("Stop getting data of " + oldDeviceId);
//			getDetailService.removeListener(oldDeviceId, new AsyncCallback<Boolean> (){
//
//				@Override
//				public void onFailure(Throwable caught) {
//					// TODO Auto-generated method stub
//					Window.alert(caught.getMessage());
//				}
//
//				@Override
//				public void onSuccess(Boolean result) {
//					// TODO Auto-generated method stub
//					if (result.booleanValue()) {
//						logger.info("Listener of " + oldDeviceId + " removed");
//					}
//				}
//			});
//		}
		
		public void stopUpdatingData() {
			deviceName.setText("Not available");
			dangerTime.setText("Not available");
			dangerTemp.setText("Not available");
			measureTime.setText("Not available");
			temperature.setText("Not available");
			totalMeas.setText("Not available");
		}
	}
}
