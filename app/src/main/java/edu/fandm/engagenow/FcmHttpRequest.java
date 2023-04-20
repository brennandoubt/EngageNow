package edu.fandm.engagenow;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FcmHttpRequest {
    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send";
    private static final String FCM_SERVER_KEY = "AIzaSyBjH4anaf7WzwHaBI0JY8u1hXlf1p5ppfY";
    private static final String FCM_CONTENT_TYPE = "application/json";

    public static void sendFcmMessage(String deviceToken, String messageTitle, String messageBody) {
        try {
            // Create a URL object
            URL url = new URL(FCM_API_URL);

            // Create a HttpURLConnection object
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            conn.setRequestMethod("POST");

            // Set the request headers
            conn.setRequestProperty("Authorization", "key=" + FCM_SERVER_KEY);
            conn.setRequestProperty("Content-Type", FCM_CONTENT_TYPE);

            // Create the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("to", deviceToken);

            JSONObject notification = new JSONObject();
            notification.put("title", messageTitle);
            notification.put("body", messageBody);

            jsonPayload.put("notification", notification);

            // Send the request
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(jsonPayload.toString());
            writer.flush();

            // Check the response code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = reader.readLine();
                Log.d("FcmHttpRequest", "FCM message sent. Response: " + response);
            } else {
                // Handle the error
                Log.e("FcmHttpRequest", "FCM message send failed. Response code: " + responseCode);
            }

            // Close the connections
            writer.close();
            conn.disconnect();
        } catch (Exception e) {
            Log.e("FcmHttpRequest", "Error sending FCM message", e);
        }
    }
}
