package realtimedetection;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import omnicameras.wings.omnicameras.utility.Event;
import omnicameras.wings.omnicameras.utility.Formulas;
import omnicameras.wings.omnicameras.utility.PreProcess;
import omnicameras.wings.omnicameras.utility.Trace;


public class RealTimeLaneChangeDetection {
    private final String TAG = "Lane Change Detection";

    private static long kMinimumDuration = 2500;
    private static double kTurnThreshold = 0.6;
    private static double kThreshold = 1;
    private static int kWindowSize = 15;
    public List<Trace> orientation = new ArrayList<Trace>();
    public List<Trace> ori_grade = new ArrayList<Trace>();
    int lc_counter = 0;
    int lc_pos = 0;
    int lc_neg = 0;
    double lc_pmax = 0;
    double lc_nmax = 0;
    public Event lc_event = null;
    List<Trace> lc_window_orientation = new LinkedList<Trace>();
    public List<Event> lc_events = new ArrayList<Event>();
    Trace lastgrade = new Trace(2);
    public boolean lcfound = false;
    
    public RealTimeLaneChangeDetection(){
    	
    }
    
    public void processTrace(Trace trace){
		if(orientation.size() == 0){
			orientation.add(trace);
			lastgrade = trace;
			return;
		}
        Trace curori = exponentialMovingAverage(lastgrade, trace, -1, 0.3);
        lastgrade = curori;
		orientation.add(curori);
        if(orientation.size() > 4){
            Trace grade = findDerivative(orientation.get(orientation.size()-5), curori, 0);
            ori_grade.add(grade);
            extractLaneChanges(grade, 0);
        }
    }
    
	public Trace findDerivative(Trace last, Trace cur, int dim){
        Trace tmp = new Trace(2);
        tmp.time = last.time;
        tmp.values[0] = (cur.values[dim]-last.values[dim])/(cur.time-last.time)*1000;
        tmp.values[1] = Math.signum(tmp.values[0]);
        return tmp;
    }

    public void extractLaneChanges(Trace cur, int dim) {
        Log.i(TAG, "lane change detection");
        double value = cur.values[dim];
        if(Math.abs(value) >= kTurnThreshold){
            lc_counter++;
        }
        Trace past = null;
        lc_window_orientation.add(cur);
        if(lc_window_orientation.size() > kWindowSize) {
            past = lc_window_orientation.remove(0);
        }else{
            return;
        }
        double pv = past.values[dim];
        if(Math.abs(pv) >= kTurnThreshold){
            lc_counter--;
        }
        boolean turning = false;
        if((double)lc_counter >= (double)kWindowSize * 0.5) {
            turning = true;
        }
        //Log.log("lc_counter: " + lc_counter + " ori_grade: " + ori_grade.size() + " grade: " + cur.values[0]);
        if(turning) {
            if(lc_event == null ) {
                lc_event = new Event();
                lc_event.start_ = lc_window_orientation.get(0).time;
                lc_event.start_index_ = ori_grade.size()-kWindowSize;
                for(Trace tr:lc_window_orientation){
                    double vsign = Math.signum(tr.values[dim]);
                    if(vsign > 0){
                        if(tr.values[dim] > lc_pmax){
                            lc_pmax = tr.values[dim];
                        }
                        lc_pos ++;
                    }else{
                        if(Math.abs(tr.values[dim]) > lc_nmax){
                            lc_nmax = Math.abs(tr.values[dim]);
                        }
                        lc_neg ++;
                    }
                }
            }
            double vsign = Math.signum(value);
            if(vsign > 0){
                if(value > lc_pmax){
                    lc_pmax = value;
                }
                lc_pos ++;
            }else{
                if(Math.abs(value) > lc_nmax){
                    lc_nmax = Math.abs(value);
                }
                lc_neg ++;
            }
        } else {
            if(lc_event != null ) {
                lc_event.end_ = cur.time;
                lc_event.end_index_ = ori_grade.size()-1;
                lc_event.type_ = Event.LANECHANGE;
                if((lc_event.end_ - lc_event.start_) > kMinimumDuration){
                    double npct = (double)lc_neg/(lc_event.end_index_ - lc_event.start_index_);
                    double ppct = (double)lc_pos/(lc_event.end_index_ - lc_event.start_index_);
                    Trace avg = PreProcess.getAverage(ori_grade.subList(lc_event.start_index_, lc_event.end_index_));
                    //Log.log(lc_event.start_  + " <---> " + lc_event.end_, Formulas.secondToMinute(lc_event.start_/1000) + " <---> " + Formulas.secondToMinute(lc_event.end_/1000)  + " avg: " + avg.values[0] + " npct: " + npct + " ppct: " + ppct + " pmax: " + lc_pmax + " nmax: " + lc_nmax);
                    //if((Math.abs(npct-ppct) <= 0.5)){

                    if((Math.abs(avg.values[0]) <= 1) && (Math.abs(npct-ppct) <= 0.8) || (Math.abs(avg.values[0]) <= 3) && (Math.abs(npct-ppct) <= 0.65)){
                        double headingchange = getMaxHeadingChange(lc_event, dim);
                        if(lc_pmax>kThreshold && lc_nmax>kThreshold && headingchange<30){                            lc_events.add(lc_event);
                            lcfound = true;
                            Log.i(TAG, lc_event.start_  + " <---> " + lc_event.end_ + Formulas.secondToMinute(lc_event.start_/1000) + " <---> " + Formulas.secondToMinute(lc_event.end_/1000)  + " avg: " + avg.values[0] + " npct: " + npct + " ppct: " + ppct + " pmax: " + lc_pmax + " nmax: " + lc_nmax);

                        }
                    }
                }
            }
            lc_event = null;
            lc_pos = 0;
            lc_neg = 0;
            lc_pmax = 0;
            lc_nmax = 0;
        }
/*        if(null!= lc_event) {
            lc_event.end_ = orientation.get(orientation.size() - 1).time;
            lc_event.end_index_ = orientation.size()-1;
            lc_event.type_ = Event.LANECHANGE;
            if((lc_event.end_ - lc_event.start_) > kMinimumDuration){
                lc_events.add(lc_event);
            }
        }*/
    }


    public static Trace exponentialMovingAverage(Trace last, Trace cur, int index, double alpha) {
        int d = last.dim;
        Trace trace = new Trace(d);
        trace.copyTrace(cur);
        if(index >= 0) {
            trace.values[index] = alpha * cur.values[index] + (1.0 - alpha) * last.values[index];
        } else {
            for(int j = 0; j < d; ++j) {
                trace.values[j] = alpha * cur.values[j] + (1.0 - alpha) * last.values[j];
            }
        }
        return trace;
    }

    public double getMaxHeadingChange(Event event, int dim){
        int maxpos = -1;
        int minpos = -1;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        List<Trace> data = orientation.subList(event.start_index_ + 4, event.end_index_ + 4);
        for(int i=0; i<data.size(); i++){
            Trace cur = data.get(i);
            if(cur.values[dim] < min){
                min = cur.values[dim];
                maxpos = i;
            }
            if(cur.values[dim] > max){
                max = cur.values[dim];
                minpos = i;
            }
        }
        return max-min;
    }
}
