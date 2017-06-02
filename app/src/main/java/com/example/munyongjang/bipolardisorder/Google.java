package com.example.munyongjang.bipolardisorder;

import android.os.AsyncTask;
import android.renderscript.Type;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1.model.AnalyzeSentimentRequest;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.model.AnalyzeSentimentResponse;
import com.google.api.services.language.v1.model.Document;

import java.io.IOException;


/**
 * Created by munyongjang on 3/3/17.
 */

// AsyncTask to call upon Google's API, all it needs is the text to be analyzed
public class Google extends AsyncTask<Object, Void, Void> {

    private static final String API_KEY = "AIzaSyCPJ9Xff5lXkGjdLuoDw-cd_KITmKbptAY";

    @Override
    protected Void doInBackground(Object... params) {

        // following DDC's code
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        CloudNaturalLanguage.Builder builder = new CloudNaturalLanguage.Builder(httpTransport, jsonFactory, null);
        builder.setCloudNaturalLanguageRequestInitializer(new CloudNaturalLanguageRequestInitializer(API_KEY));
        CloudNaturalLanguage naturalLanguageAPI = builder.build();
        AnalyzeSentimentRequest analyzeSentimentRequest = new AnalyzeSentimentRequest();

        // the parameters that are passed in
        String textToBeAnalyzed = (String) params[0];
        SentimentScoreDatabaseHandler ssDB = (SentimentScoreDatabaseHandler) params[1];
        long time = (long) params[2];
        String sender = (String) params[3];


        Document document = new Document();
        document.setType("PLAIN_TEXT");
        document.setContent(textToBeAnalyzed);
        analyzeSentimentRequest.setDocument(document);
        try {
            CloudNaturalLanguage.Documents.AnalyzeSentiment sentimentRequest = naturalLanguageAPI.documents().analyzeSentiment(analyzeSentimentRequest);
            AnalyzeSentimentResponse response = sentimentRequest.execute();

            //TODO: write more processing functions for getting the information back
            double score = response.getDocumentSentiment().getScore();
            double magnitude = response.getDocumentSentiment().getMagnitude();

            ssDB.addSentimentScore(new SentimentScore(time, score, magnitude, sender));
            Log.d("google", "added sentiment");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
