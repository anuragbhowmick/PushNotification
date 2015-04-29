package com.vizury.PushNotification;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReadJsonFile {
	
	public ReadJsonFile() {
		
	}
	
	// read the specified json file and populate a hashMAP
	public HashMap<String,JSONObject> readFile(String file) {
		
		JSONParser jparser = new JSONParser();
		HashMap<String,JSONObject> mMap = new HashMap<String,JSONObject>();
		try {
			Object obj =jparser.parse(new FileReader(file));
			JSONObject jobj =  (JSONObject)obj;
			String gcmID;
			JSONObject msg;
			JSONObject tmp;
			int count = 1;
			while((tmp = (JSONObject)jobj.get(String.valueOf(count))) != null) {
				 gcmID = (String)tmp.get("gcmID");
				 msg = (JSONObject)tmp.get("data");
				 mMap.put(gcmID, msg);
				 count++;
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mMap;
		
	}
}
