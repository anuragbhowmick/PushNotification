package com.vizury.PushNotification.DataLayer;

import com.vizury.PushNotification.common.PNConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.Map;

/**
 * Created by anurag on 5/19/15.
 */
public class DBHelper {

    private Logger logger = LoggerFactory.getLogger(DBHelper.class);

    private String dbUserName;
    private String dbPassword;
    private String dbURL ;

    DBConnector dbconnector;

    public DBHelper() {
        dbUserName = PNConstants.properties.getString(PNConstants.PROP_DB_USERNAME,
                PNConstants.DEFAULT_DB_USERNAME);
        dbPassword = PNConstants.properties.getString(PNConstants.PROP_DB_PASSWORD,
                PNConstants.DEFAULT_DB_PASSWORD);
        dbURL = PNConstants.properties.getString(PNConstants.PROP_DB_SERVER_URL,
                PNConstants.DEFAULT_DB_SERVER_URL);
        logger.debug("Initializing dbConnector()");
        dbconnector = new DBConnector();
    }

    public void updateDBForStatusChange(String id, String status) {
        String query = "update VizardPNExecDetails set Status ='" + status + "' where Id=" + id;
        logger.debug("updateDBForStatusChange. Firing query {}", query);
        dbconnector.updateDB(dbUserName, dbPassword, dbURL, query);
    }

    public Map<Integer, Map<String, String>> queryDBForCampaignSettings(String advId) {
        String query = "select ServerKey from VizardPushNotificationSettings where AdvId=" + advId;
        logger.debug("queryDBForCampaignSettings. Firing query {}", query);
        return dbconnector.queryDB(dbUserName, dbPassword, dbURL,query);
    }

    public void updateDBForSuccessfulSending(String id, int sentCount, int successCount, int canonicalCount,
                                             int unRegisteredCount, int errorCount, double sendingTime) {
        String query = "update VizardPNExecDetails set SentCount = " + sentCount +
                ", SuccessCount=" + successCount + ", CanonicalCount=" + canonicalCount +
                ", UnregisteredCount=" + unRegisteredCount + ", ErrorCount=" + errorCount +
                ", SendingTime =" + sendingTime +
                ", Status = '" + PNConstants.DB_STATUS_SENT + "' where Id=" +id;

        logger.debug("updateDBForSuccessfulSending. query {}", query);
        dbconnector.updateDB(dbUserName, dbPassword, dbURL, query);
    }

    public void updateDBForFailedSending(String id, double sendingTime,
                                            String exceptionMessage) {
        String query = "update VizardPNExecDetails set SendingTime =" + sendingTime +
                ", Exceptions = '" + exceptionMessage + "', Status = '" + PNConstants.DB_STATUS_SENDING_FAILED +
                "' where Id=" + id;

        logger.debug("updateDBForFailedProcessing. query {}", query);
        dbconnector.updateDB(dbUserName, dbPassword, dbURL, query);
    }

    public Map<Integer, Map<String, String>> queryDBForSendingNotifications() {
//        String query = "select Id, AdvId, PNId, FileName from VizardPNExecDetails where Status like '" +
//                PNConstants.DB_STATUS_DATA_READY + "'";
        String query = "select a.Id,a.AdvId,a.PNId,a.FileName,b.Type from " +
                "(select Id, AdvId, PNId, FileName from VizardPNExecDetails where Status like 'dataready') a " +
                "INNER JOIN (select Id, Type from VizardPushNotification) b ON a.PNId=b.Id";
        return dbconnector.queryDB(dbUserName, dbPassword, dbURL, query);
    }
}
