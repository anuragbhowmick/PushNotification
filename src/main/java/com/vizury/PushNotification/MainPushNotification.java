package com.vizury.PushNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by anurag on 4/22/15.
 */
public class MainPushNotification {


    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(MainPushNotification.class);

        long time = System.currentTimeMillis();
        logger.debug("time started " + time);

//        JsonWriteExample sampleJSON = new JsonWriteExample();
//    	sampleJSON.writeInATextFile();

        int cores = Runtime.getRuntime().availableProcessors();
        logger.debug("number of cores " + cores);

        String apiKey = "AIzaSyDcdlCN6YLkescly6Uvs05MVd-kxuPSRvo";
        String PATH = "/disk1/vizard/PushTest/testdata.txt";
        SendAllMessages sendmsg = new SendAllMessages(apiKey, PATH);
        logger.debug("total time taken " + (System.currentTimeMillis() - time));

    }
}
