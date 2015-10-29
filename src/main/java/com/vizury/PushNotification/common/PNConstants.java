package com.vizury.PushNotification.common;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by anurag on 5/13/15.
 */
public class PNConstants {
    private static final Logger logger = LoggerFactory.getLogger(PNConstants.class);
    private static final String CONFIG_FILE_NAME = "/PushNotification.properties";
    public static PropertiesConfiguration properties;

    static {
        // Default config file is loaded here
        logger.debug("calling init of PNConstants");
        init(CONFIG_FILE_NAME);
    }

    // This allows to load a different config file for testing
    public static void init(String configFile){
        logger.info("Loading config file " + configFile);
        try {
            properties = new PropertiesConfiguration();
            properties.load(PNConstants.class.getResourceAsStream(configFile));

        } catch (Exception e) {
            logger.error("Could not load config file " + configFile, e);
            System.exit(1);
        }
    }

    public static final String PROP_DB_DRIVER = "DBDriver";
    public static final String PROP_DB_USERNAME = "DBUsername";
    public static final String PROP_DB_PASSWORD = "DBPassword";
    public static final String PROP_DB_SERVER_URL = "VRMServerURL";

    public static final String DEFAULT_DB_DRIVER = "com.mysql.jdbc.Driver";
    public static final String DEFAULT_DB_USERNAME = "";
    public static final String DEFAULT_DB_PASSWORD = "";
    public static final String DEFAULT_DB_SERVER_URL = "";


    public static String DB_COLUMN_ID          = "Id";
    public static String DB_COLUMN_ADV_ID      = "AdvId";
    public static String DB_COLUMN_PNID          = "PNId";
    public static String DB_COLUMN_FILENAME     = "FileName";
    public static String DB_COLUMN_SERVER_KEY  = "ServerKey";
    public static String DB_COLUMN_PUSH_TYPE = "Type";

    public static String PUSH_TYPE_SILENT = "SILENT";

    public static String DB_STATUS_DATA_READY       = "dataready";
    public static String DB_STATUS_SENDING          = "sending";
    public static String DB_STATUS_SENT             = "sent";
    public static String DB_STATUS_SENDING_FAILED   = "sendingfailed";

    public static final String COMMON_NOTF_DIR =   "/disk1/RT/PushNotification/notificationData/";

    public static final String S3BUCKET = "viz-raw-data";
    public static final String LOCAL_UNINSTALL_DIR = "/disk1/mobiDMP/uninstallData/";

    public static final String DEFAULT_S3_SECRET_KEY = "";
    public static final String DEFAULT_S3_ACCESS_KEY = "";
}
