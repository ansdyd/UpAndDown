package com.example.munyongjang.bipolardisorder;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by munyongjang on 3/4/17.
 */

// takes in the text to be analyzed
public class Microsoft extends AsyncTask<String, Void, String> {

    private static final String MICROSOFT_URL = "https://westus.api.cognitive.microsoft.com/text/analytics/v2.0/sentiment";
    private static final String API_KEY = "ENTER YOUR API KEY HERE";

    @Override
    protected String doInBackground(String... params) {
        String text = params[0];

        return sendPostRequest(makeJson(text), MICROSOFT_URL);
    }

    private static String sendPostRequest(String payload, String requestUrl) {
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

    // code to build the json string itself
    private static String makeJson (String text) {
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

}
