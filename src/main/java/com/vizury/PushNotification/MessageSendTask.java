package com.vizury.PushNotification;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Message.Builder;

public class MessageSendTask extends RecursiveAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(MessageSendTask.class);

	HashMap<String,JSONObject> mMap;
	int threshold = 500;
	
	MessageSendTask(HashMap<String,JSONObject> map) {
		mMap = map;
	}
	
	void sendAsyncMessage() {
		System.out.println("sendAsyncMessage map size " + mMap.size() +
				" thread id " + Thread.currentThread().getId());
		long time = System.currentTimeMillis();
		logger.debug("sendAsyncMessage : map size " + mMap.size() + " thread id " + Thread.currentThread().getId());
		Thread.currentThread().getId();
		for(Map.Entry<String, JSONObject> e : mMap.entrySet()) {
			String gcmID = e.getKey();
			JSONObject data = e.getValue();

			// adding the payload data 
			// add optional attributes if required
			Builder mBuilder = new Message.Builder();
			if(data != null) {
				Iterator<?> itr = data.keys();

				while(itr.hasNext()) {
					String key = (String)itr.next();
					try {
						mBuilder.addData(key, (String)data.get(key));
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			Message message = mBuilder.build();
//			logger.debug("sending message " + message.toString());
			Result result = null;
			//send the message only once
			try {
				result = SendAllMessages.sender.sendNoRetry(message, gcmID);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		System.out.println("finished : thread id " + Thread.currentThread().getId() 
				+ " time taken "+ (System.currentTimeMillis() - time));

	}
	
	@Override
	protected void compute() {
		if(mMap.size() <= threshold) {
			sendAsyncMessage();
			return;
		}
		
		int total = mMap.size();
		int split = total/2;
		
		HashMap<String,JSONObject> partial1 = new HashMap<String, JSONObject>();
		HashMap<String,JSONObject> partial2 = new HashMap<String, JSONObject>();

		int counter = 0;
		for (Map.Entry<String, JSONObject> e : mMap.entrySet()) {
			if(counter < threshold)
				partial1.put(e.getKey(), e.getValue());
			else 
				partial2.put(e.getKey(), e.getValue());
			counter++;
		}
		System.out.println("size " + partial1.size() + " " + partial2.size() );

		invokeAll(new MessageSendTask(partial1), new MessageSendTask(partial2));
		// TODO Auto-generated method stub
		
	}

}
