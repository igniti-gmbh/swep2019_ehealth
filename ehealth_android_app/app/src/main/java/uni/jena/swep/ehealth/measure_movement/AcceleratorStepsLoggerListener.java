package uni.jena.swep.ehealth.measure_movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;


public class AcceleratorStepsLoggerListener extends MovementLoggerListener {

    private static final int ABOVE = 1;
    private static final int BELOW = 0;
    private static int CURRENT_STATE = 0;
    private static int PREVIOUS_STATE = BELOW;
    private long streakStartTime;
    private long streakPrevTime;
    private float[] prev = {0f,0f,0f};

    public AcceleratorStepsLoggerListener() {
        super(Sensor.TYPE_ACCELEROMETER);
        streakPrevTime = System.currentTimeMillis() - 500;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // handle sensor event
        handleEvent(event);
    }

    private float[] lowPassFilter(float[] input, float[] prev) {
        float ALPHA = 0.1f;

        if(input == null || prev == null) {
            return null;
        }

        for (int i = 0; i < input.length; i++) {
            prev[i] = prev[i] + ALPHA * (input[i] - prev[i]);
        }

        return prev;
    }

    private void handleEvent(SensorEvent event) {
        // low pass filter event value
        prev = lowPassFilter(event.values,prev);
        Accelerometer data = new Accelerometer(prev);

        // check if step was done
        if(data.R > 10.5f){
            CURRENT_STATE = ABOVE;

            if(PREVIOUS_STATE != CURRENT_STATE) {
                streakStartTime = System.currentTimeMillis();

                if ((streakStartTime - streakPrevTime) <= 250f) {
                    streakPrevTime = System.currentTimeMillis();
                    return;
                }

                streakPrevTime = streakStartTime;
                updateSteps();
            }

            PREVIOUS_STATE = CURRENT_STATE;
        }
        else if(data.R < 10.5f) {
            CURRENT_STATE = BELOW;
            PREVIOUS_STATE = CURRENT_STATE;
        }
    }

    // TODO reduce db access, count steps and insert if some time is passed
    private void updateSteps() {
        // create new step entity
        StepEntity se = new StepEntity();

        // set timestamp
        se.setTime(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        // insert actual done steps into database
        se.setAmount(1);
        db.getStepDAO().insert(se);

        // Toast.makeText(myContext, "Insert Steps: " + se.getAmount(), Toast.LENGTH_LONG).show();
        Log.v("tracking", "Step occured!");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void printData() {

    }
}
