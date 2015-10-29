package com.vizury.PushNotification.core;

import com.vizury.PushNotification.Engine.Constants;
import com.vizury.PushNotification.Engine.Message;
import com.vizury.PushNotification.Engine.Result;
import com.vizury.PushNotification.Engine.Sender;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by anurag on 10/27/15.
 */
public class PushHelper {

    private Logger logger = LoggerFactory.getLogger(PushHelper.class);

    public static final ExecutorService threadPool = Executors.newFixedThreadPool(20);

    private String filePath;

    protected String campaignId;
    protected Sender sender;
    protected int batchSize;                   // number of devices to which messages will be sent
    protected int timeToLive = 172800;        // default time to live is 2 days

    public int sentCount = 0;
    public int successCount = 0;
    public int canonicalCount = 0;
    public int unRegisteredCount = 0;
    public int errorCount = 0;
    public String exceptionMessage = null;

    List<Callable<List<Result>>> callables = new ArrayList<Callable<List<Result>>>();

    public PushHelper(String campaignId , String apiServerKey, String path, int batchSize) {
        logger.debug("Initializing SendAllMessage with api key {} and path {}", apiServerKey, path);
        this.filePath = path;
        this.campaignId = campaignId;
        this.batchSize = batchSize;
        this.sender = new Sender(apiServerKey);
    }

    /**
     * Read the file, construct callable and invoke all the callable
     * From the futures returned, get the number of success, un-install,
     * canonical and errors.
     * @return          boolean. True if the messages were sent successfully.
     */
    public boolean sendMessages() {
        logger.debug("PushHelper. sendMessages started");
        List<String> unInstallCookieList = new ArrayList<String>();

        try {
            readFileAndCreateCallable();
            List<Future<List<Result>>> futures = threadPool.invokeAll(callables);
            // Get the result of all the futures returned
            logger.debug("sendMessages future obtained with size {}", futures.size());
            int count = 0;
            for (Future<List<Result>> future : futures) {
                sentCount += future.get().size();
                for(Result result : future.get()) {
                    String messageId = result.getMessageId();
                    String cookie = result.getCookie();
                    count++;
                    logger.debug("message id is {}. and count {}", messageId,count);

                    if (messageId != null) {
                        successCount++;
                        String canonicalRegId = result.getCanonicalRegistrationId();
                        if (canonicalRegId != null) {
                            // same device has more than one registration id
                            // should be updated
                            logger.debug("got cannonical registration id in result");
                            canonicalCount += 1;
                        }
                    } else {
                        String errorCode = result.getErrorCodeName();
                        if (errorCode.equals(Constants.ERROR_NOT_REGISTERED)) {
                            // application has been removed from device
                            unInstallCookieList.add(cookie);
                            unRegisteredCount +=1;
                        } else {
                            errorCount +=1;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.error("Error in sendAllMessages InterruptedException " + e.getMessage());
            exceptionMessage = e.getMessage();
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            logger.error("Error in sendAllMessages ExecutionException " + e.getMessage());
            e.printStackTrace();
            exceptionMessage = e.getMessage();
            return false;
        } catch (JSONException e) {
            logger.error("Error in sendAllMessages JSONException " + e.getMessage());
            exceptionMessage = e.getMessage();
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            logger.error("Error in sendAllMessages IOException " + e.getMessage());
            exceptionMessage = e.getMessage();
            e.printStackTrace();
            return false;
        }
        boolean result = true;
        if (unInstallCookieList != null && unInstallCookieList.size() != 0) {
            try {
                result = handleUnInstallCookies(unInstallCookieList);
            } catch (Exception e) {
                logger.error("Error in sendAllMessages while handling unInstallCookies");
                exceptionMessage = e.getMessage();
                result = false;
            }
        }
        logger.debug("Total messages sent {}. Total canonical {}." +
                        "Total unregistered {}. Total errors {}.", sentCount,
                canonicalCount, unRegisteredCount,errorCount);
        return result;
    }

    protected boolean handleUnInstallCookies(List<String> cookieList) throws Exception {
        return true;
        // do nothing for normal push messages
    }

    private void readFileAndCreateCallable() throws IOException, JSONException {

        HashMap<Integer, String> partialDevices = new HashMap<Integer, String>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        int count = 0;
        while ((line=br.readLine()) != null) {
            partialDevices.put(count,line);
            count++;
            if (partialDevices.size() == batchSize) {
                addCallable(partialDevices);
                partialDevices.clear();
                count = 0;
            }
        }
        if (partialDevices.size() >= 0) {
            addCallable(partialDevices);
            partialDevices.clear();
        }
    }

    protected void addCallable(HashMap<Integer, String> partialDevices) {

        final HashMap<Integer, String> partial = new HashMap<Integer, String>(partialDevices);
        logger.debug("addCallable with partialDevice size of {}", partialDevices.size() );

        callables.add(new Callable<List<Result>>() {

            public List<Result> call() throws Exception {
                List<Result> results = new ArrayList<Result>();

                for (Map.Entry<Integer, String> entry : partial.entrySet()) {
                    String line  = entry.getValue();
                    String lineArr[] = line.split("\t");
                    String cookie = "";
                    String gcmID = "";
                    String payLoad = "{}";

                    if (lineArr.length == 3) {
                        cookie = lineArr[0];
                        gcmID = lineArr[1];
                        payLoad = lineArr[2];
                    }

                    JSONObject data = new JSONObject(payLoad);

                    Message.Builder mBuilder = new Message.Builder();
                    if (data != null) {
                        mBuilder.addPayLoadData(data);
                    }
                    // add other optional attributes if required
                    mBuilder.timeToLive(timeToLive);
                    Message message = mBuilder.build();

                    logger.debug("sending message " + message);
                    Result result = null;
                    try {
                        result = sender.sendSingleMessage(message, gcmID, cookie);
                    } catch (IOException e) {
                        logger.error("Error in addCallable: IOException  " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        logger.error("Exception while sending message " + e.getMessage());
                    }

                    if(result != null){
                        logger.debug("Result is " + result);
                        results.add(result);
                    }
                }
                return results;
            }
        });
    }

}
