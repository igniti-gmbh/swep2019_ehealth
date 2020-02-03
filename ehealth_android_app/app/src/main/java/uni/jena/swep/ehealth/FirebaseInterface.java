package uni.jena.swep.ehealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import uni.jena.swep.ehealth.data_visualisation.TotalStepHourly;
import uni.jena.swep.ehealth.data_visualisation.VisualDatabase;
import uni.jena.swep.ehealth.measure_movement.LocationEntity;
import uni.jena.swep.ehealth.measure_movement.StepEntity;


public class FirebaseInterface {
    private VisualDatabase db;
    private boolean is_logged_in = false;
    private FirebaseUser user = null;
    private Context app_context = null;

    // TODO check for logged out while app running

    public FirebaseInterface(Context app_context) {
        this.app_context = app_context;

        // check for internet connection
        if (this.isNetworkConnected(app_context)) {

            // init time
            AndroidThreeTen.init(app_context);

            // init database
            this.db = Room.databaseBuilder(app_context, VisualDatabase.class, "visualDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

            // init firebase
            this.loginFirebase();
        }
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
        } else {
            return "";
        }
    }

    public String getUserName() {
        if (this.is_logged_in) {
            return this.user.getDisplayName();
        } else {
            return "";
        }
    }

    public void updateDailyStepGoal() {
        // check if user is logged in
        if (this.is_logged_in) {
            // get actual date
            LocalDateTime actual = LocalDateTime.now();

            final String year_str = Integer.toString(actual.getYear());
            final String month_str = Integer.toString(actual.getMonthValue());
            final String day_str = Integer.toString(actual.getDayOfMonth());

            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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
                                            if (task.getResult().getDouble("stepgoal") != null) {
                                                double daily_step_goal = task.getResult().getDouble("stepgoal");

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

    // TODO room db could be access simultaneously and data could get lost! -> split room data into four entities
    public void updateActualRoomData() {
        // check if user is logged in
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // get actual date
            LocalDateTime ldt = LocalDateTime.now();
            final int year = ldt.getYear();
            final int month = ldt.getMonthValue();
            final int day = ldt.getDayOfMonth();
            final int hour = ldt.getHour();

            firestore.collection("users").whereEqualTo("email", this.user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getDouble("room") != null) {
                                final int roomId = document.getDouble("room").intValue();

                                firestore.collection("rooms").document(Integer.toString(roomId)).collection(Integer.toString(year)).document(Integer.toString(month)).collection(Integer.toString(day)).document(Integer.toString(hour)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.getResult().getDouble("values") != null) {
                                            List<RoomData> room_data = db.getVisualDAO().getAllRoomData();
                                            RoomData rd;

                                            if (room_data.size() > 0) {
                                                rd = room_data.get(0);
                                            } else {
                                                rd = new RoomData();
                                            }

                                            // set values
                                            if (task.getResult().getDouble("gasCurrent") != null)
                                                rd.setGas(task.getResult().getDouble("gasCurrent").doubleValue());

                                            if (task.getResult().getDouble("humidityCurrent") != null)
                                                rd.setHumidity(task.getResult().getDouble("humidityCurrent").doubleValue());

                                            if (task.getResult().getDouble("pressureCurrent") != null)
                                                rd.setPressure(task.getResult().getDouble("pressureCurrent").doubleValue());

                                            if (task.getResult().getDouble("temperatureCurrent") != null)
                                                rd.setTemp(task.getResult().getDouble("temperatureCurrent").doubleValue());

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
    public void updateDailyData() {
        // check if user is logged in
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // get actual date
            final LocalDateTime actual_date = LocalDateTime.of(LocalDate.now(), LocalTime.of(LocalTime.now().getHour(), 0));
            LocalDateTime last_date = actual_date.minusDays(1);

            // get values from passed 24 hours
            while (last_date.isBefore(actual_date) || actual_date.isEqual(last_date)) {
                final String year_str = Integer.toString(last_date.getYear());
                final String month_str = Integer.toString(last_date.getMonthValue());
                final String day_str = Integer.toString(last_date.getDayOfMonth());
                final String hour_str = Integer.toString(last_date.getHour());

                final LocalDateTime time_value = last_date;
                final long timestamp = time_value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

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
                                                    List<TotalStepHourly> steps = db.getVisualDAO().getHourlyStepsByTimestamp(timestamp);

                                                    if (steps.size() == 0) {
                                                        // insert new steps
                                                        TotalStepHourly totalStepHourly = new TotalStepHourly();
                                                        totalStepHourly.setNumber_steps(step_value);
                                                        totalStepHourly.setTimestamp(LocalDateTime.of(time_value.getYear(), time_value.getMonth(), time_value.getDayOfMonth(), time_value.getHour(), 0, 0));
                                                        db.getVisualDAO().insert(totalStepHourly);
                                                        Log.d("firebase", "Updated hour steps: " + totalStepHourly.getTimestamp());
                                                    }
                                                    else {
                                                        if (steps.get(0).getNumber_steps() < step_value) {
                                                            db.getVisualDAO().deleteTotalStepHourly(steps);
                                                            TotalStepHourly totalStepHourly = new TotalStepHourly();
                                                            totalStepHourly.setNumber_steps(step_value);
                                                            totalStepHourly.setTimestamp(LocalDateTime.of(time_value.getYear(), time_value.getMonth(), time_value.getDayOfMonth(), time_value.getHour(), 0, 0));
                                                            db.getVisualDAO().insert(totalStepHourly);
                                                        }
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

    public void uploadRoomId(final int room_id) {
        // check if user is logged in
        if (this.is_logged_in) {
            // init firestore
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("rooms").document(String.valueOf(room_id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // update room id in user if room exists
                            firestore.collection("users").document(user.getUid()).update("room", room_id);
                            Log.d("firebase", "Uploaded RoomID: " + room_id);
                        } else {
                            Log.d("firebase", "Room does not exist!");
                        }
                    } else {
                        Log.d("firebase", "Error getting documents: ", task.getException());
                    }
                }
            });
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
                            firestore.collection("users").document(document.getId()).update("stepgoal", daily_step_goal);
                            Log.d("firebase", "Uploaded StepGoal: " + daily_step_goal);
                        }
                    } else {
                        Log.d("firebase", "Error getting documents: ", task.getException());
                    }
                }
            });

            // update values in database
            this.updateDailyStepGoal();
        }
    }

    public void createDeviceDocument() {
        if (this.is_logged_in) {
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            final Map<String, Object> device_doc = new HashMap<>();
            device_doc.put("userId", this.user.getUid());

            firestore.collection("devices").add(device_doc).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d("firebase", "Created Devicedocument with ID: " + documentReference.getId());

                    // save device id in shared preferences
                    SharedPreferences sp = app_context.getSharedPreferences(app_context.getString(R.string.sp_file_name), Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString(app_context.getString(R.string.sp_device_id_key), documentReference.getId());
                    edit.apply();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("firebase", "Error adding device document", e);
                }
            });
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
                            docRef.collection("steps").add(steps_obj);
                            Log.v("firebase", "uploaded steps object: " + steps_obj.get("value") + " " + steps_obj.get("timestamp"));
                            Log.v("firebase", "written steps to device with id " + document.getId());
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

    private boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isLoggedIn() {
        return this.is_logged_in;
    }
}
