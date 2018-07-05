package customresource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.json.JSONObject;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CustomResourceHandler implements RequestHandler<Map<String, Object>, Object> {

  @Override
  public Object handleRequest(Map<String, Object> input, Context context) {
    LambdaLogger logger = context.getLogger();
    logger.log("Input: " + input);

    String requestType = (String) input.get("RequestType");

    JSONObject responseData = new JSONObject();

    if (requestType!=null && requestType.equalsIgnoreCase("Create")) {
      logger.log("CREATE!");
      responseData.put("Message", "Resource creation successful!");
      return sendResponse(input, context, "SUCCESS", responseData);
    } else if (requestType!=null && requestType.equalsIgnoreCase("Update")) {
      logger.log("UDPATE!");
      responseData.put("Message", "Resource update successful!");
      return sendResponse(input, context, "SUCCESS", responseData);
    } else if (requestType!=null && requestType.equalsIgnoreCase("Delete")) {
      logger.log("DELETE!");
      responseData.put("Message", "Resource deletion successful!");
      return sendResponse(input, context, "SUCCESS", responseData);
    } else {
      logger.log("FAILURE!");
      return sendResponse(input, context, "FAILURE", responseData);
    }
  }

  public Object sendResponse(final Map<String, Object> input, final Context context, final String responseStatus,
    JSONObject responseData) {

    String responseURL = (String) input.get("ResponseURL");		
    context.getLogger().log("ResponseURL: " + responseURL);

    URL url;
    HttpURLConnection connection = null;
    try {
      url = new URL(responseURL);
      connection = (HttpURLConnection) url.openConnection();
      context.getLogger().log("1");
      connection.setDoOutput(true);
      context.getLogger().log("2");
      connection.setRequestMethod("PUT");
      context.getLogger().log("3");
      OutputStreamWriter response = new OutputStreamWriter(connection.getOutputStream());
      context.getLogger().log("4");
      connection.connect();
      context.getLogger().log("5");
      JSONObject responseBody = new JSONObject();
      context.getLogger().log("6");
      responseBody.put("Status", responseStatus);
      responseBody.put("PhysicalResourceId", context.getLogStreamName());
      responseBody.put("StackId", input.get("StackId"));
      responseBody.put("RequestId", input.get("RequestId"));
      responseBody.put("LogicalResourceId", input.get("LogicalResourceId"));
      responseBody.put("Data", responseData);
      context.getLogger().log("Response Body: " + responseBody.toString());
      response.write(responseBody.toString());
      response.close();
      context.getLogger().log("Response Code: " + connection.getResponseCode());
    } catch (Exception e) {
      context.getLogger().log(e.getMessage());
      e.printStackTrace();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    return null;
    }
  }
}
