package com.vizury.PushNotification;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonWriteExample {

	
	public JsonWriteExample() {
		
	}
	
	public void writeInATextFile() {
		try {
			FileWriter file = new FileWriter("testdata.txt");
			BufferedWriter bw = new BufferedWriter(file);
			for(int i = 0 ; i <= 100000 ; i++) {
				String gcmID = "asd_"+i;
				String msg = "sample_msg_"+i;
				
                String message = gcmID + "\t" + "{"+ "\"message\"" + ":" + "\"" +msg + "\"" + "}"+ "\n";
                bw.write(message);
			}
            file.flush();
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeInAFile() {
		JSONObject jobj1 = new JSONObject();
		JSONObject jobj = new JSONObject();
		int count = 1;

		
		jobj1.put("gcmID","APA91bE8OhMBy2ASRirr192Dp7nZB3Sqg-w11_9hGX37BywijgpMb2XvebC7ek2GoRwP8cqpOltwXI9Zei2Ewmocn7v7SEnuMsvQ_vziBJ6GXm8WYGv3RC8KJvbz5lQnlkBgIVDonL_RYfM0QXNwPzB0H_G9zwNWkHfZC84WIYiQrzTbaaYwZ2c");
		jobj1.put("message","Buy Blue Levis jeans only at Rs 2234");
		
		jobj.put(new Integer(count),jobj1);
		count++;
		JSONObject jobj2 = new JSONObject();
		
		jobj2.put("gcmID","APA91bFPq7EtKHQlwYhTrSaLgkg8AGAKf3KYSoNjdu8w_tfESd8I7eZ_t8CNdeBwQUGoipmegVYNMT1ScY3MKWSXohZ3e3VAat5CzyZRVKttG4yfPwuCiy4V-uaJdX4vmndgGiVkUISBDhEHwEP3vgjK820pii-Kpq5awtYGezHuo6Tt7gMS1-w");
		jobj2.put("message","Buy white shirt at rs 2000");
		
		
		jobj.put(new Integer(count),jobj2);
		
		try {
			FileWriter file = new FileWriter("input.json");
			file.write(jobj.toJSONString());
			file.flush();
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(jobj);
	}
}
