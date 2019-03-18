package com.example.poon6.ureca_shared_v1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.poon6.ureca_shared_v1.Adapter.ChatMessageAdapter;
import com.example.poon6.ureca_shared_v1.Model.ChatMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "debug";

    // CONSTANTS
    private final int REQUEST_PERMISSION_CODE = 1000;

    private URI uri = null;
    private AudioStream audioStream;

    // TODO
    // change to recycler view
    private ListView mListView;
    private FloatingActionButton btnSend;
    private FloatingActionButton btnSpeech;
    private EditText mEditText;
    private ChatMessageAdapter adapter;

    private AIService aiService;



    // test card reply
    ImageView iv;



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

        initChatBot();



        // test card reply
        iv = findViewById(R.id.iv_test);
    }

    private void initChatBot() {
        final AIConfiguration config = new AIConfiguration("00b66b29848a4c8d9edc270b029f3b8b",
                ai.api.AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);

    }

    // display reply from server to user
    protected void displayBotReply(String response) {
        ChatMessage chatMessage = new ChatMessage(false, false, response);
        adapter.add(chatMessage);
    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(false, true, message);
        adapter.add(chatMessage);

        // send message to server using AsyncTask
        QueryTask queryTask = new QueryTask(MainActivity.this, aiService);
        queryTask.execute(message);
    }

    private final View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = mEditText.getText().toString();

            sendMessage(message);

            mEditText.setText("");
        }
    };

    private final View.OnTouchListener speechListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (checkPermissionFromDevice()) {
                    try {
                        uri = getURI();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    audioStream = new AudioStream(MainActivity.this, uri);
                    audioStream.startStreaming();
                } else {
                    requestPermission();
                }
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                audioStream.stopStreaming();
                return true;
            }
            return false;
        }
    };

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
                    try {
                        uri = getURI();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    audioStream = new AudioStream(MainActivity.this, uri);
                    audioStream.startStreaming();
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


    protected void displaySpeechReply(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEditText.setText(txt);
            }
        });
    }

    private URI getURI() throws java.net.URISyntaxException {

        Uri.Builder builder = new Uri.Builder();

        builder.scheme("ws")
                .encodedAuthority("118.189.188.87:8888")    // global
//                    .encodedAuthority("155.69.146.209:8888")  // NTU network
                .appendPath("client")
                .appendPath("ws")
                .appendEncodedPath("speech?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1");

        Uri aUri = builder.build();
        URI jUri = new URI(aUri.toString());

        Log.d(TAG, "getURI: " + jUri);

        return jUri;
    }

    protected void setImageView(String uriString) {
        iv.setImageURI(Uri.parse(uriString));
    }
}


