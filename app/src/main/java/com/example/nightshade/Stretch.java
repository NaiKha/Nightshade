package com.example.nightshade;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Stretch extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ImageView stretchImage;
    private TextView readyText;

    private boolean armsUp = false;
    private boolean isReady = false;
    private long lastUpdateTime = 0;

    private static final float MOVEMENT_THRESHOLD = 5f;
    private static final long MIN_TIME_BETWEEN_SWITCH = 1500;
    private static final long START_DELAY_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stretch);

        stretchImage = findViewById(R.id.stretchImage);
        readyText = findViewById(R.id.readyText);

        stretchImage.setImageResource(R.drawable.stretch1);
        readyText.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            isReady = true;
            readyText.setVisibility(View.GONE);
        }, START_DELAY_MS);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isReady || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float z = event.values[2];
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdateTime < MIN_TIME_BETWEEN_SWITCH) return;

        if (!armsUp && z < -MOVEMENT_THRESHOLD) {
            stretchImage.setImageResource(R.drawable.stretch2); // arms up
            armsUp = true;
            giveFeedback();
            lastUpdateTime = currentTime;
        } else if (armsUp && z > MOVEMENT_THRESHOLD) {
            stretchImage.setImageResource(R.drawable.stretch1); // arms down
            armsUp = false;
            giveFeedback();
            lastUpdateTime = currentTime;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void giveFeedback() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(200);
        }
    }
}
