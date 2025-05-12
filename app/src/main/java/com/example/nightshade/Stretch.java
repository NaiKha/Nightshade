package com.example.nightshade;

import android.content.Context;
import android.content.Intent;
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
    private Sensor linearAcceleration;

    private ImageView stretchImage;
    private TextView readyText;

    private boolean armsUp = false;
    private boolean isReady = false;
    private long lastEventTime = 0;
    private long lastTimestamp = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stretch);

        stretchImage = findViewById(R.id.stretchImage);
        readyText = findViewById(R.id.readyText);
        stretchImage.setImageResource(R.drawable.stretch1);
        readyText.setVisibility(View.VISIBLE);

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> didFinish());

        // Vi vill inte att en av aktiveringsskakningarna ska räknas...
        new Handler().postDelayed(() -> {
            isReady = true;
            readyText.setText(R.string.stretcha);
        }, 1000);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isReady || event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
            lastTimestamp = event.timestamp;
            return;
        }

        long nowNs = event.timestamp;
        if (lastTimestamp != 0L) {
            float deltaTime = (nowNs - lastTimestamp) / 1_000_000_000f;
            float y = event.values[1];
            long now = System.currentTimeMillis();
            if (Math.abs(y * deltaTime) > 0.275f && (now - lastEventTime) > 2000) {
                toggleStretch();
                lastEventTime = now;
                resetTime();
            }
        }
        lastTimestamp = nowNs;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void toggleStretch() {
        stretchImage.setImageResource(armsUp ? R.drawable.stretch1 : R.drawable.stretch2);
        armsUp = !armsUp;
        giveFeedback();
    }

    private void giveFeedback() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private void resetTime() {
        lastTimestamp = 0L;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        didFinish();
    }

    private void didFinish() {
        Intent intent = new Intent(this, Movement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}