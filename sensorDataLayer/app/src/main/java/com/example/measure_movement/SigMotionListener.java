package com.example.measure_movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.widget.Toast;

public class SigMotionListener extends MovementLoggerListener {


    SigMotionListener(){
        super(Sensor.TYPE_SIGNIFICANT_MOTION);
    }

    @Override
    public void printData() {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        try {
            Toast.makeText(myContext, " yeet", Toast.LENGTH_LONG).show(); // For example
        }catch (Error e){

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
