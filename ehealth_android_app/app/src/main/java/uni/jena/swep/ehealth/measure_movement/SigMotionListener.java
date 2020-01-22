package uni.jena.swep.ehealth.measure_movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import android.widget.Toast;

public class SigMotionListener extends MovementLoggerListener {


    public SigMotionListener(){
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

        Log.v("tracking", "SigMotion: " + event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
