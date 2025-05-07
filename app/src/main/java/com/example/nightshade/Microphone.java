package com.example.nightshade;

import android.Manifest;
import android.content.Intent;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaRecorder;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Microphone extends AppCompatActivity implements SensorEventListener {

    private MediaRecorder mediaRecorder;
    private File tempAudioFile;
    private volatile boolean isMicActive = false;
    private CountDownTimer countDown = null;
    private CountDownTimer miclistening;
    private TextView secs;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private float ax, ay, az;
    private long lastShakeTime = 0;
    private long startupTime = 0;
    private ImageView balloon;
    private float currentScale = 1f;

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

        balloon = (ImageView) findViewById(R.id.imageBalloon);

        ImageView back = (ImageView) findViewById(R.id.back2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Microphone.this, Movement.class));
            }
        });

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

            isMicActive = true;

            miclistening = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) { }
                public void onFinish() {
                    stopMicListening();
                    Toast.makeText(Microphone.this, "You did great!", Toast.LENGTH_LONG).show();
                    Log.d("MicDebug", "Mic stopped due to timeout");
                }
            }.start();

            //monitor volume
            new Thread(() -> {
                Log.d("MicDebug", "Mic listening thread started");
                while(isMicActive && mediaRecorder != null){
                    try {
                        Thread.sleep(200); //checks every 200ms
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    //assign volume safely, in case getMaxAmplitude() returns null
                    int volume;
                    try {
                        volume = mediaRecorder.getMaxAmplitude();
                    } catch (IllegalStateException | NullPointerException e){
                        Log.e("MicDebug", "Attempted to read from released MediaRecorder");
                        break;
                    }

                    Log.d("MicDebug", "Mic volume: " + volume);
                    if(volume > 25000){
                        runOnUiThread(() -> {
                            float targetScale = Math.min(1f + (volume / 25000f), 2.5f);
                            currentScale = currentScale + 0.05f * (targetScale - currentScale);                            balloon.setImageResource(R.drawable.inflated);
                            balloon.setScaleX(currentScale);
                            balloon.setScaleY(currentScale);

                            /*if (currentScale >= 2f) {
                                Toast.makeText(Microphone.this, "You did great!", Toast.LENGTH_SHORT).show();
                                stopMicListening();
                            }*/
                        });
                    }
                }
            }).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void stopMicListening(){
        isMicActive = false;
        if (mediaRecorder != null){
            try {
                mediaRecorder.stop();
            } catch (RuntimeException stopException) {
                Log.w("MicDebug", "Recorder stop failed: " + stopException.getMessage());
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (miclistening != null) {
            miclistening.cancel();
            miclistening = null;
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
                ImageView exhale = (ImageView) findViewById(R.id.imageView3);
                exhale.setImageResource(R.drawable.breath);
                secs.setVisibility(View.INVISIBLE);
                balloon.setVisibility(View.VISIBLE);
                TextView breathOut = (TextView) findViewById(R.id.textView11);
                breathOut.setText("Breath out:");

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
    protected void onPause() {
        super.onPause();
        stopMicListening();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Movement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); //removes current activity from stack
    }
}