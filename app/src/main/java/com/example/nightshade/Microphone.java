package com.example.nightshade;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaRecorder;

import java.io.File;

public class Microphone extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private File tempAudioFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_microphone);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startMicListening();
        } else {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MicDebug", "Permission granted, starting mic");
                startMicListening();
            } else {
                Log.d("MicDebug", "Mic permission denied");
            }
        }
    }
    private void startMicListening(){
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try{
            //if any previous recorder is already running, stop it
            if(mediaRecorder != null){
                mediaRecorder.release();
                mediaRecorder = null;
            }
            tempAudioFile = File.createTempFile("temp_audio", ".3gp", getCacheDir());
            Log.d("MicDebug", "Temp output file: " + tempAudioFile.getAbsolutePath());
            //set up new recorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(tempAudioFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d("MicDebug", "MediaRecorder started");

            //monitor volume
            new Thread(() -> {
                Log.d("MicDebug", "Mic listening thread started");
                while(mediaRecorder != null){
                    try {
                        Thread.sleep(200); //checks every 200ms
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    int volume = mediaRecorder.getMaxAmplitude();
                    Log.d("MicDebug", "Mic volume: " + volume);
                    if(volume > 1500){
                        runOnUiThread(() -> {
                            TextView micResult = findViewById(R.id.textView5);
                            micResult.setText("Nice exhale!");
                        });
                        stopMicListening();
                    }
                }
            }).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void stopMicListening(){
        if (mediaRecorder != null){
            try {
                mediaRecorder.stop();
            } catch (Exception ignored) {
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (tempAudioFile != null && tempAudioFile.exists()) {
            boolean deleted = tempAudioFile.delete();
            Log.d("MicDebug", "Temp file deleted: " + deleted);
            tempAudioFile = null;
        }
    }
}