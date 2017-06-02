package com.example.munyongjang.bipolardisorder;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

// basic code for requesting Microsoft's sentiment analysis api's
public class sentimentApiMicrosoft {

    // code to build the json string itself
    public static String makeJson (String text) {
        int idNum = (int) (Math.random() * 100) ;
        JSONObject document = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            // i think id needs to be a string
            document.put("id", idNum + "");
            document.put("language", "en");
            document.put("text", text);
            JSONArray documents = new JSONArray();
            documents.put(document);
            json.put("documents", documents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    // code for sending to Microsoft
    public static String sendPostRequest(String payload, String requestUrl) {
        StringBuffer jsonString = new StringBuffer();
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // this is my API key
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", "d9e1b76c741744b08500b5ef544688bd");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.d("test", e.toString());
        }
        return jsonString.toString();
    }
}
