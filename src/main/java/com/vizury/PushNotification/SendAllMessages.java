package com.vizury.PushNotification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


/*
 * For sending messages to GCM
 */
public class SendAllMessages {

	private Logger logger = LoggerFactory.getLogger(SendAllMessages.class);

	private static final ExecutorService threadPool = Executors.newFixedThreadPool(20);

	HashMap<String,String> canonicalIDs;
	List<String> unRegisterIDs;
	String Key;
	String PATH;
	public static Sender sender;
	private int mthreshold = 500;           // number of devices to which messages will be sent
	private int TimeToLive = 172800;		//default time to live is 2 days
	Set<Callable<List<String>>> callables = new HashSet<Callable<List<String>>>();


	// call this method with the api key and the path 
	// of the json file
	public SendAllMessages(String apiKey, String path) {
		this.Key = apiKey;
		this.PATH = path;
		sender = new Sender(apiKey);
		canonicalIDs = new HashMap<String,String>();
		unRegisterIDs = new ArrayList<String>();

        int count = 0;

        readFileAndAddCallables();
		long time = System.currentTimeMillis();
		try {
			List<Future<List<String>>> futures = threadPool.invokeAll(callables);
			for(Future<List<String>> future : futures){
//			    unRegisterIDs.addAll(future.get());
                count += future.get().size();
			}
			// unRegisterIDs contain all the gcmID that have been unregistered
			// from the GCM server.
			// TODO :  The settings cache should be updated accordingly
//			logger.debug("Total number of un-registerd device " + unRegisterIDs.size() );
            logger.debug("Total messages sent " + count);
			
		} catch (InterruptedException e) {
            logger.error("Error in sendAllMessages InterruptedException " + e.getMessage());
            e.printStackTrace();
		} catch (ExecutionException e) {
            logger.error("Error in sendAllMessages ExecutionException " + e.getMessage());
            e.printStackTrace();
		}
		// shutdown the ExecutorService
		threadPool.shutdown();
        logger.debug("Total messages sent " + count);
        logger.debug("total time taken shutting down threadPool " + (System.currentTimeMillis() - time));

	}

	// create callable tasks for sending messages to 500 devices at a time
	public void readFileAndAddCallables() {
		BufferedReader br;
		HashMap<String,JSONObject> partialDevices = new HashMap<String, JSONObject>();

		try {
			br = new BufferedReader(new FileReader(PATH));
			String line = br.readLine();
	        while (line != null) {
	            String lineArr[] = line.split("\t");

	            String gcmID = "";
	            String payLoad= "{}";
	            
	            if(lineArr.length == 2) {
		            gcmID=lineArr[0];
		            payLoad=lineArr[1];
	            }
	            
	            JSONObject jobj = new JSONObject(payLoad);
	            partialDevices.put(gcmID, jobj);
	            if(partialDevices.size() == mthreshold) {
	            	addCallables(partialDevices);
	            	partialDevices.clear();
	            }
	            
	            line = br.readLine();
	            logger.debug("gcmID " + gcmID);
	            logger.debug("payload " + payLoad);
	        }
	        if(partialDevices.size() >= 0) {
	        	addCallables(partialDevices);
            	partialDevices.clear();
	        }
	        	
		} catch (IOException e) {
            logger.error("Error in readFileAndAddCallables: IO Exception  " + e.getMessage());
            e.printStackTrace();
		} catch (JSONException e) {
			logger.error("Error in readFileAndAddCallables: JSON Exception  " + e.getMessage());
            e.printStackTrace();
		} 
		
	}
	
	private void addCallables( HashMap<String, JSONObject> partialDevices) {

		final HashMap<String,JSONObject> partial = new HashMap<String, JSONObject>(partialDevices);
		callables.add(new Callable<List<String>>() {

			public List<String> call() throws Exception {
				long time = System.currentTimeMillis();
				List<String> unregistered = new ArrayList<String>();
				
		  		for(Map.Entry<String, JSONObject> e : partial.entrySet()) {
					String gcmID = e.getKey();
					JSONObject data = e.getValue();

					// adding the payload data 
					Builder mBuilder = new Message.Builder();
					if(data != null) {
						Iterator<?> itr = data.keys();
						while(itr.hasNext()) {
							String key = (String)itr.next();
							try {
								mBuilder.addData(key, (String)data.get(key));
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
						}
					}
					
					// add other optional attributes if required
					mBuilder.timeToLive(TimeToLive);

					Message message = mBuilder.build();
					logger.debug("sending message " + message.toString());
					Result result = null;
					try {
						result = sender.sendNoRetry(message, gcmID);
                        if(result != null)
                            logger.debug("Result " + result.toString());
					} catch (IOException e1) {
                        logger.error("Error in addCallables: IOException  " + e1.getMessage());
                        e1.printStackTrace();
					}


                    if(result != null) {

                        String messageId = result.getMessageId();
                        if (messageId != null) {
                            logger.debug("Succesfully sent message to device: " + gcmID +
                                    "; messageId = " + messageId);

//						String canonicalRegId = result.getCanonicalRegistrationId();
//						if (canonicalRegId != null) {
//							// same device has more than one registration id
//							// should be updated
//							canonicalIDs.put(gcmID, canonicalRegId);
//							logger.debug("canonicalRegId " + canonicalRegId);
//						}
                        } else {
                            String error = result.getErrorCodeName();
                            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                                // application has been removed from device - unregister it
//							unregistered.add(gcmID);
//							logger.debug("Unregistered device: " + gcmID);
                            } else {
                                unregistered.add(gcmID);
                                logger.debug("Error sending message to " + gcmID + " : " + error);
                            }
                        }
                    }
				}
				return unregistered;
			}});
	}
	
	
	// send message to all the devices sequentially
	
//	private void sendMessage() {
//		for(Map.Entry<String, JSONObject> e : mMap.entrySet()) {
//			String gcmID = e.getKey();
//			JSONObject data = e.getValue();
//
//			// adding the payload data 
//			// add optional attributes if required
//			Builder mBuilder = new Message.Builder();
//			if(data != null) {
//				Iterator<?> itr = data.keys();
//				while(itr.hasNext()) {
//					String key = (String)itr.next();
//					try {
//						mBuilder.addData(key, (String)data.get(key));
//					} catch (JSONException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//				}
//			}
//
//			Message message = mBuilder.build();
//			logger.debug("sending message " + message.toString());
//			Result result = null;
//			//send the message only once
//			try {
//				result = sender.sendNoRetry(message, gcmID);
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//
//			logger.debug("sent message to device : result " + result.toString());
//
//			String messageId = result.getMessageId();
//			if (messageId != null) {
//				logger.debug("Succesfully sent message to device: " + gcmID +
//						"; messageId = " + messageId);
//
//				String canonicalRegId = result.getCanonicalRegistrationId();
//				if (canonicalRegId != null) {
//					// same device has more than one registration id
//					// should be updated
//					canonicalIDs.put(gcmID, canonicalRegId);
//					logger.debug("canonicalRegId " + canonicalRegId);
//				}
//			} else {
//				String error = result.getErrorCodeName();
//				if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
//					// application has been removed from device - unregister it
//					unRegisterIDs.add(gcmID);
//					logger.debug("Unregistered device: " + gcmID);
//				} else {
//					logger.debug("Error sending message to " + gcmID + ": " + error);
//				}
//			}
//		}
//	}

}
