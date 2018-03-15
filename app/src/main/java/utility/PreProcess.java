package utility;

import java.util.ArrayList;
import java.util.List;


public class PreProcess {
	
	
	/**
	 * 
	 * @param raw the input data to be interpolated 
	 * @param rate samples per second
	 * @return
	 */
	public static List<TraceSensor> interpolate(List<TraceSensor> raw, double rate) {
		List<TraceSensor> res = new ArrayList<TraceSensor>();
		
		int sz = raw.size();
		if(0==sz) return res;
		assert sz > 0 && rate>=1;
		long x = raw.get(0).time/1000 * 1000 + 1000;
		for(int i = 0; i < sz - 1; ++i) {
			TraceSensor cur = raw.get(i);
			TraceSensor next = raw.get(i + 1);
			if(x >= cur.time && x < next.time) {
				TraceSensor inter = new TraceSensor();
				inter.copyTrace(cur);
				inter.time = x;
				//Log.log(x/1000 - 1379879638, cur.values[2]);
				//assert (x/1000 - 1379879638) < 190;
				for(int j = 0; j < inter.dim; ++j) {
					long x1 = cur.time, x2 = next.time;
					double y1 = cur.values[j], y2 = next.values[j];
					
					double v1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
					//double v2 = y2 - (x2 - x) * (y2 - y1) / (x2 - x1);
					inter.values[j] = v1;
				}
				res.add(inter);
				x += (1000.0/rate);
				--i;
			}
		}
		return res;
		
	}
	


	/*
	 * make the trace starts from time 0
	 * 
	 * */
	
	public static List<TraceSensor> ClearTimeOffset(List<TraceSensor> traces) {
		int offset = (int) traces.get(0).time;
		List<TraceSensor> res = new ArrayList<TraceSensor>();
		for (int i = 0; i < traces.size(); i++) {
			TraceSensor tr = new TraceSensor();
			tr.time = traces.get(i).time + offset;
			tr.values = traces.get(i).values;
			res.add(tr);
		}
		return res;
	}
	
	public static List<TraceSensor> ClearTimeOffset(List<TraceSensor> traces, long offset) {
		List<TraceSensor> res = new ArrayList<TraceSensor>();
		for (int i = 0; i < traces.size(); i++) {
			TraceSensor tr = new TraceSensor();
			tr.time = traces.get(i).time - offset;
			tr.values = traces.get(i).values;
			res.add(tr);
		}
		return res;
	}
	
	
	/*extract a sublist of a given List<Trace>, using binary search 
	 * 
	 * any interval in the implementation is [si, ei), from si, inclusive, to ei, exclusive.
	 * */
	
	public static int binarySearch(List<TraceSensor> raw, int si, int ei, long target) {
		int mid = 0;
		ei -= 1;
		while(si <= ei) {
			mid = si + (ei - si)/2;
			if (raw.get(mid).time == target) {
				break;
			} else if (raw.get(mid).time < target) {
				si = mid + 1;
			} else {
				ei = mid - 1;
			}
		}
		return mid;
	}
	
	public static List<TraceSensor> extractSubList(List<TraceSensor> raw, long start, long end) {
		
		int si = binarySearch(raw, 0, raw.size(), start);
		//Log.error(start, raw.get(si).time);
		int ei = binarySearch(raw, si, raw.size(), end);
		//Log.error(end, raw.get(ei).time);
		return raw.subList(si, ei + 1);
	}
	
	/**
	 * 
	 * @param traces
	 * @param time
	 * @return
	 */
	public static TraceSensor getTraceAt(List<TraceSensor> traces, long time) {
/*		if(time - time/1000 * 1000 > 500) {
			time+=1000;
		}*/
		int sz = traces.size();
		if(sz == 0 || ! (time >= traces.get(0).time && time <= traces.get(sz - 1).time)) {
			return null;
		}
		if(sz > 1) {
			long td = traces.get(sz - 1).time - traces.get(0).time;
			int index;
/*			if(td/rate + 1 == sz) {
				//sample rate is 1.0
				index = (int)(time - traces.get(0).time)/rate;
			} else {
				//index = binarySearch(traces, 0, sz, time);
				index = getClosestK(traces, time);
			}
*/			index = getClosestK(traces, time);

			return traces.get(index);
		} else {
			return traces.get(0);
		}
	}
	
	public static int getClosestK(List<TraceSensor> raw, long target) {
		    int low = 0;
		    int high = raw.size() - 1;
		    if (high < 0)
		        throw new IllegalArgumentException("The array cannot be empty");

		    while (low < high) {
		        int mid = (low + high) / 2;
		        assert(mid < high);
		        long d1 = Math.abs(raw.get(mid).time - target);
		        long d2 = Math.abs(raw.get(mid+1).time - target);
		        if (d2 <= d1){
		            low = mid+1;
		        }else {
		            high = mid;
		        }
		    }
		    return high;
	}
	

	
	public static TraceSensor getAverage(List<TraceSensor> input) {
		return simpleMovingAverage(input, input.size()).get(0);
	}
	
	/*
	 * given a window size, get the moving average of the trace values
	 * e.g., for an array {1, 2, 3, 4, 5}, and wnd = 3
	 * the moving result is {(1+2+3)/3, (2+3+4)/3, (3+4+5)/3}
	 * return the overall average if wnd = input.size();
	 * */
	public static List<TraceSensor> simpleMovingAverage(List<TraceSensor> input, int wnd)
	{
		List<TraceSensor> res = new ArrayList<TraceSensor>();
		int sz = input.size();
		int d = input.get(sz - 1).dim;
		double [] sum = new double[d];
		for(int j = 0; j < d; ++j) sum[j] = 0.0;
		
		for(int i = 0, len = 1; i < sz; ++i, ++len) {
			TraceSensor temp = input.get(i);
			for(int j = 0; j < d; ++j) {
				sum[j] += temp.values[j];
			}
			/**/
			
			if(len == wnd) {
				--len;
				TraceSensor trace = new TraceSensor(d);
				trace.time = temp.time;
				for(int j = 0; j < d; ++j) {
					trace.values[j] = sum[j]/wnd;
					sum[j] -= input.get(i - wnd + 1).values[j];
				}
				res.add(trace);
			}

		}
		return res;
	}

	/*alpha is from 0 to 1
	 * if alpha is 1, the result List is exactly the same to input traces
	 * if alpha is 0, the result List is a List of the first value of input traces
	 * */
	public static List<TraceSensor> exponentialMovingAverage(List<TraceSensor> traces, int index, double alpha) {
				
		List<TraceSensor> res = new ArrayList<TraceSensor>();
		int sz = traces.size();
		if(0==sz)
			return res;
		int d = traces.get(sz - 1).dim;
		double[] history = new double [d];
	
		for(int i = 0; i < sz; ++i) {
			TraceSensor trace = new TraceSensor();
			trace.copyTrace(traces.get(i));
			if(i==0) {
				history = trace.values.clone();
				res.add(trace); 
				continue;
			}
			if(index >= 0) {
				trace.values[index] = alpha * traces.get(i).values[index] + (1.0 - alpha) * history[index];
				history[index] = trace.values[index];
			} else {
				for(int j = 0; j < d; ++j) {
					trace.values[j] = alpha * traces.get(i).values[j] + (1.0 - alpha) * history[j];
					history[j] = trace.values[j];
				}
			}
			res.add(trace);
		}
		return res;
	}
	

}