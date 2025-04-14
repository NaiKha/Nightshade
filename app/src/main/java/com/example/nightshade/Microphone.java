package com.example.nightshade;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaRecorder;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Microphone extends AppCompatActivity implements SensorEventListener {

    private MediaRecorder mediaRecorder;
    private File tempAudioFile;
    CountDownTimer countDown = null;
    TextView secs;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private float ax, ay, az;
    private long lastShakeTime = 0;
    private long startupTime = 0;

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
        startupTime = System.currentTimeMillis();

        // check permission for microphone usage
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        secs = (TextView)findViewById((R.id.textView20));
        //secs.setText("5");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        // set up baseline
        mAccel = 9f; // 10 approx. earth's gravity, was too high
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
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

    private void vibrate() {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Vibrate for 400 milliseconds
        v.vibrate(400);
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
                    if(volume > 25000){
                        runOnUiThread(() -> {
                            TextView micResult = findViewById(R.id.textView13);
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

    public void startTimer(TextView secs) {
        countDown = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                NumberFormat f = new DecimalFormat("0");
                long sec = (millisUntilFinished / 1000) % 60;
                secs.setText(f.format(sec));
            }
            public void onFinish() {
                secs.setText("0");
                vibrate();
                startMicListening();
                ImageView exhale = (ImageView) findViewById((R.id.imageView3));
                exhale.setImageResource(R.drawable.breath);
            }
        }.start();
    }

    public void cancelTimer(TextView secs) {
        if(countDown!=null)
            countDown.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMicListening();
        cancelTimer((TextView) findViewById(R.id.textView20));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // fetch x, y, z values, first checking if the sensor triggered is the accelerometer
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        }

        // save previous acceleration
        mAccelLast = mAccelCurrent;
        // use total acceleration magnitude formula
        mAccelCurrent = (float) Math.sqrt((double) ax * ax + ay * ay + az * az);
        // difference between last and new acceleration
        float delta = mAccelCurrent - mAccelLast;
        // multiply by 0.9f for a more stable shake detection
        mAccel = mAccel * 0.9f + delta;
        //mAccel = delta;

        long time = System.currentTimeMillis();
        if (time - startupTime < 2000) return;

        if (mAccel > 10f && time - lastShakeTime > 2000) {
            lastShakeTime = time;
            vibrate();
            startTimer(secs);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}