package uni.jena.swep.ehealth.measure_movement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.room.Room;

import static android.content.Context.SENSOR_SERVICE;

public abstract class MovementLoggerListener implements SensorEventListener {


    MovementDatabase db;

    protected Context myContext;
    MovementLoggerListener(int sensorType){
        this.sensorType=sensorType;
    }

    public int sensorType;


    public boolean startListening(Context context){
        myContext = context;
        SensorManager sm = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        // check if sensor exists
        if (sm.getDefaultSensor(sensorType) != null) {
            // register sensor
            Sensor mySensor = sm.getDefaultSensor(sensorType);
            sm.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
            loadDB(context);

            return true;
        }
        else {
            // TODO handle sensor failed
            return false;
        }
    }

    public void loadDB(Context context) {
        db= Room.databaseBuilder(context,MovementDatabase.class,"mvmtDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    public void stopListening(Context context){
        SensorManager sm=(SensorManager)context.getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);
    }

    public abstract void printData();
}
