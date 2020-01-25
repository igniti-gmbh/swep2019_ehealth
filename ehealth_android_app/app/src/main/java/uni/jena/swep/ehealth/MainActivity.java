package uni.jena.swep.ehealth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import uni.jena.swep.ehealth.measure_movement.LocationLoggerListener;
import uni.jena.swep.ehealth.measure_movement.MovementDatabase;
import uni.jena.swep.ehealth.measure_movement.SigMotionListener;
import uni.jena.swep.ehealth.measure_movement.StepEntity;
import uni.jena.swep.ehealth.measure_movement.StepsLoggerListener;

public class MainActivity extends AppCompatActivity {
    private MovementDatabase steps_db;
    private FirebaseAuth auth;

    private Toolbar main_toolbar;
    private AppBarConfiguration mAppBarConfiguration;

    LocationLoggerListener locList = new LocationLoggerListener();
    StepsLoggerListener stepList = new StepsLoggerListener();
    SigMotionListener sigList = new SigMotionListener();

    // TODO save values in shared preferences
    private boolean step_tracking = true;
    private boolean motion_tracking = true;
    private boolean location_tracking = true;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // check if user is logged in already
        if (auth.getCurrentUser() == null) {
            // switch to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show();
        }

        // setup toolbar
        main_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(main_toolbar);

        // create drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // set username and email if logged in
        // TODO name isn't displayed, get name attribute from firebase?
        if (auth.getCurrentUser() != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView nav_user = (TextView) headerView.findViewById(R.id.drawer_username);
            TextView nav_email = (TextView) headerView.findViewById(R.id.drawer_email);
            nav_user.setText(auth.getCurrentUser().getDisplayName());
            nav_email.setText(auth.getCurrentUser().getEmail());
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_movement_data, R.id.nav_share, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // check if device id is assigned
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sp_file_name), Context.MODE_PRIVATE);
        if (sharedPref.getString(getString(R.string.sp_device_id_key), null) == null && auth.getCurrentUser() != null) {
            Log.v("deviceid", "No DeviceID found, generating a new one...");
            // generate new device id and set it in shared preferences
            FirebaseInterface firebaseInterface = new FirebaseInterface(this);
            firebaseInterface.createDeviceDocument();
        }


        // create some random sensor test data
        // TODO remove later, after debugging
        if (false) {
            this.createTestData();
        }

        // init user tracking
        // TODO let user decide which tracking modes should be activated
        this.updateSensors();

        // create work manager for uploading data
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        // TODO change interval or trigger uploadworker in motion event?
        PeriodicWorkRequest uploadWorkRequest = new PeriodicWorkRequest.Builder(UploadWorker.class, 5, TimeUnit.MINUTES).setConstraints(constraints).build();

        // start work manager for uploading data
        WorkManager.getInstance(this).enqueue(uploadWorkRequest);
    }

    private void updateSensors() {
        // TODO handle not found sensors, move this in external method
        boolean sensor_motion_active = this.sigList.startListening(MainActivity.this);
        this.locList.startListeningLocations(MainActivity.this);
        boolean sensor_steps_active = this.stepList.startListening(MainActivity.this);

        Log.v("sensors", "step sensor: " + sensor_steps_active);
        Log.v("sensors", "motion sensor: " + sensor_motion_active);
    }

    private void createSwitchListener() {
        // TODO create switches in settings fragment
        /*
        Switch sw_location = (Switch) findViewById(R.id.switch_location_tracking);
        Switch sw_motion = (Switch) findViewById(R.id.switch_motion_sensor);
        Switch sw_step = (Switch) findViewById(R.id.switch_step_tracking);

        // set default values
        // TODO read them later from shared preferences
        sw_location.setChecked(location_tracking);
        sw_motion.setChecked(motion_tracking);
        sw_step.setChecked(step_tracking);

        sw_location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // change value in settings
                location_tracking = isChecked;

                // update trackers
                updateSensors();
            }
        });

        sw_motion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // change value in settings
                motion_tracking = isChecked;

                // update trackers
                updateSensors();
            }
        });

        sw_step.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // change value in settings
                step_tracking = isChecked;

                // update trackers
                updateSensors();
            }
        });

         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // check if settings was selected
        if (id == R.id.action_logout) {
            // logout from firebase and switch to login activity
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void createTestData() {
        steps_db = Room.databaseBuilder(this, MovementDatabase.class, "mvmtDB").allowMainThreadQueries().build();

        Random r = new Random();
        int min1 = r.nextInt(50);
        int min2 = r.nextInt(100 - min1) + min1;
        int min3 = r.nextInt(150 - min2) + min2;
        int step_sum = 0;

        int millis_in_one_day = 1000 * 60 * 60 * 24;
        int millis_per_step = millis_in_one_day / min3;
        LocalDateTime actual = LocalDateTime.now();
        Log.v("testdata", "StartInterval: " + LocalDateTime.now().minusDays(1));
        Log.v("testdata", "EndInterval: " + LocalDateTime.now());
        Log.v("testdata", "Min1: " + min1);
        Log.v("testdata", "Min2: " + min2);
        long begin_day = LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        for (int i = 0; i < min1; i++) {
            StepEntity step = new StepEntity();
            int new_step_count = r.nextInt(150 - 50) + 50;
            step_sum += new_step_count;
            step.setAmount(new_step_count);
            step.setIs_synchronized(false);
            step.setTime(begin_day + millis_per_step * i);
            steps_db.getStepDAO().insert(step);
        }

        for (int i = min1; i < min2; i++) {
            StepEntity step = new StepEntity();
            int new_step_count = r.nextInt(250 - 50) + 50;
            step_sum += new_step_count;
            step.setAmount(new_step_count);
            step.setIs_synchronized(false);
            step.setTime(begin_day + millis_per_step * i);
            steps_db.getStepDAO().insert(step);
        }

        for (int i = min2; i < min3; i++) {
            StepEntity step = new StepEntity();
            int new_step_count = r.nextInt(100 - 50) + 50;
            step_sum += new_step_count;
            step.setAmount(new_step_count);
            step.setIs_synchronized(false);
            step.setTime(begin_day + millis_per_step * i);
            steps_db.getStepDAO().insert(step);
        }
        Log.v("testdata", "Total number steps: " + step_sum);
        Log.v("testdata", "Total entries: : " + min3);
    }

}
