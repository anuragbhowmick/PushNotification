package com.google.android.gcm.server;

public class VizSender extends Sender{

	public VizSender(String key) {
		super(key);
		// TODO Auto-generated constructor stub
	}
//
//	@Override
//	public Result send(Message message, String registrationId, int retries)
//			throws IOException {
//	    int attempt = 0;
//	    Result result = null;
//	    int backoff = BACKOFF_INITIAL_DELAY;
//	    boolean tryAgain;
//	    do {
//	      attempt++;
//	      if (logger.isLoggable(Level.FINE)) {
//	        logger.fine("Attempt #" + attempt + " to send message " +
//	            message + " to regIds " + registrationId);
//	      }
//	      result = sendNoRetry(message, registrationId);
//	      tryAgain = result == null && attempt <= retries;
//	      if (tryAgain) {
//	        int sleepTime = backoff / 2 + random.nextInt(backoff);
//	        sleep(sleepTime);
//	        if (2 * backoff < MAX_BACKOFF_DELAY) {
//	          backoff *= 2;
//	        }
//	      }
//	    } while (tryAgain);
//	    if (result == null) {
//	      throw new IOException("Could not send message after " + attempt +
//	          " attempts");
//	    }
//	    return result;
//	  }
//
//	@Override
//	public Result sendNoRetry(Message message, String registrationId)
//			throws IOException {
//	    StringBuilder body = newBody(PARAM_REGISTRATION_ID, registrationId);
//	    Boolean delayWhileIdle = message.isDelayWhileIdle();
//	    if (delayWhileIdle != null) {
//	      addParameter(body, PARAM_DELAY_WHILE_IDLE, delayWhileIdle ? "1" : "0");
//	    }
//	    Boolean dryRun = message.isDryRun();
//	    if (dryRun != null) {
//	      addParameter(body, PARAM_DRY_RUN, dryRun ? "1" : "0");
//	    }
//	    String collapseKey = message.getCollapseKey();
//	    if (collapseKey != null) {
//	      addParameter(body, PARAM_COLLAPSE_KEY, collapseKey);
//	    }
//	    String restrictedPackageName = message.getRestrictedPackageName();
//	    if (restrictedPackageName != null) {
//	      addParameter(body, PARAM_RESTRICTED_PACKAGE_NAME, restrictedPackageName);
//	    }
//	    Integer timeToLive = message.getTimeToLive();
//	    if (timeToLive != null) {
//	      addParameter(body, PARAM_TIME_TO_LIVE, Integer.toString(timeToLive));
//	    }
//	    for (Entry<String, String> entry : message.getData().entrySet()) {
//	      String key = entry.getKey();
//	      String value = entry.getValue();
//	      if (key == null || value == null) {
//	        logger.warning("Ignoring payload entry thas has null: " + entry);
//	      } else {
//	        key = PARAM_PAYLOAD_PREFIX + key;
//	        addParameter(body, key, URLEncoder.encode(value, UTF8));
//	      }
//	    }
//	    String requestBody = body.toString();
//	    logger.finest("Request body: " + requestBody);
//	    HttpURLConnection conn;
//	    int status;
//	    try {
//	      conn = post(GCM_SEND_ENDPOINT, requestBody);
//	      status = conn.getResponseCode();
//	    } catch (IOException e) {
//	      logger.log(Level.FINE, "IOException posting to GCM", e);
//	      return null;
//	    }
//	    if (status / 100 == 5) {
//	      logger.fine("GCM service is unavailable (status " + status + ")");
//	      return null;
//	    }
//	    String responseBody;
//	    if (status != 200) {
//	      try {
//	        responseBody = getAndClose(conn.getErrorStream());
//	        logger.finest("Plain post error response: " + responseBody);
//	      } catch (IOException e) {
//	        // ignore the exception since it will thrown an InvalidRequestException
//	        // anyways
//	        responseBody = "N/A";
//	        logger.log(Level.FINE, "Exception reading response: ", e);
//	      }
//	      throw new InvalidRequestException(status, responseBody);
//	    } else {
//	      try {
//	        responseBody = getAndClose(conn.getInputStream());
//	      } catch (IOException e) {
//	        logger.log(Level.WARNING, "Exception reading response: ", e);
//	        // return null so it can retry
//	        return null;
//	      }
//	    }
//	    String[] lines = responseBody.split("\n");
//	    if (lines.length == 0 || lines[0].equals("")) {
//	      throw new IOException("Received empty response from GCM service.");
//	    }
//	    String firstLine = lines[0];
//	    String[] responseParts = split(firstLine);
//	    String token = responseParts[0];
//	    String value = responseParts[1];
//	    if (token.equals(TOKEN_MESSAGE_ID)) {
//	      Builder builder = new Result.Builder().messageId(value);
//	      // check for canonical registration id
//	      if (lines.length > 1) {
//	        String secondLine = lines[1];
//	        responseParts = split(secondLine);
//	        token = responseParts[0];
//	        value = responseParts[1];
//	        if (token.equals(TOKEN_CANONICAL_REG_ID)) {
//	          builder.canonicalRegistrationId(value);
//	        } else {
//	          logger.warning("Invalid response from GCM: " + responseBody);
//	        }
//	      }
//	      Result result = builder.build();
//	      if (logger.isLoggable(Level.FINE)) {
//	        logger.fine("Message created succesfully (" + result + ")");
//	      }
//	      return result;
//	    } else if (token.equals(TOKEN_ERROR)) {
//	      return new Result.Builder().errorCode(value).build();
//	    } else {
//	      throw new IOException("Invalid response from GCM: " + responseBody);
//	    }
//	  }
//
//	@Override
//	public MulticastResult send(Message message, List<String> regIds,
//			int retries) throws IOException {
//		// TODO Auto-generated method stub
//		return super.send(message, regIds, retries);
//	}
//
//	@Override
//	public MulticastResult sendNoRetry(Message message,
//			List<String> registrationIds) throws IOException {
//		// TODO Auto-generated method stub
//		return super.sendNoRetry(message, registrationIds);
//	}

}
