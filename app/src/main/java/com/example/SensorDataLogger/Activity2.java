package com.example.SensorDataLogger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Activity2 extends AppCompatActivity implements SensorEventListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "SensorLog";

    private Sensor accelSensor, gyroSensor;
    private SensorManager sensorManager;
    private ToggleButton recordtb;
    private Button actionb1;
    private Button actionb2;
    private Button actionb3;
    private Button actionb4;
    private Chronometer chronometer;

    private Boolean accelData = false;
    private Boolean dataCollected = false;
    private Boolean isRecording = false;
    private FileWriter writer;
    private String cached_data;
    private int action1State = 0;
    private int action2State = 0;
    private int action3State = 0;
    private int action4State = 0;
    private long startTime = 0;
    private long recordTime;

    private String fileName = "jason";
    private long recordLimit = 8;
    private String label1 = "DU";
    private String label2 = "UD";
    private String label3 = "circle";
    private String label4 = "shake";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        recordtb = (ToggleButton) findViewById(R.id.recordToggle);
            recordtb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                }
                else {
                    chronometer.stop();
                }
            }
        });
        actionb1 = (Button) findViewById(R.id.actionButton1);
        actionb1.setText(label1);
        actionb1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Override onTouch listener for the button to register if button is pressed or not
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    action1State = 1;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action1State = 0;
                }
                return true;
            }
        });
        actionb2 = (Button) findViewById(R.id.actionButton2);
        actionb2.setText(label2);
        actionb2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Override onTouch listener for the button to register if button is pressed or not
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    action2State = 1;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action2State = 0;
                }
                return true;
            }
        });
        actionb3 = (Button) findViewById(R.id.actionButton3);
        actionb3.setText(label3);
        actionb3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Override onTouch listener for the button to register if button is pressed or not
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    action3State = 1;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action3State = 0;
                }
                return true;
            }
        });
        actionb4 = (Button) findViewById(R.id.actionButton4);
        actionb4.setText(label4);
        actionb4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Override onTouch listener for the button to register if button is pressed or not
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    action4State = 1;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action4State = 0;
                }
                return true;
            }
        });
        chronometer = findViewById(R.id.chronometer);
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometerChanged) {
                chronometer = chronometerChanged;
            }
        });

            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission is granted");
        }
            else {
            Log.d(TAG, "No permission is granted, attempting to request permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        }
    }

    public void changeScreen(View view) {
        Intent intent = new Intent(this, Activity1.class);
        startActivity(intent);
    }

    public void writeCSV(String data, String pass, String error) {
        try {
            writer.write(data);
            Log.d(TAG, pass);
        } catch (IOException e) {
            Log.d(TAG, error);
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        //update record time
        if (startTime != 0){
            recordTime = System.currentTimeMillis()/1000 - startTime;
        }

        // when user begins recording
        if (!isRecording && recordtb.isChecked()){
            startTime = System.currentTimeMillis() / 1000;
            Log.d(TAG, "start timestamp: " + startTime);
            isRecording = true;
            try {
                writer = new FileWriter(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName + "_" + System.currentTimeMillis()/1000 + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeCSV("acc_x, acc_y, acc_z, gyro_x, gyro_y, gyro_z" + "," + label1 + ","+ label2 + "," + label3 + ","+ label4 + "\n","Successfully created writer object","Error in creating writer object");
        }

        else if (isRecording && !recordtb.isChecked() || recordTime > recordLimit) {
            Log.d(TAG, "end timestamp: " + System.currentTimeMillis()/1000);
            try {
                writer.close();
                Log.d(TAG,"Successfully closed writer object");
            } catch (IOException e) {
                Log.d(TAG, "Error in closing writer object");
                e.printStackTrace();
            }
            isRecording = false;
            recordtb.setChecked(false);
            recordTime = 0;
            startTime = 0;
        }

        if (recordtb.isChecked()) {

            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && !accelData) {
                cached_data = (event.values[0] + "," + event.values[1] + "," + event.values[2]);
                accelData = true;

            } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE && accelData) {
                cached_data += ("," + event.values[0] + "," + event.values[1] + "," + event.values[2]);
                dataCollected = true;
            }

            if (dataCollected) {
                writeCSV(cached_data + "," + action1State + "," + action2State + "," + action3State + "," + action4State + "\n","","Error in generating data row in csv file");
                accelData = false;
                dataCollected = false;
            }
        }
    }
}
