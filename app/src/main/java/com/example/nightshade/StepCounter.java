package com.example.nightshade;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StepCounter extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepCounter;
    private boolean countingSteps = false;
    private int stepsAtReset = -1;
    private TextView textView;
    private ImageView imageView;
    private Handler handler;
    private Runnable imageSwitchRunnable;
    private boolean WalkingImage1 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_step_counter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView = findViewById(R.id.textView6);
        imageView = findViewById(R.id.imageView6);

        handler = new Handler();
        imageSwitchRunnable = new Runnable() {
            @Override
            public void run() {
                if (WalkingImage1) {
                    imageView.setImageResource(R.drawable.walk2);
                } else {
                    imageView.setImageResource(R.drawable.walk1);
                }
                WalkingImage1 = !WalkingImage1;
                handler.postDelayed(this, 750);
            }
        };

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 100);
        } else {
            startMotionActivity();
        }
    }

    private void startMotionActivity() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
            countingSteps = true;
            stepsAtReset = -1;
            textView.setText("Start walking...");
            handler.post(imageSwitchRunnable); // Start the image switching
        } else {
            textView.setText("Step Counter is not supported on this device.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            if (stepsAtReset == -1) {
                stepsAtReset = totalSteps;
            }

            int currentSteps = totalSteps - stepsAtReset;
            textView.setText("Steps taken: " + currentSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        }
        stepsAtReset = -1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(imageSwitchRunnable); // Stop the image switching when the activity is paused
    }
}
