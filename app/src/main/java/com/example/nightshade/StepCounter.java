package com.example.nightshade;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StepCounter extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepCounter;
    private boolean countingSteps = false;
    private int stepsAtReset = -1;

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
        //startMotionActivity();

    }
    private void startMotionActivity() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
            countingSteps = true;
            stepsAtReset = -1;

            TextView textView = findViewById(R.id.textView3);
            textView.setText("Start walking...");
        } else {
            Log.d("StepCounter", "Step Counter not available!");
            TextView textView = findViewById(R.id.textView3);
            textView.setText("Step Counter not supported on this device.");
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER && countingSteps) {
            int totalSteps = (int) event.values[0];
            if (stepsAtReset == -1) stepsAtReset = totalSteps;
            int currentSteps = totalSteps - stepsAtReset;

            TextView textView = findViewById(R.id.textView3);
            textView.setText("Steps taken: " + currentSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}