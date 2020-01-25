package uni.jena.swep.ehealth;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import uni.jena.swep.ehealth.data_visualisation.VisualDatabase;
import uni.jena.swep.ehealth.measure_movement.LocationEntity;
import uni.jena.swep.ehealth.measure_movement.MovementDatabase;
import uni.jena.swep.ehealth.measure_movement.StepEntity;


public class UploadWorker extends Worker {
    public UploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        Log.v("upworker", "Upload Worker started!");

        // create movement database instance
        MovementDatabase db = Room.databaseBuilder(getApplicationContext(), MovementDatabase.class, "mvmtDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        VisualDatabase vdb = Room.databaseBuilder(getApplicationContext(), VisualDatabase.class, "visualDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        FirebaseInterface fireinterface = new FirebaseInterface(getApplicationContext());

        if (fireinterface.isLoggedIn()) {
            // read database and check if not uploaded sensor data exists
            List<StepEntity> not_synchronized_steps = db.getStepDAO().getSynchronizedSteps(false);
            List<LocationEntity> not_synchronized_locations = db.getLocationDAO().getSynchronizedLocations(false);

            // iterate through locations and upload them
            for (LocationEntity location : not_synchronized_locations) {
                // TODO upload data
                // fireinterface.uploadLocation(location);

                // set data as uploaded
                location.setIs_synchronized(true);
                db.getLocationDAO().update(location); // TODO update the whole list at once?
            }

            // iterate through steps and upload them
            for (StepEntity step : not_synchronized_steps) {
                // upload steps
                fireinterface.uploadSteps(step);

                // set data as uploaded
                step.setIs_synchronized(true);
                db.getStepDAO().update(step); // TODO update the whole list at once?
            }
        }
        else {
            Log.d("firebase", "No user logged in! Can't connect to firebase");
        }

        // delete uploaded data
        db.getStepDAO().deleteMultiple(db.getStepDAO().getSynchronizedSteps(true));
        db.getLocationDAO().deleteMultiple(db.getLocationDAO().getSynchronizedLocations(true));

        // TODO catch exception for failed db access and return Result.failed()
        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
