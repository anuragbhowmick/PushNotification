package com.vizury.PushNotification.common;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by anurag on 10/29/15.
 */
public class Utils {

    static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static void uploadFileToS3(AWSCredentials credentials,
                               String key, String fileName) throws Exception{
        // uploading bloom filter to s3
        logger.debug("Uploading a new object to S3 from a file\n");
        File s3File = new File(fileName);
        AmazonS3 s3client = new AmazonS3Client(credentials);
        s3client.putObject(new PutObjectRequest(PNConstants.S3BUCKET,
                key, s3File));
        logger.debug("Uploading to S3 DONE.\n");
    }
}
