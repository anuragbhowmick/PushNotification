package com.vizury.PushNotification.core;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.vizury.PushNotification.Engine.Message;
import com.vizury.PushNotification.Engine.MulticastResult;
import com.vizury.PushNotification.Engine.Result;
import com.vizury.PushNotification.common.PNConstants;
import com.vizury.PushNotification.common.Utils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by anurag on 10/28/15.
 */
public class SilentPushHelper extends PushHelper {

    private Logger logger = LoggerFactory.getLogger(SilentPushHelper.class);
    private static String secretKey;
    private static String accessKey;

    static {
        secretKey = PNConstants.properties.getString("SecretKey",
                PNConstants.DEFAULT_S3_SECRET_KEY);
        accessKey = PNConstants.properties.getString("AccessKey",
                PNConstants.DEFAULT_S3_ACCESS_KEY);
    }

    public SilentPushHelper(String campaignId, String apiKey, String path, int batchSize) {
        super(campaignId, apiKey, path, batchSize);
    }

    @Override
    protected void addCallable(HashMap<Integer, String> partialDevices) {
        final HashMap<Integer, String> partial = new HashMap<Integer, String>(partialDevices);
        logger.debug("SilentPushHelper : addCallable with partialDevice size of {}", partialDevices.size() );

        callables.add(new Callable<List<Result>>() {

            public List<Result> call() throws Exception {
                List<Result> results = new ArrayList<Result>();
                List<String> registrationIds = new ArrayList<String>();
                List<String> cookieList = new ArrayList<String>();

                String payLoad = "{}";
                for (Map.Entry<Integer, String> entry : partial.entrySet()) {
                    String line  = entry.getValue();
                    String lineArr[] = line.split("\t");
                    if (lineArr.length == 3) {
                        String cookie = lineArr[0];
                        String gcmID = lineArr[1];
                        payLoad = lineArr[2];
                        registrationIds.add(gcmID);
                        cookieList.add(cookie);
                    }
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

                try {
                    MulticastResult multicastResult = sender.sendMultiCastMessage(message, registrationIds, cookieList);
                    if (multicastResult == null) {
                        return null;
                    }
                    results = multicastResult.getResults();
                } catch (IOException e) {
                    logger.error("Error in addCallables: IOException  " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error("Runtime Exception while sending message " + e.getMessage());
                }
                return results;
            }
        });
    }

    @Override
    public boolean handleUnInstallCookies(List<String> cookieList) throws Exception {
        // update aerospike, upload to s3 and send sqs message to RTB
        DateTime currentDateTime = new DateTime(DateTimeZone.UTC);
        String localFileName = PNConstants.LOCAL_UNINSTALL_DIR +
                campaignId + "_" +currentDateTime.toString("yyyyMMddHHmm") + ".tsv";
        String targetFileName = currentDateTime.toString("yyyy/MM/dd/") +
                "VIZVRM" + campaignId + "/" + currentDateTime.toString("HHmm") + ".tsv";
        logger.debug("local file name {}", localFileName);
        logger.debug("target file name {}", targetFileName);
        String currentDate = currentDateTime.toString("yyyy-MM-dd");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(localFileName));
            for(String cookie : cookieList) {
                String message = "VIZVRM" + campaignId + "\t" + cookie + "\t" + currentDate + "\n";
                bw.write(message);
            }
        } catch (Exception e) {
            logger.error("Exception in handleUnInstallCookies");
            throw e;
        } finally {
            if(bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String key = "Mobi-DMP/uninstalls/"+targetFileName;
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        Utils.uploadFileToS3(awsCredentials, key, localFileName);
        return true;
    }
}
