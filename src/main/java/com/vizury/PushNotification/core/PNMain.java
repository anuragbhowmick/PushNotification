package com.vizury.PushNotification.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by anurag on 4/22/15.
 */
public class PNMain {

    private static Logger logger = LoggerFactory.getLogger(PNMain.class);

    public static void main(String[] args) {
        logger.debug("Starting PushNotification main");
        PNJob pJob = new PNJob();
        pJob.init();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                logger.debug("Shutdown initiated");
                // shutdown the ExecutorService
                logger.debug("Shutting down the ExecutorService");
                PushHelper.threadPool.shutdown();
                logger.debug("Good bye!");
            }
        });
    }
}
