package com.example.poon6.ureca_shared_v1;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;

public class QueryTask extends AsyncTask<String, Void, AIResponse> {

    private static final String TAG = "debug";

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
        Log.d(TAG, "onPostExecute: " + aiResponse);
        List<ResponseMessage> responseMessages = extractData(aiResponse);
        for (ResponseMessage responseMessage : responseMessages) {
            if (responseMessage instanceof ResponseMessage.ResponseSpeech) {
                List<String> speechList = ((ResponseMessage.ResponseSpeech) responseMessage).getSpeech();
                for (String response : speechList) {
                    ((MainActivity) activity).displayBotReply(response);
                }
            }

            if (responseMessage instanceof ResponseMessage.ResponseQuickReply) {
                String title = (((ResponseMessage.ResponseQuickReply) responseMessage).getTitle());
                ((MainActivity) activity).displayBotReply(title);

                // TODO: Display quickReplies in button form
                for (String quickReply : ((ResponseMessage.ResponseQuickReply) responseMessage).getReplies()) {
                    ((MainActivity) activity).displayBotReply(quickReply);
                }
            }

            // TODO: Display image properly
            if (responseMessage instanceof ResponseMessage.ResponseCard) {
                String uriString = ((ResponseMessage.ResponseCard) responseMessage).getImageUrl();
                ((MainActivity) activity).setImageView(uriString);

                String title = ((ResponseMessage.ResponseCard) responseMessage).getTitle();
                ((MainActivity) activity).displayBotReply(title);
            }
        }
    }

    protected List extractData(AIResponse aiResponse) {
        List botReply = new ArrayList<>();

        if (aiResponse != null) {
            // process aiResponse here
            List responseMessageList = aiResponse.getResult().getFulfillment().getMessages();
            for (int i=0; i<responseMessageList.size(); i++) {
                botReply.add(responseMessageList.get(i));
            }
        } else {
            Log.d(TAG, "extractData: aiResponse is null.");
        }

        return botReply;
    }
}
