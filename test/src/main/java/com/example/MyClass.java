package com.example;


public class MyClass {
    // code to build the json string itself
    public static String makeJson (String text) {
        int idNum = (int) (Math.random() * 100);

        JS

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


    // just for testing
    public static void main(String[] args) {
        System.out.println(makeJson("hello"));
    }
}
