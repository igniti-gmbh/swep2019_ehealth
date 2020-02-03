package uni.jena.swep.ehealth.measure_movement;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import uni.jena.swep.ehealth.MainActivity;
import uni.jena.swep.ehealth.R;

public class TrackingService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    StepsLoggerListener stepList = new StepsLoggerListener();
    AcceleratorStepsLoggerListener accStepList = new AcceleratorStepsLoggerListener();

    public TrackingService() {
    }

    public TrackingService(Context applicationContext) {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("eHealth Trackerservice", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.v("backgroundservice", "starting backgroundservice");

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Tracking user activity...")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        // check for available sensors for tracking steps
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            // start step counter sensor
            stepList.startListening(getApplicationContext());
        }
        else if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // start accelerator step counter sensor
            accStepList.startListening(getApplicationContext());
        }
        else {
            // TODO handle no sensor
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("backgroundservice", "closing backgroundservice");

        // stop listeners
        accStepList.stopListening(getApplicationContext());
        stepList.stopListening(getApplicationContext());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
