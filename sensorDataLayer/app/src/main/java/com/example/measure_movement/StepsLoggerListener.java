package com.example.measure_movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.Toast;

public class StepsLoggerListener extends MovementLoggerListener {

    StepsLoggerListener(){
        super(Sensor.TYPE_STEP_COUNTER);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            Toast.makeText(myContext,"Steps: "+event.values[0], Toast.LENGTH_LONG).show(); // For example
        }catch (Error e){
        }
        System.out.println("Steps"+event.values[0]);



        StepDAO sd=db.getStepDAO();

        StepEntity se=new StepEntity();
        se.setTime(event.timestamp);
        se.setAmount((int)event.values[0]);
        sd.insert(se);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void printData() {

        StepDAO dao=db.getStepDAO();

        System.out.println("Steps in db:");
        for(StepEntity s:dao.getItems()){
            System.out.println(s);
        }
    }
}
