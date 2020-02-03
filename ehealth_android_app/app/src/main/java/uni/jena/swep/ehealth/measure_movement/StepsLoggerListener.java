package uni.jena.swep.ehealth.measure_movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.Toast;

import java.util.List;

public class StepsLoggerListener extends MovementLoggerListener {

    public StepsLoggerListener() {
        super(Sensor.TYPE_STEP_COUNTER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values.length > 0) {
            // create new step entity
            StepEntity se = new StepEntity();
            se.setTime(event.timestamp);

            // check the last step entity from database to remove counter offset
            List<LastStepEntity> last_steps = db.getStepDAO().getLastStepOffset();
            LastStepEntity last_step_offset = null;
            int offset = 0;

            if (last_steps.size() > 0) {
                // set the actual offset
                offset = last_steps.get(0).getStep_offset();

                // check if reboot happend
                if ((int) event.values[0] <= offset) {
                    // reset offset
                    offset = 0;
                }
            } else {
                // create new step offset
                last_step_offset = new LastStepEntity();
            }

            // insert updated step offset
            last_step_offset.setStep_offset((int) event.values[0]);
            db.getStepDAO().insert(last_step_offset);

            // insert actual done steps into database
            se.setAmount((int) event.values[0] - offset);
            db.getStepDAO().insert(se);

            try {
                Toast.makeText(myContext, "Steps insert: " + se.getAmount(), Toast.LENGTH_LONG).show();
            } catch (Error e) {
            }
        } else {
            try {
                Toast.makeText(myContext, "No steps value in event!", Toast.LENGTH_LONG).show();
            } catch (Error e) {
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void printData() {
        StepDAO dao = db.getStepDAO();

        System.out.println("Steps in db:");
        for (StepEntity s : dao.getItems()) {
            System.out.println(s);
        }
    }
}
