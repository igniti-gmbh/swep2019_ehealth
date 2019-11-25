package com.example.measure_movement;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import androidx.room.Room;

import static android.content.Context.LOCATION_SERVICE;


public class LocationLoggerListener implements LocationListener {

    Context myContext;

    MovementDatabase db;

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("Location Changed");
        saveLocation(location);
        try {
            Toast.makeText(myContext, "Longitude:"+location.getLongitude()+"\nLatitude:"+location.getLatitude(), Toast.LENGTH_LONG).show(); // For example
        }catch (Error e){

        }
        printData();
    }

    private void saveLocation(Location location) {

        LocationEntity le=new LocationEntity();

        le.setLatitude(location.getLatitude());
        le.setAltitude(location.getAltitude());
        le.setLongitude(location.getLongitude());
        le.setTime(location.getTime());

        LocationDAO dao=db.getLocationDAO();
        dao.insert(le);

    }

    public void printData(){
        LocationDAO dao=db.getLocationDAO();

        System.out.println("Positions in db:");
        for(LocationEntity l:dao.getItems()){
            System.out.println(l);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("Status Changed");

    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("Provider enabled");

    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("Provider disabled");
    }


    void startListeningLocations(Context context) {
        myContext=context;
        System.out.println("starting to listen");

        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER,1000,1,this);

        db=Room.databaseBuilder(context,MovementDatabase.class,"mvmtDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    public void stopListeningLocation(Context context){
        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        mLocationManager.removeUpdates(this);
    }

    public boolean checkListening(Context context){
        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        //mLocationManager.

        return false;
    }

}
