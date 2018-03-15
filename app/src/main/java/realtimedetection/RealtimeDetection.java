package realtimedetection;/*
package omnicameras.wings.omnicameras.realtimedetection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import omnicameras.wings.omnicameras.utility.Event;
import omnicameras.wings.omnicameras.utility.Formulas;
import omnicameras.wings.omnicameras.utility.Log;
import omnicameras.wings.omnicameras.utility.PreProcess;
import omnicameras.wings.omnicameras.utility.Trace;

*/
/**
 * Created by bozhao on 2/20/18.
 *//*


public class RealtimeDetection {

    private static long kMinimumDuration = 2500;
    private static double kTurnThreshold = 0.6;
    private static double kThreshold = 1;
    private static int kWindowSize = 15;
    private final String TAG = "Real time Detection";
    private List<Trace> accelerometer = new ArrayList<Trace>();
    private List<Trace> gyroscope = new ArrayList<Trace>();
    private List<Trace> magnetic = new ArrayList<Trace>();
    private List<Trace> orientation = new ArrayList<Trace>();
    private List<Trace> ori_grade = new ArrayList<Trace>();
    int lc_counter = 0;
    int lc_pos = 0;
    int lc_neg = 0;
    double lc_pmax = 0;
    double lc_nmax = 0;
    Event lc_event = null;
    List<Trace> lc_window_orientation = new LinkedList<Trace>();
    List<Event> lc_events = new ArrayList<Event>();
    Trace lastgrade = new Trace(2);
    RealTimeBrakeDetection brakeDetector = new RealTimeBrakeDetection();
    RealTimeTurnDetection turnDetector = new RealTimeTurnDetection();

    public  void processTrip(Trace data){
        if(data.type.equals(Trace.ACCELEROMETER)){
            onAccelerationChanged(data);

        }else if(data.type.equals(Trace.MAGNETOMETER)){
            onGyroscopeChanged(data);

        }else if(data.type.equals(Trace.ORIENTATION)){
        }else if(data.type.equals(Trace.GYROSCOPE)){
            onGyroscopeChanged(data);
        }else if(data.type.equals(Trace.GPS)){
            onGPSChanged(data);

        }
    }
    private void onMagnetometerChanged(Trace magnetometer) {
        turnDetector.processTrace(magnetometer);
    }

    private  void onAccelerationChanged(Trace accelerometer){
        turnDetector.processTrace(accelerometer);
    }

    private  void onGyroscopeChanged(Trace gyroscope){
        turnDetector.processTrace(gyroscope);
        if(turnDetector.turnfound){
            Event tn_event = turnDetector.tn_events.get(turnDetector.tn_events.size()-1);
            Log.log("Turns: " + tn_event.start_/1000 + " <---> " + tn_event.end_/1000, "  " + Formulas.secondToMinute(tn_event.start_/1000) + " <---> " + Formulas.secondToMinute(tn_event.end_/1000));
            turnDetector.turnfound = false;
        }
    }

    private void onGPSChanged(Trace gps) {
        brakeDetector.processTrace(gps);
        if(brakeDetector.brakefound){
            Event br_inter = brakeDetector.brakes.get(brakeDetector.brakes.size()-1);
            Log.log("Brake : " + br_inter.start_/1000 + " <---> " + br_inter.end_/1000, "  " + Formulas.secondToMinute(br_inter.start_/1000) + " <---> " + Formulas.secondToMinute(br_inter.end_/1000));
            brakeDetector.brakefound = false;
        }
        if(brakeDetector.stopfound){
            Event st_inter = brakeDetector.stops.get(brakeDetector.stops.size()-1);
            Log.log("Stop : " + st_inter.start_/1000 + " <---> " + st_inter.end_/1000, "  " + Formulas.secondToMinute(st_inter.start_/1000) + " <---> " + Formulas.secondToMinute(st_inter.end_/1000));
            brakeDetector.stopfound = false;
        }
    }




}
*/
