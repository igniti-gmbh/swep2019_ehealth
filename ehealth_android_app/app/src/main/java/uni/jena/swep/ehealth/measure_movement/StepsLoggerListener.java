package uni.jena.swep.ehealth.measure_movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class StepsLoggerListener extends MovementLoggerListener {
    private int daily_offset = 0;
    private Date actual_date = new Date(System.currentTimeMillis());

    public StepsLoggerListener(){
        super(Sensor.TYPE_STEP_COUNTER);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            Toast.makeText(myContext,"Steps: "+event.values[0], Toast.LENGTH_LONG).show(); // For example
        } catch (Error e) {
        }

        if (event.values.length > 0) {
            Log.v("tracking", "New Steps: " + event.values[0]);

            StepDAO sd = db.getStepDAO();

            StepEntity se = new StepEntity();
            se.setTime(event.timestamp);

            // TODO check for reboot instead this
            // check for step counter reset

            // check for next day
            // TODO implement an other check later, this wont work if month changes and days are equal
            if (actual_date.getDay() != new Date(System.currentTimeMillis()).getDay()) {
                // reset daily offset
                daily_offset = 0;
                actual_date = new Date(System.currentTimeMillis());
            }

            StepEntity last_steps = sd.getLastStep();
            Log.v("tracking", "last step was " + last_steps.getAmount() + " in timestap " + last_steps.getTime());

            if (last_steps != null && event.values[0] < last_steps.getAmount()) {
                // rereset value
                daily_offset = last_steps.getAmount();
            }

            // set value
            se.setAmount((int) event.values[0] - daily_offset);

            // insert
            sd.insert(se);
        }
        else {
            Log.v("tracking", "Event values are empty for steplogger...");
        }
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
