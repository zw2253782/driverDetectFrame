package realtimedetection;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import omnicameras.wings.omnicameras.utility.Event;
import omnicameras.wings.omnicameras.utility.Formulas;
import omnicameras.wings.omnicameras.utility.Trace;

public class RealTimeBrakeDetection {
	private final String TAG = "Brake Detection";

	public List<Event> brakes = new ArrayList<Event>();
	double br_initvalue = 0.0;
	LinkedList<Trace> br_sliding = new LinkedList<Trace>();
	public Event br_inter = null;
	boolean br_in_static = false;
	List<Trace> gpstraces = new ArrayList<Trace>();

	//stop detection
	public Event st_inter = null;
	boolean st_in_static = false;
	public List<Event> stops = new ArrayList<Event>();

	public boolean brakefound = false;
	public boolean stopfound = false;
	
	public RealTimeBrakeDetection() {
		
	}
	
	public void processTrace(Trace gps){
		extractBrakeIntervals(gps, 5, 3); // detect brakes, should be called before stop detection ==> gpstraces.add(gps);

		extractStopIntervals(gps, 3); // detect stops
	}
	
	public void extractStopIntervals(Trace gps, int dim) {
        Log.i(TAG, "stop detection");
		if(gps.values[dim-1] < 0.1){
			if(false==st_in_static) {
				st_in_static = true;
				st_inter = new Event();
				st_inter.start_index_ = gpstraces.size()-1;
				st_inter.start_ = gps.time;
				st_inter.type_ = Event.STOP;
			}
		}else{
			if(true==st_in_static) {
				st_in_static = false;
				st_inter.end_index_ = gpstraces.size() - 1;
				st_inter.end_ = gpstraces.get(gpstraces.size()-2).time;
				stops.add(st_inter);
				stopfound = true;
				Log.i(TAG, "Stop : " + st_inter.start_/1000 + " <---> " + st_inter.end_/1000+ "  " + Formulas.secondToMinute(st_inter.start_/1000) + " <---> " + Formulas.secondToMinute(st_inter.end_/1000));

			}
		}
		
//		if(true==in_static) {
//			inter.end_index_ = sz - 1;
//			inter.end_ = traces.get(sz - 1).time;
//		}
/*		for(Event ev: stops) {
			Log.log("Stop : " + ev.start_/1000 + " <---> " + ev.end_/1000, "  " + Formulas.secondToMinute(ev.start_/1000) + " <---> " + Formulas.secondToMinute(ev.end_/1000));
		}*/
	}

	public void extractBrakeIntervals(Trace gps, int wnd, int dim) {
        Log.i(TAG, "Brake detection");
		gpstraces.add(gps);
		br_sliding.add(gps);
		int len = br_sliding.size();	
		if(len==wnd) {
			br_initvalue = br_sliding.get(0).values[dim-1];
			if (isNonIncreasing(br_sliding, dim, br_initvalue))	{
				if(false==br_in_static) {
					br_in_static = true;
					br_inter = new Event();
					br_inter.start_index_ = gpstraces.size() - wnd;
					br_inter.start_ = gpstraces.get(gpstraces.size() - wnd).time;
					br_inter.type_ = Event.BRAKE;
				}
			} else {
				if(true==br_in_static) {
					br_in_static = false;
					br_inter.end_index_ = gpstraces.size() - 2;
					br_inter.end_ = gpstraces.get(gpstraces.size() - 2).time;
					brakes.add(br_inter);
					brakefound = true;
					Log.i(TAG, "Brake : " + br_inter.start_/1000 + " <---> " + br_inter.end_/1000 + "  " + Formulas.secondToMinute(br_inter.start_/1000) + " <---> " + Formulas.secondToMinute(br_inter.end_/1000));
				}
			}
			br_sliding.removeFirst();			
		}
		
//		if(true==in_static) {
//			inter.end_index_ = sz - 1;
//			inter.end_ = traces.get(sz - 1).time;
//			intervals.add(inter);
//		}
		//List<Event> brakes = combineEvents(intervals, 2100.0); 
/*		for(Event ev: brakes) {
			Log.log("Brake : " + ev.start_/1000 + " <---> " + ev.end_/1000, "  " + Formulas.secondToMinute(ev.start_/1000) + " <---> " + Formulas.secondToMinute(ev.end_/1000));
		}
*/		return;	
	}
	
	public static boolean isNonIncreasing (List<Trace> traces, int dim, double initvalue){
		int sz = traces.size();
		/* for debug purpose
		 * for(Trace tr: traces){
			System.out.print(tr.values[dim-1] + ", ");
		}
		System.out.println("");*/
		for (int i = 0; i <= sz -2; i++)
		{
			//Log.log("initial: " + initvalue, "i: " + traces.get(i).values[dim-1], "i+1: " + traces.get(i+1).values[dim-1]);
			if ((traces.get(i).values[dim-1] < traces.get(i+1).values[dim-1]) || (traces.get(i).values[dim - 1] == 0 ))
			{
				if(traces.get(i+1).values[dim-1] > (traces.get(i).values[dim-1] - initvalue)*90/100 + initvalue ||(traces.get(i+1).values[dim - 1]+traces.get(i).values[dim - 1] == 0 ) )
				{
					return false;
				}
			}
		}
		return true;
	}
}
