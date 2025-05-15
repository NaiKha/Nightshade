package com.example.nightshade;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class Balance extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private ImageView levelImage;
    private KonfettiView konfettiView;
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
        konfettiView = findViewById(R.id.konfettiView);



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
                if (lastBalanceTimestamp == 0 || now - lastBalanceTimestamp >= 100) {
                    balanceSeconds++;
                    lastBalanceTimestamp = now;

                    ObjectAnimator anim = ObjectAnimator.ofInt(
                            balanceProgressBar,
                            "progress",
                            balanceProgressBar.getProgress(),
                            balanceSeconds
                    );
                    anim.setDuration(90);
                    anim.start();
                }

                if (balanceSeconds >= 50) {
                    feedback.setText("Awesome! You did it! 🎉");
                    triggerCelebration();
                    sensorManager.unregisterListener(this);
                } else {
                    feedback.setText("Good balance! Stay steady...");
                }
            } else {

                feedback.setText("Hold still... Try to balance!");
                balanceSeconds = 0;
                lastBalanceTimestamp = 0;

                ObjectAnimator resetAnim = ObjectAnimator.ofInt(
                        balanceProgressBar,
                        "progress",
                        balanceProgressBar.getProgress(),
                        0
                );
                resetAnim.setDuration(300);
                resetAnim.start();
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

    private void triggerCelebration() {
        EmitterConfig emitterConfig = new Emitter(500, TimeUnit.MILLISECONDS).max(500);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .shapes(Shape.Circle.INSTANCE, Shape.Square.INSTANCE)
                        .spread(360)
                        .position(0.0,0.0,1.0,1.0)
                        .sizes(new Size(8,50,10))
                        .timeToLive(2000).fadeOutEnabled(true).build()
        );
        vibrateAndNotify();
        ConstraintLayout dialogLayout = findViewById(R.id.completeConstraintLayout);
        View view  = LayoutInflater.from(Balance.this).inflate(R.layout.movement_dialog, dialogLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(Balance.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();


        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        sensorManager.unregisterListener(this);
        alertDialog.show();
        new Handler().postDelayed(() -> {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
                startActivity(new Intent(Balance.this, Movement.class));
            }
        }, 5000);

    }
    ;
    private void vibrateAndNotify() {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                v.vibrate(2000);
            }

            MediaPlayer mp = MediaPlayer.create(this, R.raw.achievement);
            if (mp != null) {
                mp.setOnCompletionListener(mediaPlayer -> {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
