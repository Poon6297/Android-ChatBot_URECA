package com.example.poon6.ureca_shared_v1;

import android.app.Activity;
import android.os.AsyncTask;

import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class QueryTask extends AsyncTask<String, Void, AIResponse> {

    private Activity activity;
    private AIService aiService;

    public QueryTask(Activity activity, AIService aiService) {
        this.activity = activity;
        this.aiService = aiService;
    }

    @Override
    protected AIResponse doInBackground(String... strings) {

        AIRequest request = new AIRequest(strings[0]);
        try {
            return aiService.textRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(AIResponse aiResponse) {
        String botReply = ((MainActivity) activity).extractData(aiResponse);
        ((MainActivity) activity).displayBotReply(botReply);
    }
}
