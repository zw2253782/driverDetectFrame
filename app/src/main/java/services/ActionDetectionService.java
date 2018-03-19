package services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import database.DatabaseHelperSensor;

import main.MainActivity;
import realtimedetection.RealTimeBrakeDetection;
import realtimedetection.RealTimeLaneChangeDetection;
import realtimedetection.RealTimeTurnDetection;
import selfdriving.streaming.R;
import utility.Event;
import utility.Formulas;
import utility.TraceSensor;
import utility.PreProcess;


/**
 * Created by bozhao on 2/20/18.
 */

public class ActionDetectionService extends Service {
    private final String TAG = "Detection Service";
    private DatabaseHelperSensor _dbHelper = null;

    private final Binder _binder = new ActionDetectionBinder();
    private int acce = 0;
    private int gyro = 0;
    private int mag = 0;
    private int ori = 0;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 001;
    private Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


    RealTimeBrakeDetection brakeDetector = null;
    RealTimeTurnDetection turnDetector = null;
    RealTimeLaneChangeDetection lcDetector = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub

        return _binder;
    }

    public class ActionDetectionBinder extends Binder {
        public void setDatabaseHelper(DatabaseHelperSensor dbhelper) {
            _dbHelper = dbhelper;
        }
        ActionDetectionService getService(){
            return ActionDetectionService.this;
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        brakeDetector = new RealTimeBrakeDetection();
        turnDetector = new RealTimeTurnDetection();
        lcDetector = new RealTimeLaneChangeDetection();
        LocalBroadcastManager.getInstance(this).registerReceiver(mSensorMessageReceiver, new IntentFilter("sensor"));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "stop service");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSensorMessageReceiver);
        brakeDetector = null;
        turnDetector = null;
        lcDetector = null;

        stopSelf();

        // Tell the user we stopped.
        Toast.makeText(this, "Detection service shutdown", Toast.LENGTH_SHORT).show();
    }


    private void showNotification(CharSequence title, CharSequence contents) {
        Log.i(TAG, "notify called " + title + contents);
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)  // the status icon
                .setContentTitle(title)  // the label of the entry
                .setContentText(contents)  // the contents of the entry
                .setSound(uri)
                .build();
        // Send the notification.
//        mNM.notify(NOTIFICATION, notification);
        startForeground(NOTIFICATION, notification);
    }


    private void showForegroundNotification(String title, String contentText) {
        Log.i(TAG, contentText);
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("test"));
        PendingIntent pendingIntent
                = PendingIntent.getActivity(getBaseContext(),
                0, myIntent,
                Intent.FLAG_ACTIVITY_NEW_TASK);


        Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(contentText).setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent).setSound(uri).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        notificationManager.notify(m, notification);
    }

    private BroadcastReceiver mSensorMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.i(TAG, "broadcast received");
            TraceSensor data = (TraceSensor) intent.getSerializableExtra("trace");
            if(data.type.equals(TraceSensor.ACCELEROMETER)){
                if(turnDetector != null) {
                    onAccelerationChanged(data);
                }
            }else if(data.type.equals(TraceSensor.MAGNETOMETER)){
                if(turnDetector != null) {
                    onMagnetometerChanged(data);
                }
            }else if(data.type.equals(TraceSensor.ORIENTATION)){
                if(lcDetector != null) {
                    onOrientationChanged(data);
                }
            }else if(data.type.equals(TraceSensor.GYROSCOPE)){
                if(turnDetector != null) {
                    onGyroscopeChanged(data);
                }
            }else if(data.type.equals(TraceSensor.GPS)){
                if(brakeDetector != null) {
                    onGPSChanged(data);
                }
            }
        }

    };

    private void onMagnetometerChanged(TraceSensor magnetometer) {
        turnDetector.processTrace(magnetometer);
    }

    private  void onAccelerationChanged(TraceSensor accelerometer){
        turnDetector.processTrace(accelerometer);
    }

    private  void onGyroscopeChanged(TraceSensor gyroscope){
        Log.i(TAG, String.valueOf(gyroscope.time));
        turnDetector.processTrace(gyroscope);
        if(turnDetector.turnfound){
            Event tn_event = turnDetector.tn_events.get(turnDetector.tn_events.size()-1);
            utility.Log.log("Turns: " + tn_event.start_/1000 + " <---> " + tn_event.end_/1000, "  " + Formulas.secondToMinute(tn_event.start_/1000) + " <---> " + Formulas.secondToMinute(tn_event.end_/1000));
            long time = System.currentTimeMillis();
            _dbHelper.addEventData(time, tn_event.start_, tn_event.end_, tn_event.type_);
            showForegroundNotification("Turn", Formulas.secondToMinute(tn_event.start_/1000) + " <---> " + Formulas.secondToMinute(tn_event.end_/1000));
            turnDetector.turnfound = false;
        }
    }

    private void onGPSChanged(TraceSensor gps) {
        brakeDetector.processTrace(gps);
        if(brakeDetector.brakefound){
            Event br_inter = brakeDetector.brakes.get(brakeDetector.brakes.size()-1);
//            omnicameras.wings.omnicameras.utility.Log.log("Brake : " + br_inter.start_/1000 + " <---> " + br_inter.end_/1000, "  " + Formulas.secondToMinute(br_inter.start_/1000) + " <---> " + Formulas.secondToMinute(br_inter.end_/1000));
            long time = System.currentTimeMillis();
            _dbHelper.addEventData(time, br_inter.start_, br_inter.end_, br_inter.type_);
            showForegroundNotification("Brake", Formulas.secondToMinute(br_inter.start_/1000) + " <---> " + Formulas.secondToMinute(br_inter.end_/1000));
            brakeDetector.brakefound = false;
        }
        if(brakeDetector.stopfound){
            Event st_inter = brakeDetector.stops.get(brakeDetector.stops.size()-1);
  //          omnicameras.wings.omnicameras.utility.Log.log("Stop : " + st_inter.start_/1000 + " <---> " + st_inter.end_/1000, "  " + Formulas.secondToMinute(st_inter.start_/1000) + " <---> " + Formulas.secondToMinute(st_inter.end_/1000));
            long time = System.currentTimeMillis();
            _dbHelper.addEventData(time, st_inter.start_, st_inter.end_, st_inter.type_);
            showForegroundNotification("Stop", Formulas.secondToMinute(st_inter.start_/1000) + " <---> " + Formulas.secondToMinute(st_inter.end_/1000));
            brakeDetector.stopfound = false;
        }
    }

    private void onOrientationChanged(TraceSensor trace){
        lcDetector.processTrace(trace);
        if(lcDetector.lcfound){
            Event lc_event = lcDetector.lc_events.get(lcDetector.lc_events.size()-1);
            long time = System.currentTimeMillis();
            _dbHelper.addEventData(time, lc_event.start_, lc_event.end_, lc_event.type_);
            showForegroundNotification("Lane Change", Formulas.secondToMinute(lc_event.start_/1000) + " <---> " + Formulas.secondToMinute(lc_event.end_/1000));
            lcDetector.lcfound = false;
           // Log.i(lc_event.start_  + " <---> " + lc_event.end_, Formulas.secondToMinute(lc_event.start_/1000) + " <---> " + Formulas.secondToMinute(lc_event.end_/1000));  //+ " avg: " + avg.values[0] + " npct: " + npct + " ppct: " + ppct + " pmax: " + lc_pmax + " nmax: " + lc_nmax);
        }
    }

}
