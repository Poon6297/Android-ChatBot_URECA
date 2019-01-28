package com.example.poon6.ureca_shared_v1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.poon6.ureca_shared_v1.Adapter.ChatMessageAdapter;
import com.example.poon6.ureca_shared_v1.Model.ChatMessage;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_MUSIC;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "debug";

    // CONSTANTS
    private final int REQUEST_PERMISSION_CODE = 1000;
    private int SAMPLE_RATE = 44100 ;
    private int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private int MIN_BUFF_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 3;


    private String filePath;
    private WavRecorder wavRecorder;
    private WebSocketClient mWebSocketClient;

    // TODO
    // change to recycler view
    private ListView mListView;
    private FloatingActionButton btnSend;
    private FloatingActionButton btnSpeech;
    private EditText mEditText;

    private ChatMessageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot_main);
        mListView = findViewById(R.id.list_view);
        btnSend = findViewById(R.id.btn_send);
        btnSpeech = findViewById(R.id.btn_speech);
        mEditText = findViewById(R.id.edit_text);

        adapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(adapter);

        btnSend.setOnClickListener(sendListener);
        btnSpeech.setOnTouchListener(speechListener);
    }

    // display reply from server to user
    private void botReply(String response) {
        ChatMessage chatMessage = new ChatMessage(false, false, response);
        adapter.add(chatMessage);
    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(false, true, message);
        adapter.add(chatMessage);

        // TODO
        // send message to server
    }

    private final View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = mEditText.getText().toString();

            sendMessage(message);

            mEditText.setText("");

            // TODO
            // response from server
            String response = "Hello There";

            botReply(response);
        }
    };

    private final View.OnTouchListener speechListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (checkPermissionFromDevice()) {
                    filePath = getFilePath();
                    Log.d(TAG, "onClick: " + filePath);

                    wavRecorder = new WavRecorder(filePath);
                    startRecord(wavRecorder);

                    Toast.makeText(MainActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
                } else {
                    requestPermission();
                }
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                stopRecord(wavRecorder);
                Log.d("debug","Recorder released");
                sendAudio(filePath);
                return true;
            }
            return false;
        }
    };

    private String getFilePath() {
        String filePath = (Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC).getAbsolutePath() + "/test_audio_record.pcm");
        return filePath;
    }



    private void requestPermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    startRecord(wavRecorder);
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return ((write_external_storage_result == PackageManager.PERMISSION_GRANTED)
                && (record_audio_result == PackageManager.PERMISSION_GRANTED));
    }


    private void displayReply(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEditText.setText(txt);
            }
        });
    }


    private void startRecord(WavRecorder wavRecorder) {
        wavRecorder.startRecording();
    }

    private void stopRecord(WavRecorder wavRecorder) {
        wavRecorder.stopRecording();
    }

    private void sendAudio(String filePath) {
        byte[] audioByte = getByteArrayFromFile(filePath);

        connectWebSocket();

        mWebSocketClient.send(audioByte);
    }

    private byte[] getByteArrayFromFile(String filePath) {

        File file = new File(filePath);
        byte[] audioByte = new byte[(int) file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(audioByte);
            fis.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return audioByte;
    }

    private void connectWebSocket() {
        try {
            URI uri = getURI();
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
                                if (isFinal) {
                                    Log.d(TAG, "onMessage: 1st result");
                                    JSONArray hypothesesArray = resultJSON.getJSONArray("hypotheses");
                                    JSONObject hypothesesJSONObject = hypothesesArray.getJSONObject(0);
                                    String transcript = hypothesesJSONObject.getString("transcript");

                                    // TODO
                                    displayReply(transcript);
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

    private URI getURI() throws java.net.URISyntaxException {

        Uri.Builder builder = new Uri.Builder();

        builder.scheme("ws")
                .encodedAuthority("118.189.188.87:8888")    // global
//                    .encodedAuthority("155.69.146.209:8888")  // NTU network
                .appendPath("client")
                .appendPath("ws")
                .appendPath("speech");

        Uri aUri = builder.build();
        URI jUri = new URI(aUri.toString());

        Log.d(TAG, "getURI: " + jUri);

        return jUri;
    }
}


