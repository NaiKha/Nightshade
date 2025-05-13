package com.example.nightshade;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.MediaRecorder;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Movement extends ComponentActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;


    private float ax, ay, az;
    private long lastShakeTime = 0;

    private Sensor lightSensor;
    private boolean isCovered = false;
    private long coverStartTime = 0;
    private static final int COVER_THRESHOLD = 10;
    private static final int COVER_DURATION_MS = 3000;

    private TextView luxValueText;
    private boolean isActivityLaunching = false;
    private List<String> activityQueue = new ArrayList<>();


    private String[] activities = {"Microphone", "Step Counter", "Stretch", "Balance"};


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Movement.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        // set up baseline
        mAccel = 9f; // 10 approx. earth's gravity, was too high
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;




        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];


            if (lux < COVER_THRESHOLD) {
                if (!isCovered) {
                    isCovered = true;
                    coverStartTime = System.currentTimeMillis();
                } else {
                    long now = System.currentTimeMillis();
                    if (now - coverStartTime >= COVER_DURATION_MS) {
                        goBackToTimer();
                    }
                }
            } else {
                isCovered = false;
                coverStartTime = 0;
            }
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

        if (mAccel > 10f) {
            // add a time check for last shake to prevent answers being generated directly after each other.
            long time = System.currentTimeMillis();
            if (time - lastShakeTime > 2000){
                lastShakeTime = time;
                // generate answer
                randomAnswer();

                // vibrate
                vibrate();

                // add sound effect?
            }

        }

    }

    private void vibrate() {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Vibrate for 400 milliseconds
        v.vibrate(400);
    }

    private void initializeQueue() {
        activityQueue = new ArrayList<>(Arrays.asList(activities));
        Collections.shuffle(activityQueue);
    }
    private void randomAnswer() {
        if (isActivityLaunching) return;
        isActivityLaunching = true;

        if (activityQueue.isEmpty()) {
            initializeQueue(); // do when all activities are used
        }
        String rActivity = activityQueue.remove(0);
        Intent intent = null;

        switch (rActivity) {
            case "Microphone":
                intent = new Intent(this, Microphone.class);
                break;

            case "Step Counter":
                intent = new Intent(this, StepCounter.class);
                break;

            case "Stretch":
                intent = new Intent(this, Stretch.class);
                break;

            case "Balance":
                intent = new Intent(this, InstructionActivity.class);
                break;
        }
        if (intent != null){
            startActivity(intent);
            new Handler().postDelayed(() -> isActivityLaunching = false, 1000);
        }

    }
    private void goBackToTimer() {
        MediaPlayer mediaPlayer = MediaPlayer.create(Movement.this, R.raw.timer);
        mediaPlayer.start();

        Intent intent = new Intent(Movement.this, Timer.class);
        intent.putExtra("start_timer", true);  // Pass data to indicate that the timer should be started
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }








    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        if (lightSensor != null) {
            sensorManager.unregisterListener(this, lightSensor);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
