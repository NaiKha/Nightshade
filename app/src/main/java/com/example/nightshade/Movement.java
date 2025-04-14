package com.example.nightshade;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
import java.util.Arrays;
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




    private String[] activities = {"Microphone", "Step Counter", "Stretch"};


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        Button freeTime = (Button)findViewById(R.id.button3);

        freeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Movement.this, FreeTime.class));
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

        luxValueText = findViewById(R.id.luxValueText);


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

            luxValueText.setText("Lux: " + lux);

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

    private void randomAnswer() {
        Random random = new Random();
        int index = random.nextInt(activities.length);
        String rActivity = activities[index];

        switch (rActivity) {
            case "Microphone":
                startActivity(new Intent(Movement.this, Microphone.class));
                break;

            case "Step Counter":
                startActivity(new Intent(Movement.this, StepCounter.class));
                break;

            case "Stretch":
                startActivity(new Intent(Movement.this, Stretch.class));
                break;
        }

    }
    private void goBackToTimer() {
        Intent intent = new Intent(Movement.this, Timer.class);
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
