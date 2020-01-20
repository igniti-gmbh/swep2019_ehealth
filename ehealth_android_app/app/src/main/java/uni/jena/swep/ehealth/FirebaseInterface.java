package uni.jena.swep.ehealth;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uni.jena.swep.ehealth.data_visualisation.RoomData;
import uni.jena.swep.ehealth.data_visualisation.StepGoal;
import uni.jena.swep.ehealth.data_visualisation.TotalStepDaily;
import uni.jena.swep.ehealth.data_visualisation.TotalStepHourly;
import uni.jena.swep.ehealth.data_visualisation.VisualDatabase;
import uni.jena.swep.ehealth.measure_movement.LocationEntity;
import uni.jena.swep.ehealth.measure_movement.StepEntity;


public class FirebaseInterface {
    private VisualDatabase db;
    private boolean is_logged_in = false;
    private FirebaseUser user = null;

    // TODO check for logged out while app running

    public FirebaseInterface(Context app_context) {
        // init time
        AndroidThreeTen.init(app_context);

        // init database
        this.db = Room.databaseBuilder(app_context, VisualDatabase.class, "visualDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        // init firebase
        this.loginFirebase();
    }

    private void loginFirebase() {
        // init firebase auth
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // log in
        this.user = auth.getCurrentUser();

        // check if user logged in
        if (user != null) {
            // set as logged in
            this.is_logged_in = true;
        } else {
            this.is_logged_in = false;
        }
    }

    public String getUserEmail() {
        if (this.is_logged_in) {
            return this.user.getEmail();
        }
        else {
            return "";
        }
    }

    public String getUserName() {
        if (this.is_logged_in) {
            return this.user.getDisplayName();
        }
        else {
            return "";
        }
    }

    public void updateDailyStepsTotalAndGoal() {
        // check if user is logged in
        if (this.is_logged_in) {
            // get actual date
            LocalDateTime actual = LocalDateTime.now();

            final String year_str = Integer.toString(actual.getYear());
            final String month_str = Integer.toString(actual.getMonthValue());
            final String day_str = Integer.toString(actual.getDayOfMonth());

            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // get daily steps
            // TODO use uid instead of email
            firestore.collection("users").whereEqualTo("email", this.user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            firestore.collection("users").document(document.getId()).collection(year_str).document(month_str).collection(day_str).document("totalSteps").get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            // get value from firestore
                                            if (task.getResult().getDouble("value") != null) {
                                                int step_value = task.getResult().getDouble("value").intValue();

                                                // get actual value from local database
                                                List<TotalStepDaily> steps = db.getVisualDAO().getActualDailySteps();

                                                // set actual value
                                                TotalStepDaily step;
                                                if (steps.size() > 0) {
                                                    step = steps.get(steps.size() - 1);
                                                    // TODO check for steps from the actual day
                                                } else {
                                                    step = new TotalStepDaily();
                                                }
                                                step.setTimestamp(LocalDate.now());
                                                step.setNumber_steps(step_value);

                                                // insert in local database
                                                db.getVisualDAO().insert(step);

                                                Log.d("firebase", "Updated totalSteps in local database: " + step.getNumber_steps());
                                            } else {
                                                Log.v("firebase", "totalSteps daily is null");
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.d("firebase", "Error getting documents: ", task.getException());
                    }
                }
            });

            // get step goal
            // TODO use uid instead of email
            firestore.collection("users").
                    whereEqualTo("email", this.user.getEmail()).
                    get().
                    addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    firestore.collection("users").document(document.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            // get step goal value from firebase
                                            if (task.getResult().getDouble("daily_step_goal") != null) {
                                                double daily_step_goal = task.getResult().getDouble("daily_step_goal");

                                                // get value from local database
                                                List<StepGoal> step_goals = db.getVisualDAO().getAllStepGoals();
                                                StepGoal step_goal;

                                                if (step_goals.size() > 0) {
                                                    step_goal = step_goals.get(0);
                                                } else {
                                                    step_goal = new StepGoal();
                                                }

                                                // update and insert step goal
                                                step_goal.setNumber_steps((int) daily_step_goal);
                                                db.getVisualDAO().insert(step_goal);

                                                Log.d("firebase", "Updated STepGoal in local database: " + step_goal.getNumber_steps());
                                            } else {
                                                Log.v("firebase", "Daily step goal is null");
                                            }
                                        }
                                    });
                                }
                            } else {
                                Log.d("firebase", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

    }

    // TODO room db could be access simultaniously and data could get lost! -> split room data into four entities
    public void updateRoomData() {
        // check if user is logged in
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("users").whereEqualTo("email", this.user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getDouble("room") != null) {
                                final int roomId = document.getDouble("room").intValue();

                                firestore.collection("rooms").document(Integer.toString(roomId)).collection("gas").document("gasvalue").get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.getResult().getDouble("value") != null) {
                                                    List<RoomData> room_data = db.getVisualDAO().getAllRoomData();
                                                    RoomData rd;

                                                    if (room_data.size() > 0) {
                                                        rd = room_data.get(0);
                                                        rd.setGas(task.getResult().getDouble("value").intValue());
                                                    }
                                                    else {
                                                        rd = new RoomData();
                                                        rd.setGas(task.getResult().getDouble("value").intValue());
                                                    }

                                                    db.getVisualDAO().insert(rd);
                                                }
                                            }
                                        });

                                firestore.collection("rooms").document(Integer.toString(roomId)).collection("pressure").document("pressurevalue").get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.getResult().getDouble("value") != null) {
                                                    List<RoomData> room_data = db.getVisualDAO().getAllRoomData();
                                                    RoomData rd;

                                                    if (room_data.size() > 0) {
                                                        rd = room_data.get(0);
                                                        rd.setPressure(task.getResult().getDouble("value").intValue());
                                                    }
                                                    else {
                                                        rd = new RoomData();
                                                        rd.setPressure(task.getResult().getDouble("value").intValue());
                                                    }

                                                    db.getVisualDAO().insert(rd);
                                                }
                                            }
                                        });

                                firestore.collection("rooms").document(Integer.toString(roomId)).collection("temp").document("tempvalue").get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.getResult().getDouble("value") != null) {
                                                    List<RoomData> room_data = db.getVisualDAO().getAllRoomData();
                                                    RoomData rd;

                                                    if (room_data.size() > 0) {
                                                        rd = room_data.get(0);
                                                        rd.setTemp(task.getResult().getDouble("value").intValue());
                                                    }
                                                    else {
                                                        rd = new RoomData();
                                                        rd.setTemp(task.getResult().getDouble("value").intValue());
                                                    }

                                                    db.getVisualDAO().insert(rd);
                                                }
                                            }
                                        });

                                firestore.collection("rooms").document(Integer.toString(roomId)).collection("humidity").document("humidityvalue").get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.getResult().getDouble("value") != null) {
                                                    List<RoomData> room_data = db.getVisualDAO().getAllRoomData();
                                                    RoomData rd;

                                                    if (room_data.size() > 0) {
                                                        rd = room_data.get(0);
                                                        rd.setHumidity(task.getResult().getDouble("value").intValue());
                                                    }
                                                    else {
                                                        rd = new RoomData();
                                                        rd.setHumidity(task.getResult().getDouble("value").intValue());
                                                    }

                                                    db.getVisualDAO().insert(rd);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                }
            });
        }
    }

    // TODO update other days too here?
    public void updateDailySteps() {
        // check if user is logged in
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // get actual date
            final LocalDateTime actual_date = LocalDateTime.of(LocalDate.now(), LocalTime.of(LocalTime.now().getHour(), 0));
            LocalDateTime last_date = actual_date.minusDays(1);

            // get datebase values
            // TODO use new query later and remove processing all values in database
            //final List<TotalStepHourly> actual_steps = this.db.getVisualDAO().getHourlyStepsFromDay(actual_date.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond());
            //actual_steps.addAll(this.db.getVisualDAO().getHourlyStepsFromDay(last_date.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond()));
            List<TotalStepHourly> actual_steps_all = this.db.getVisualDAO().getAllTotalStepsHourly();
            List<TotalStepHourly> actual_steps = new ArrayList<TotalStepHourly>();

            for (TotalStepHourly steps : actual_steps_all) {
                if (steps.getTimestamp().isBefore(actual_date) && steps.getTimestamp().isAfter(last_date)) {
                    actual_steps.add(steps);
                }
            }

            final List<TotalStepHourly> hourly_steps = actual_steps;


            Log.v("firebase", "Number hourly steps in db (passed 24h): " + actual_steps.size());

            // get passed 24 hours
            while (last_date.isBefore(actual_date) || actual_date.isEqual(last_date)) {
                final String year_str = Integer.toString(last_date.getYear());
                final String month_str = Integer.toString(last_date.getMonthValue());
                final String day_str = Integer.toString(last_date.getDayOfMonth());
                final String hour_str = Integer.toString(last_date.getHour());

                final LocalDateTime time_value = last_date;

                // get steps for actual timestamp
                firestore.collection("users").whereEqualTo("email", this.user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                firestore.collection("users").document(document.getId()).collection(year_str).document(month_str).collection(day_str).document(hour_str).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                // get value from firestore
                                                if (task.getResult().getDouble("value") != null) {
                                                    int step_value = task.getResult().getDouble("value").intValue();

                                                    // check if value is in database actual
                                                    // TODO use better check, are dates compareable (all have minutes&seconds equal zero)?
                                                    boolean value_present = false;
                                                    for (int i = 0; i < hourly_steps.size() && value_present == false; i++) {
                                                        if (hourly_steps.get(i).getTimestamp().isEqual(time_value)) {
                                                            value_present = true;
                                                        }
                                                    }

                                                    // insert steps if they aren't present yet
                                                    if (value_present == false) {
                                                        TotalStepHourly totalStepHourly = new TotalStepHourly();
                                                        totalStepHourly.setNumber_steps(step_value);
                                                        totalStepHourly.setTimestamp(LocalDateTime.of(time_value.getYear(), time_value.getMonth(), time_value.getDayOfMonth(), time_value.getHour(), 0, 0));
                                                        db.getVisualDAO().insert(totalStepHourly);
                                                        Log.d("firebase", "Updated hour steps: " + totalStepHourly.getTimestamp());
                                                    } else {
                                                        Log.v("firebase", "HourlySteps are uptodate in db");
                                                    }


                                                } else {
                                                    Log.v("firebase", "hour steps is null");
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d("firebase", "Error getting documents: ", task.getException());
                        }
                    }
                });

                // increment time counter
                last_date = last_date.plusHours(1);
            }
        }
    }

    public void uploadStepGoal(final int daily_step_goal) {
        // check if user is logged in
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // TODO use uid instead of email
            firestore.collection("users").whereEqualTo("email", this.user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // update value in firebase
                            firestore.collection("users").document(document.getId()).update("daily_step_goal", daily_step_goal);
                            Log.d("firebase", "Uploaded StepGoal: " + daily_step_goal);
                        }
                    } else {
                        Log.d("firebase", "Error getting documents: ", task.getException());
                    }
                }
            });

            // update values in database
            this.updateDailyStepsTotalAndGoal();
        }
    }

    public void uploadSteps(StepEntity steps) {
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // create document for firebase
            final Map<String, Object> steps_obj = new HashMap<>();
            steps_obj.put("timestamp", new Timestamp(new Date(steps.getTime())));
            steps_obj.put("value", steps.getAmount());

            // write steps into firestore
            firestore.collection("devices").whereEqualTo("userId", this.user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DocumentReference docRef = firestore.collection("devices").document(document.getId());
                            // TODO uncomment code later
                            docRef.collection("steps").add(steps_obj);
                            Log.v("firebase", "uploaded steps object");
                        }
                    } else {
                        Log.d("firebase", "Error getting documents: ", task.getException());
                    }
                }
            });
        }
    }

    public void uploadLocation(LocationEntity location) {
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // create document for firebase
            Map<String, Object> location_obj = new HashMap<>();
            location_obj.put("latitude", location.getLatitude());
            location_obj.put("longitude", location.getLongitude());
            location_obj.put("altitude", location.getAltitude());
            location_obj.put("timestamp", new Timestamp(new Date(location.getTime())));

            // write location into firestore
            firestore.collection("devices").whereEqualTo("userId", this.user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DocumentReference docRef = firestore.collection("devices").document(document.getId());
                            // TODO uncomment code later
                            // docRef.collection("locations").add(location_obj);
                            // Log.v("firebase", "uploaded location object");
                        }
                    } else {
                        Log.d("firebase", "Error getting documents: ", task.getException());
                    }
                }
            });
        }
    }

    public List<StepEntity> getStepsOnDay(int year, int month, int day) {
        List<StepEntity> steps = new ArrayList<StepEntity>();

        // check if user is logged in
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            final String year_str = Integer.toString(year);
            final String month_str = Integer.toString(month);
            final String day_str = Integer.toString(day);

            // TODO use uid instead of email to identify user
            firestore.collection("users").whereEqualTo("email", this.user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.v("firestore", "found user document!");
                            firestore.collection("users").document(document.getId()).collection(year_str).document(month_str).collection(day_str).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    if (document.getId().equals("totalSteps")) {
                                                        String total_steps = document.getId();
                                                        int step_value = document.getDouble("value").intValue();
                                                        Log.v("firebase", "Steps From Firebase Total: " + total_steps + " => " + step_value);
                                                    } else {
                                                        int hour = Integer.valueOf(document.getId());
                                                        int step_value = document.getDouble("value").intValue();
                                                        Log.v("firebase", "Steps From Firebase: " + hour + " => " + step_value);
                                                    }

                                                    // TODO insert data into local database
                                                }
                                            } else {
                                                Log.d("firebase", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.d("firebase", "Error getting documents: ", task.getException());
                    }
                }
            });
        }

        return steps;
    }

    public boolean isLoggedIn() {
        return this.is_logged_in;
    }
}
