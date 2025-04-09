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

    private List<ActivityIdea> activityIdeas;
    private MediaRecorder mediaRecorder;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        activityIdeas = Arrays.asList(
                new ActivityIdea("Take a deep breath and exhale into the microphone.", R.drawable.tekopp, "mic"),
                new ActivityIdea("Wave your phone in the air like you just don't care!", R.drawable.gear_icon, "motion"),
                new ActivityIdea("Up down turn around.", R.drawable.nalle, "motion"),
                new ActivityIdea("Take a walk!", R.drawable.regn, "motion")
        );

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }


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
        ActivityIdea selectedIdea = activityIdeas.get(random.nextInt(activityIdeas.size()));

        TextView textView = findViewById(R.id.textView3);
        ImageView imageView = findViewById(R.id.imageView);

        textView.setText(selectedIdea.getDescription());
        imageView.setImageResource(selectedIdea.getImageResId());

        if (selectedIdea.getType().equals("mic")) {
            startMicListening();
        } else {
            stopMicListening();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // Permission denied
        }
    }


    private void startMicListening(){
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try{
            //if any previous recorder is already running, stop it
            if(mediaRecorder != null){
                mediaRecorder.release();
            }
            //set up new recorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null"); //don't save audio
            mediaRecorder.prepare();
            mediaRecorder.start();

            //monitor volume
            new Thread(() -> {
                while(mediaRecorder != null){
                    try {
                        Thread.sleep(200); //checks every 200ms
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    int volume = mediaRecorder.getMaxAmplitude();
                    Log.d("MicDebug", "Mic volume: " + volume);
                    if(volume > 200){
                        runOnUiThread(() -> {
                            TextView micResult = findViewById(R.id.textView3);
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static class ActivityIdea {
        private String description;
        private int imageResId;
        private String type;

        public ActivityIdea(String description, int imageResId, String type) {
            this.description = description;
            this.imageResId = imageResId;
            this.type = type;
        }

        public String getDescription(){
            return description;
        }

        public int getImageResId(){
            return imageResId;
        }

        public String getType(){
            return type;
        }
    }
}
