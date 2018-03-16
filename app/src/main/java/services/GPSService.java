package services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import database.DatabaseHelperSensor;
import utility.TraceSensor;

public class GPSService extends Service implements LocationListener {

    private DatabaseHelperSensor _dbHelper = null;
    private LocationManager locationManager;

    private final Binder _binder = new GPSBinder();

    private final String TAG = "GPS Service";
    private AtomicBoolean _isRunning = new AtomicBoolean(false);

    private double latitude = 0.0;
    private double longitude = 0.0;
    private double speed = 0.0;
    private long time = 0;
    private long starttime = 0;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return _binder;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location update speed:" + String.valueOf(location.getSpeed()));
        // TODO Auto-generated method stub
        if (location != null && _dbHelper != null) {
            time = System.currentTimeMillis();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            speed = location.getSpeed();
            TraceSensor tmp = new TraceSensor(3);
            tmp.type = TraceSensor.GPS;
            tmp.time = time - starttime;
            tmp.values[0] = latitude;
            tmp.values[1] = longitude;
            tmp.values[2] = speed;
            sendTrace(tmp);
            _dbHelper.addGPSData(time, latitude, longitude, speed);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public class GPSBinder extends Binder {
        public void setDatabaseHelper(DatabaseHelperSensor dbhelper) {
            _dbHelper = dbhelper;
            starttime = _dbHelper.getStarttime();
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLontitude() {
            return longitude;
        }

        public double getSpeed() {
            return speed;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG, "stop service");
        locationManager.removeUpdates(this);
        _dbHelper = null;
        _isRunning.set(false);
        starttime = 0;
        stopSelf();
    }

    private void startService() {
        Log.d(TAG, "start service");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        _isRunning.set(true);
    }

    private void sendTrace(TraceSensor trace) {
        Intent intent = new Intent("sensor");
        intent.putExtra("trace", trace);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.i(TAG, "Sending trace");
    }
}
