package com.example.measure_movement;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.widget.Toast;

public class LocationLoggerService extends BroadcastReceiver {

    static int sendInterval = 1000*60*10;

    LocationLoggerListener posList=new LocationLoggerListener();

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("hello");
        /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakelock");
        wl.acquire();*/

        // Put here YOUR code.
        Toast.makeText(context, "Time passed", Toast.LENGTH_LONG).show(); // For example


        //wl.release();
        setAlarm(context);
    }



    public void setAlarm(Context context)
    {
        System.out.println("setting alarm");

        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LocationLoggerService.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ sendInterval, pi); // Millisec * Second * Minute


        posList.startListeningLocations(context);

    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, LocationLoggerService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);


        posList.stopListeningLocation(context);
    }

    public boolean checkAlarm(Context context){
        Intent intent = new Intent(context, LocationLoggerService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);

        //AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return sender!=null;
    }


}
