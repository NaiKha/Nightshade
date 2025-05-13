package com.example.nightshade;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Balance extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private ImageView levelImage;

    private TextView feedback;
    private ProgressBar balanceProgressBar;
    private int balanceSeconds = 0;
    private long lastBalanceTimestamp = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        feedback = findViewById(R.id.feedbackTextView);
        balanceProgressBar = findViewById(R.id.balanceProgressBar);


        levelImage = findViewById(R.id.level_image);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        ImageView back = (ImageView) findViewById(R.id.back2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Balance.this, Movement.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        balanceSeconds = 0;
        lastBalanceTimestamp = 0;
        if (balanceProgressBar != null) balanceProgressBar.setProgress(0);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float tiltAngle = -x * 5;
            levelImage.setRotation(tiltAngle);

            boolean isBalanced = Math.abs(x) < 2 && Math.abs(y) < 2 && z > 8 && z < 11;

            if (isBalanced) {
                long now = System.currentTimeMillis();
                if (lastBalanceTimestamp == 0 || now - lastBalanceTimestamp >= 1000) {
                    balanceSeconds++;
                    lastBalanceTimestamp = now;
                    balanceProgressBar.setProgress(balanceSeconds);
                }

                if (balanceSeconds >= 5) {
                    feedback.setText("Awesome! You did it! 🎉");
                    sensorManager.unregisterListener(this); // Stop checking
                } else {
                    feedback.setText("Good balance! Stay steady...");
                }
            } else {
                feedback.setText("Hold still... Try to balance!");
                balanceSeconds = 0;
                lastBalanceTimestamp = 0;
                balanceProgressBar.setProgress(0);
            }
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Movement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); //removes current activity from stack
    }
}
