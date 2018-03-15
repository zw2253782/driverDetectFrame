package realtimedetection;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import utility.Event;
import utility.PreProcess;
import utility.TraceSensor;

public class RealTimeTurnDetection {
	private final String TAG = "Turn Detection";

	private List<TraceSensor> magnetic = new ArrayList<TraceSensor>();
	private List<TraceSensor> accelerometer = new ArrayList<TraceSensor>();
	private List<TraceSensor> gyroscope = new ArrayList<TraceSensor>();
	private List<TraceSensor> gyro_con = new ArrayList<TraceSensor>();
	private TraceSensor initAcce = null;
	private TraceSensor initMag = null;
	private TraceSensor rotation = null;
	
	//turn detection
	private static double kTurnThreshold = 0.1;
	private static int kWindowSize = 20;
	private int tn_counter = 0;
	private double tn_cumturn = 0.0;
	public Event tn_event = null;
	private List<TraceSensor> tn_window_gyroscope = new LinkedList<TraceSensor>();
	public List<Event> tn_events = new ArrayList<Event>();
	public boolean turnfound = false;
	
	public RealTimeTurnDetection(){
		
	}
	
	public void processTrace(TraceSensor trace){
		if(trace.type.equals(TraceSensor.ACCELEROMETER)){
			//Log.d(TAG, "acce received");
			accelerometer.add(trace);
		}else if(trace.type.equals(TraceSensor.MAGNETOMETER)){
			//Log.d(TAG, "mag received");
			magnetic.add(trace);
		}else if(trace.type.equals(TraceSensor.GYROSCOPE)){
			//Log.d(TAG, "gyro received");
			gyroscope.add(trace);
		}
		if(trace.time > 10000){
			if(initAcce ==null || initMag == null || rotation == null){
				if(accelerometer.size()>10 && magnetic.size() > 10) {
					initAcce = PreProcess.getAverage(accelerometer);
					initMag = PreProcess.getAverage(magnetic);
					rotation = calculateRotationMatrix(initAcce, initMag);
				}
			}
			if(trace.type.equals(TraceSensor.GYROSCOPE)){
				TraceSensor gyro = RotationMethod(trace, rotation.values);
				gyro_con.add(gyro);
				extractTurn(gyro, 2);
			}
		}
	}
	
	public void extractTurn(TraceSensor gyro, int dim) {
		Log.i(TAG, "extract turns");
		double value = gyro.values[dim];
		TraceSensor past = null;
		tn_window_gyroscope.add(gyro);
		//Log.log(tn_counter, gyro);
		if(tn_window_gyroscope.size() > kWindowSize) {
			past = tn_window_gyroscope.remove(0);
		} else {
			return;
		}
		TraceSensor pre = tn_window_gyroscope.get(kWindowSize - 2);
		if(Math.abs(value) >= kTurnThreshold){
			tn_counter++;			
		}
		double pv = past.values[dim];			
		if(Math.abs(pv) >= kTurnThreshold){
			tn_counter--;
		}
		boolean turning = false;
		if((double)tn_counter >= (double)kWindowSize * 0.6) {
			turning = true;
		}
		if(turning) {
			tn_cumturn += Math.abs(pre.values[dim]) * (gyro.time - pre.time)/1000.0;
			if(tn_event == null) {
				tn_event = new Event();
				tn_event.start_ = tn_window_gyroscope.get(0).time;
				tn_event.start_index_ = gyro_con.size()-kWindowSize;
				for(int ii=1; ii<kWindowSize-1; ii++){
					TraceSensor cur = tn_window_gyroscope.get(ii);
					TraceSensor prev = tn_window_gyroscope.get(ii-1);
					tn_cumturn += Math.abs(prev.values[dim]) * (cur.time - prev.time)/1000.0;
				}
			}
		} else {
			if(tn_event != null) {
				tn_event.end_ = gyro.time;
				tn_event.end_index_ = gyro_con.size()-1;
				tn_event.type_ = Event.TURN;
				tn_cumturn += Math.abs(pre.values[dim]) * (gyro.time - pre.time)/1000.0;
				Log.i(TAG, "Start: " + tn_event.start_/1000 + " end: " + tn_event.end_/1000 + " angle: " + Math.toDegrees(tn_cumturn));
				if(Math.abs(Math.toDegrees(tn_cumturn)) >= 55.0){
					tn_events.add(tn_event);
					turnfound = true;
				}
			}
			tn_cumturn = 0.0;
			tn_event = null;
		}
	
	/*	if(null!= tn_event) {
			tn_event.end_ = gyro.get(gyro.size() - 1).time;
			tn_event.type_ = Event.TURN;
			if(Math.abs(Math.toDegrees(tn_cumturn)) >= 60.0){
				Log.log("recording turn");
				Log.log("angle: ", Math.toDegrees(tn_cumturn));
				tn_events.add(tn_event);
			}
		}*/
//		ReadWriteData.writeTrace(turns, outpath.concat("/turns.dat"));
	}

	/**
	 * Calculates rotation matrix from accelerometer and magnetometer output.
	 * acce and mag are average value
	 */
	public static TraceSensor calculateRotationMatrix(TraceSensor acce, TraceSensor mag)
	{
		float[] magnetic = traceToFloat(mag);
		float[] acceleration = traceToFloat(acce);
		float[] baseOrientation = new float[3];
		// accelerometer and magnetometer based rotation matrix
		float[] rotationMatrix = new float[9];
		SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetic);
		TraceSensor rotation = floatToTrace(rotationMatrix, 0);
		return rotation;
	}

	public static float[] traceToFloat(TraceSensor data){
		int dim = data.dim;
		float[] result = new float[dim];
		for(int i=0; i<dim; i++){
			result[i] = (float) data.values[i];
		}
		return result;
	}

	public static TraceSensor floatToTrace(float[] data, long time){
		int dim = data.length;
		TraceSensor result = new TraceSensor(dim);
		result.time = time;
		for(int i=0; i<dim; i++){
			result.values[i] = data[i];
		}
		return result;
	}

	/**
	 * Using rotational matrix.
	 * @param raw_tr
	 * @param rM
	 * @return
	 */
	public static TraceSensor RotationMethod(TraceSensor raw_tr, double[] rM) {
		TraceSensor calculated_tr = new TraceSensor();
		calculated_tr.time = raw_tr.time;
		double x, y, z;
		x = raw_tr.values[0];
		y = raw_tr.values[1];
		z = raw_tr.values[2];

		calculated_tr.values[0] = x * rM[0] + y * rM[1] + z * rM[2];
		calculated_tr.values[1] = x * rM[3] + y * rM[4] + z * rM[5];
		calculated_tr.values[2] = x * rM[6] + y * rM[7] + z * rM[8];

		return calculated_tr;
	}
}
