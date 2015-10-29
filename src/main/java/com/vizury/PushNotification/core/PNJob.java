package com.vizury.PushNotification.core;

import com.vizury.PushNotification.DataLayer.DBHelper;
import com.vizury.PushNotification.common.PNConstants;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by anurag on 5/25/15.
 */
public class PNJob implements Job {

    Logger logger = LoggerFactory.getLogger(PNJob.class);
    static Object lockObject = new Object();
    DBHelper dbHelper;

    public void init() {
        logger.debug("PNJob init called. registering cron job");
        try {
            JobDetail job = JobBuilder.newJob(PNJob.class)
                    .withIdentity("PushNotificationCron", "group1").build();

            //configure the scheduler time
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("PushNotificationCron", "group1")
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule("0 0/2 * * * ?"))
                    .build();

            //schedule it

            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            logger.error("Could not start quartz scheduler " , e);
        }
    }

    /**
     * Checks the DB for sending pushNotifications. If any entry in the
     * DB contains status as 'dataready', start sending the messages
     * wrt that entry.
     */
    public void checkForSendingNotification() {
        logger.debug("checkForSendingNotification started.");
        dbHelper = new DBHelper();
        Map<Integer, Map<String, String>> scheduledResultMap = getNotificationsForProcessing();
        for(Map<String, String> rowValue : scheduledResultMap.values()) {
            String id = rowValue.get(PNConstants.DB_COLUMN_ID);
            String campaignId = rowValue.get(PNConstants.DB_COLUMN_ADV_ID);
            String fileName = rowValue.get(PNConstants.DB_COLUMN_FILENAME);
            String pushType = rowValue.get(PNConstants.DB_COLUMN_PUSH_TYPE);
            String serverApiKey = null;
            logger.debug("Starting sending message for id {}, advId {}, fileName {}",
                    id, campaignId, fileName);

            Map<Integer, Map<String, String>> campaignSettingResultMap = dbHelper.queryDBForCampaignSettings(campaignId);
            for(Map<String,String> settingRow : campaignSettingResultMap.values()) {
                serverApiKey = settingRow.get(PNConstants.DB_COLUMN_SERVER_KEY);
            }
            if (serverApiKey == null || fileName == null) {
                logger.error("serverApi key or the file cannot be null");
                dbHelper.updateDBForFailedSending(id, 0, "serverApi key or the file cannot be null");
                continue;
            }

            PushHelper pushHelper;
            if(pushType.compareTo(PNConstants.PUSH_TYPE_SILENT) == 0) {
                pushHelper = new SilentPushHelper(campaignId,serverApiKey,fileName,1000);
            } else {
                pushHelper = new PushHelper(campaignId,serverApiKey,fileName,500);
            }

            long StartTime = System.currentTimeMillis();
            boolean success = pushHelper.sendMessages();
            long timeTaken = (System.currentTimeMillis() - StartTime);
            double sendingTime = timeTaken;
            logger.debug("total time taken for sending message with id {} is {} ", id, timeTaken);
            if (success) {
                logger.debug("Successfully sent message for id {}", id);
                dbHelper.updateDBForSuccessfulSending(id, pushHelper.sentCount, pushHelper.successCount,
                        pushHelper.canonicalCount, pushHelper.unRegisteredCount,
                        pushHelper.errorCount, sendingTime);
            } else {
                logger.error("Error in sending message for id {}", id);
                dbHelper.updateDBForFailedSending(id, sendingTime, pushHelper.exceptionMessage);
            }

        }
        logger.debug("Finished checkForSendingNotification");
    }

    // Making sure that if one worker thread is querying the DB for scheduled
    // notifications, and updating the status to 'sending' other worker threads, are waiting.
    // This is done so that same file corresponding to a PNId is not
    // picked up by two different worker threads, and processed
    public synchronized Map<Integer, Map<String, String>> getNotificationsForProcessing() {
        Map<Integer, Map<String, String>> scheduledResultMap = dbHelper.queryDBForSendingNotifications();
        for(Map<String, String> rowValue : scheduledResultMap.values()) {
            String id = rowValue.get(PNConstants.DB_COLUMN_ID);
            dbHelper.updateDBForStatusChange(id, PNConstants.DB_STATUS_SENDING);
        }
        return scheduledResultMap;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("Started Executing PushNotification job");
        checkForSendingNotification();
    }
}
