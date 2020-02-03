package uni.jena.swep.ehealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

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
        // check for user logged in
        FirebaseInterface fireinterface = new FirebaseInterface(getApplicationContext());

        if (fireinterface.isLoggedIn()) {
            // check for past upload
            boolean do_upload = true;
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.upload_date), Context.MODE_PRIVATE);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            if (sharedPref.getString(getApplicationContext().getString(R.string.upload_date), null) != null) {
                // get last upload date
                LocalDateTime past_upload = LocalDateTime.parse(sharedPref.getString(getApplicationContext().getString(R.string.upload_date), null), formatter);
                Log.v("upworker", "saved uploaddate: " + past_upload.toString());
                Log.v("upworker", "next upload at: " + past_upload.plusMinutes(5));

                // don't upload data if last upload is less than 5 minutes
                if (LocalDateTime.now().isBefore(past_upload.plusMinutes(5))) {
                    do_upload = false;
                }
            }

            if (do_upload) {
                Log.v("upworker", "Upload Worker started!");

                // create movement database instance
                MovementDatabase db = Room.databaseBuilder(getApplicationContext(), MovementDatabase.class, "mvmtDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
                VisualDatabase vdb = Room.databaseBuilder(getApplicationContext(), VisualDatabase.class, "visualDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

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
                LocalDateTime date_counter = null;
                StepEntity actual_step = null;
                for (StepEntity step : not_synchronized_steps) {
                    if (date_counter == null) {
                        date_counter = LocalDateTime.ofInstant(Instant.ofEpochMilli(step.getTime()), ZoneId.systemDefault());
                        Log.v("upworker", "date_counter: " + date_counter);
                        actual_step = step;
                    }
                    else {
                        LocalDateTime step_date = LocalDateTime.ofInstant(Instant.ofEpochMilli(step.getTime()), ZoneId.systemDefault());

                        if (LocalDateTime.of(step_date.toLocalDate(), LocalTime.of(step_date.getHour(), 0)).isEqual(LocalDateTime.of(date_counter.toLocalDate(), LocalTime.of(date_counter.getHour(), 0)))) {
                            actual_step.setAmount(actual_step.getAmount() + step.getAmount());
                            Log.v("upworker", "added steps from " + step_date);
                        } else {
                            // upload steps
                            Log.v("upworker", "uploading steps: " + actual_step.getAmount());
                            fireinterface.uploadSteps(actual_step);

                            date_counter = null;
                        }
                    }
                    // set data as uploaded
                    step.setIs_synchronized(true);
                    db.getStepDAO().update(step); // TODO update the whole list at once?
                }

                if (date_counter != null) {
                    // upload steps
                    Log.v("upworker", "uploading steps: " + actual_step.getAmount());
                    fireinterface.uploadSteps(actual_step);
                }

                // delete uploaded data
                db.getStepDAO().deleteMultiple(db.getStepDAO().getSynchronizedSteps(true));
                db.getLocationDAO().deleteMultiple(db.getLocationDAO().getSynchronizedLocations(true));

                // update upload date in shared preferences
                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putString(getApplicationContext().getString(R.string.upload_date), formatter.format(LocalDateTime.now()));
                edit.apply();
            }
        }

        // TODO catch exception for failed db access and return Result.failed()
        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
