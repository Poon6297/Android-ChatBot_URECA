package com.example.poon6.ureca_shared_v1;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class AudioStream {

    private final static String TAG = "AudioStream";
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSize;
    private boolean isRecording;

    private AudioRecord recorder;
    private WebSocketClient mWebSocketClient;
    private URI uri;
    private MainActivity activity;

    public AudioStream(MainActivity activity, URI uri) {
        this.activity = activity;
        this.uri = uri;
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        isRecording = false;
    }


    protected void startStreaming() {
        isRecording = true;
        connectWebSocket(uri);

        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {

                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize * 10);
                byte[] buffer = new byte[bufferSize];

                recorder.startRecording();

                while (isRecording) {

                    //reading data from MIC into buffer
                    bufferSize = recorder.read(buffer, 0, buffer.length);
                    mWebSocketClient.send(buffer);
                }
            }
        });
        streamThread.start();
    }

    protected void stopStreaming() {
        if (recorder != null) {
            isRecording = false;

            int i = recorder.getState();
            if (i == 1)
                recorder.stop();
            recorder.release();

            recorder = null;
        }
    }

    private void connectWebSocket(URI uri) {
        try {
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {

                }

                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject requestJSON = new JSONObject(message);
                        String requestString = requestJSON.toString();
                        Log.d(TAG, "requestString: " + requestString);
                        int status = requestJSON.getInt("status");
                        switch(status) {
                            case 0:
                                Log.d(TAG, "onMessage: success");
                                JSONObject resultJSON = requestJSON.getJSONObject("result");
                                boolean isFinal = resultJSON.getBoolean("final");
                                JSONArray hypothesesArray = resultJSON.getJSONArray("hypotheses");
                                JSONObject hypothesesJSONObject = hypothesesArray.getJSONObject(0);
                                String transcript = hypothesesJSONObject.getString("transcript");

                                Log.d(TAG, "onMessage: " + transcript);
                                activity.displaySpeechReply(transcript);


                                if (isFinal) {
                                    Log.d(TAG, "onMessage: Final -> " + transcript);
                                }
                                break;
                            case 2:
                                Log.d(TAG, "onMessage: aborted");
                                break;
                            case 1:
                                Log.d(TAG, "onMessage: no speech");
                                break;
                            case 9:
                                Log.d(TAG, "onMessage: not available");
                                break;
                            default:
                                Log.d(TAG, "onMessage: default case.");
                                Log.d(TAG, "onMessage: " + status);
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "onClose: " + code + reason);
                }

                @Override
                public void onError(Exception ex) {
                    Log.d(TAG, "onError: " + ex.getMessage());
                }
            };

            boolean connected = mWebSocketClient.connectBlocking();
            Log.d(TAG, "connectWebSocket: " + connected);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: url exception");
        }
    }








//    private void connectWebSocket() {
//        try {
//            URI uri = getURI();
//            mWebSocketClient = new WebSocketClient(uri) {
//                @Override
//                public void onOpen(ServerHandshake handshakedata) {
//
//                }
//
//                @Override
//                public void onMessage(String message) {
//                    if (a == 0) {
//                        firstMessageReceived = System.currentTimeMillis();
//                        a++;
//                    }
//                    try {
//                        JSONObject requestJSON = new JSONObject(message);
//                        String requestString = requestJSON.toString();
//                        Log.d(TAG, "requestString: " + requestString);
//                        int status = requestJSON.getInt("status");
//                        switch(status) {
//                            case 0:
//                                Log.d(TAG, "onMessage: success");
//                                JSONObject resultJSON = requestJSON.getJSONObject("result");
//                                boolean isFinal = resultJSON.getBoolean("final");
//                                JSONArray hypothesesArray = resultJSON.getJSONArray("hypotheses");
//                                JSONObject hypothesesJSONObject = hypothesesArray.getJSONObject(0);
//                                String transcript = hypothesesJSONObject.getString("transcript");
//
//                                displaySpeechReply(transcript);
//                                if (isFinal) {
//                                    a = 0;
//
//                                    Log.d(TAG, "onMessage: 1st result");
//                                    hypothesesArray = resultJSON.getJSONArray("hypotheses");
//                                    hypothesesJSONObject = hypothesesArray.getJSONObject(0);
//                                    transcript = hypothesesJSONObject.getString("transcript");
//
//                                    displaySpeechReply(transcript);
//                                    displayOnUi = System.currentTimeMillis();
//                                    Log.d(TAG, "time used to send data to server: " + (endTime - startTime));
//                                    Log.d(TAG, "time elapsed until the first response: " + (firstMessageReceived - endTime));
//                                    Log.d(TAG, "time elapsed until response is displayed on ui: " + (displayOnUi - endTime));
//                                    Log.d(TAG, "time elapsed between last response and first response: " + (displayOnUi - firstMessageReceived));
//                                }
//                                break;
//                            case 2:
//                                Log.d(TAG, "onMessage: aborted");
//                                break;
//                            case 1:
//                                Log.d(TAG, "onMessage: no speech");
//                                break;
//                            case 9:
//                                Log.d(TAG, "onMessage: not available");
//                                break;
//                            default:
//                                Log.d(TAG, "onMessage: default case.");
//                                Log.d(TAG, "onMessage: " + status);
//                                break;
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onClose(int code, String reason, boolean remote) {
//                    Log.d(TAG, "onClose: " + code + reason);
//                }
//
//                @Override
//                public void onError(Exception ex) {
//                    Log.d(TAG, "onError: " + ex.getMessage());
//                }
//            };
//
//            boolean connected = mWebSocketClient.connectBlocking();
//            Log.d(TAG, "connectWebSocket: " + connected);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d(TAG, "onCreate: url exception");
//        }
//    }
}
