/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vizury.PushNotification.Engine;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import static com.vizury.PushNotification.Engine.Constants.*;

/**
 * Helper class to send messages to the GCM service using an API Key.
 */
public class Sender {

    private static Logger logger = LoggerFactory.getLogger(Sender.class);

    private final String key;

    /**
     * Default constructor.
     *
     * @param key API key obtained through the Google API Console.
     */
    public Sender(String key) {
        this.key = nonNull(key);
    }

    public Result sendSingleMessage(Message message, String registrationId, String cookie) throws IOException {
        nonNull(registrationId);
        List<String> registrationIds = Collections.singletonList(registrationId);
        List<String> cookieList = Collections.singletonList(cookie);
        MulticastResult multicastResult = sendMultiCastMessage(message, registrationIds, cookieList);
        if (multicastResult == null) {
            return null;
        }
        List<Result> results = multicastResult.getResults();
        if (results.size() != 1) {
            logger.debug("Found " + results.size() +
                    " results in single multicast request, expected one");
            return null;
        }
        return results.get(0);
    }

    public MulticastResult sendMultiCastMessage(Message message,
                                       List<String> registrationIds, List<String> cookieList) throws IOException {
        logger.debug("Entering sendMessage");
        if (nonNull(registrationIds).isEmpty()) {
            logger.error("SendMessage, registrationIds cannot be empty ");
            throw new IllegalArgumentException("registrationIds cannot be empty");
        }
        Map<Object, Object> jsonRequest = new HashMap<Object, Object>();
        setJsonField(jsonRequest, PARAM_TIME_TO_LIVE, message.getTimeToLive());
        setJsonField(jsonRequest, PARAM_COLLAPSE_KEY, message.getCollapseKey());
        setJsonField(jsonRequest, PARAM_RESTRICTED_PACKAGE_NAME, message.getRestrictedPackageName());
        setJsonField(jsonRequest, PARAM_DELAY_WHILE_IDLE, message.isDelayWhileIdle());
        setJsonField(jsonRequest, PARAM_DRY_RUN, message.isDryRun());
        setJsonField(jsonRequest, JSON_PAYLOAD, message.getPayloadData());

        jsonRequest.put(JSON_REGISTRATION_IDS, registrationIds);
        String requestBody = JSONValue.toJSONString(jsonRequest);
        logger.debug("JSON request: " + requestBody);
        HttpURLConnection conn;
        int status;
        try {
            conn = post(GCM_SEND_ENDPOINT, "application/json", requestBody);
            status = conn.getResponseCode();
        } catch (IOException e) {
            logger.debug("IOException posting to GCM" + e.getMessage());
            return null;
        }
        String responseBody;
        if (status != 200) {
            try {
                responseBody = getAndClose(conn.getErrorStream());
                logger.debug("JSON error response: " + responseBody);
            } catch (IOException e) {
                // ignore the exception since it will thrown an InvalidRequestException
                // anyways
                responseBody = "N/A";
                logger.debug("Exception reading response: " + e.getMessage());
            }
            throw new InvalidRequestException(status, responseBody);
        }
        try {
            responseBody = getAndClose(conn.getInputStream());
        } catch (IOException e) {
            logger.error("IOException reading response, returning null result" + e.getMessage());
            return null;
        }
        logger.debug("JSON response: " + responseBody);

        JSONParser parser = new JSONParser();
        JSONObject jsonResponse;
        try {
            jsonResponse = (JSONObject) parser.parse(responseBody);
            int success = getNumber(jsonResponse, JSON_SUCCESS).intValue();
            int failure = getNumber(jsonResponse, JSON_FAILURE).intValue();
            int canonicalIds = getNumber(jsonResponse, JSON_CANONICAL_IDS).intValue();
            long multicastId = getNumber(jsonResponse, JSON_MULTICAST_ID).longValue();

            MulticastResult.Builder builder = new MulticastResult.Builder(success,
                    failure, canonicalIds, multicastId);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) jsonResponse.get(JSON_RESULTS);
            int count = 0;
            if (results != null) {
                for (Map<String, Object> jsonResult : results) {
                    String messageId = (String) jsonResult.get(JSON_MESSAGE_ID);
                    String canonicalRegId =
                            (String) jsonResult.get(TOKEN_CANONICAL_REG_ID);
                    String error = (String) jsonResult.get(JSON_ERROR);
                    String cookie = cookieList.get(count);
                    count++;
                    Result result = new Result.Builder()
                            .messageId(messageId)
                            .canonicalRegistrationId(canonicalRegId)
                            .errorCode(error)
                            .cookieValue(cookie)
                            .build();
                    builder.addResult(result);
                }
            }
            logger.debug("Finished sendMessage");
            return builder.build();
        } catch (ParseException e) {
            throw newIoException(responseBody, e);
        } catch (CustomParserException e) {
            throw newIoException(responseBody, e);
        }
    }


    private IOException newIoException(String responseBody, Exception e) {
        // log exception, as IOException constructor that takes a message and cause
        // is only available on Java 6
        String msg = "Error parsing JSON response (" + responseBody + ")";
        logger.error(msg + e);
        return new IOException(msg + ":" + e);
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore error
                logger.error("IOException closing stream" + e.getMessage());
            }
        }
    }

    /**
     * Sets a JSON field, but only if the value is not {@literal null}.
     */
    private void setJsonField(Map<Object, Object> json, String field,
                              Object value) {
        if (value != null) {
            json.put(field, value);
        }
    }

    private Number getNumber(Map<?, ?> json, String field) {
        Object value = json.get(field);
        if (value == null) {
            throw new CustomParserException("Missing field: " + field);
        }
        if (!(value instanceof Number)) {
            throw new CustomParserException("Field " + field +
                    " does not contain a number: " + value);
        }
        return (Number) value;
    }

    class CustomParserException extends RuntimeException {
        CustomParserException(String message) {
            super(message);
        }
    }


    /**
     * Makes an HTTP POST request to a given endpoint.
     * <p/>
     * <p/>
     * <strong>Note: </strong> the returned connected should not be disconnected,
     * otherwise it would kill persistent connections made using Keep-Alive.
     *
     * @param url         endpoint to post the request.
     * @param contentType type of request.
     * @param body        body of the request.
     * @return the underlying connection.
     * @throws java.io.IOException propagated from underlying methods.
     */
    protected HttpURLConnection post(String url, String contentType, String body)
            throws IOException {
        if (url == null || body == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }
        if (!url.startsWith("https://")) {
            logger.warn("URL does not use https: " + url);
        }

        logger.debug("Sending POST to " + url);
        logger.debug("POST body: " + body);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Authorization", "key=" + key);
        OutputStream out = conn.getOutputStream();
        try {
            out.write(bytes);
        } finally {
            close(out);
        }
        return conn;
    }

    /**
     * Gets an {@link java.net.HttpURLConnection} given an URL.
     */
    protected HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        return conn;
    }

    /**
     * Convenience method to convert an InputStream to a String.
     * <p/>
     * If the stream ends in a newline character, it will be stripped.
     * <p/>
     * If the stream is {@literal null}, returns an empty string.
     */
    protected static String getString(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream));
        StringBuilder content = new StringBuilder();
        String newLine;
        do {
            newLine = reader.readLine();
            if (newLine != null) {
                content.append(newLine).append('\n');
            }
        } while (newLine != null);
        if (content.length() > 0) {
            // strip last newline
            content.setLength(content.length() - 1);
        }
        return content.toString();
    }

    private static String getAndClose(InputStream stream) throws IOException {
        try {
            return getString(stream);
        } finally {
            if (stream != null) {
                close(stream);
            }
        }
    }

    static <T> T nonNull(T argument) {
        if (argument == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
        return argument;
    }

}
