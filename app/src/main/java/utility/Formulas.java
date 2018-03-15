package utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Formulas {

	public static double vectorLength(Trace trace) {
		return vectorLength(trace, trace.dim);
	}
	/**
	 * The name vectorSum is very very confusing!!!!!
	 * It is actually the length of a vector!
	 * @param trace
	 * @param dim
	 * @return
	 */

	public static double vectorLength(Trace trace, int dim) {
		double sum = 0.0;
		for(int i = 0; i < dim; ++i) {
			sum += Math.pow(trace.values[i], 2);
		}
		double res = Math.sqrt(sum);
		return res;
	}
	
	public static double[] vectorSum(Trace trace1, Trace trace2)
	{
		if(trace1.dim != trace2.dim)
		{
			throw new IllegalArgumentException("The dimension of two trace values must be the same");
		}
		double[] value1 = trace1.values;
		double[] value2 = trace2.values;
		double[] sum = new double[trace1.dim];
		for(int i = 0; i < trace1.dim; i++)
		{
			sum[i] = value1[i] + value2[i];
		}
		return sum;
	}
	
	/**
	 * Pure mathematical equations. Get the sum of distance squared from the points to the line.
	 * @param slope
	 * @param sum_x2
	 * @param sum_y2
	 * @param sum_xy
	 * @return
	 */
	
	public static double DistanceSquare(double slope, double sum_x2, double sum_y2, double sum_xy) {
		double res = (sum_y2 - 2 * slope * sum_xy + Math.pow(slope, 2) * sum_x2) / (Math.pow(slope, 2) + 1);
		return res;
	}

	/*0 to 360
	 * 
	 * degree difference from d2 to d1
	 * */
	public static double degreeDifference(double d1, double d2) {
		double diff = d2 - d1;
		if(diff > 180.0) {
			diff = diff - 360.0;
		} else if (diff < -180.0) {
			diff = diff + 360.0;
		} else {}

		return diff;
	}


	/**
	 * 
	 * 
	 * @param traces
	 * @return [deviation]
	 */
	public static double[] absoluteDeviation(List<Trace> traces) {
		int sz = traces.size();
		int d = traces.get(sz - 1).dim;
		
		double[] average = new double[d];
		double[] deviation = new double [d];
		for(int j = 0; j < d; ++j) {
			average[j] = 0.0;
			deviation[j] = 0.0;
		}
		for(Trace trace: traces) {
			for(int j = 0; j < d; ++j) {
				average[j] += trace.values[j];
			}
		}
		for(int j = 0; j < d; ++j) {
			average[j] /= sz;
		}
		for(Trace trace: traces) {
			for(int j = 0; j < d; ++j) {
				deviation[j] += Math.abs(average[j] - trace.values[j]);
			}
		}
		/*
		double [][] res = new double[2][d];		
		for(int j = 0; j < d; ++j) {
			deviation[j] /= sz;
			res[0][j] = average[j];
			res[1][j] = deviation[j];
		}
		*/
		return deviation;
	}
	/*
	 * For a given trace (preferably the raw accelerometer data, but apply to all)
	 * return the standard deviation of the traces
	 * */
	public static double[] standardDeviation(List<Trace> traces) {
		int sz = traces.size();
		int d = traces.get(sz - 1).dim;
		
		double[] average = new double[d];
		double[] res = new double [d];
		for(int j = 0; j < d; ++j) {
			average[j] = 0.0;
			res[j] = 0.0;
		}
		for(Trace trace: traces) {
			for(int j = 0; j < d; ++j) {
				average[j] += trace.values[j];
			}
		}
		for(int j = 0; j < d; ++j) {
			average[j] /= sz;
		}
		for(Trace trace: traces) {
			for(int j = 0; j < d; ++j) {
				res[j] += Math.pow((average[j] - trace.values[j]), 2.0);
			}
		}
		for(int j = 0; j < d; ++j) {
			res[j] = Math.sqrt(res[j]/sz);
		}
		
		return res;
	}
	
	
	public static double linear_correlation(double [] x, double [] y) {
		double corr = 0.0;
		int sz = x.length;
		double average_x = 0.0;
		double average_y = 0.0;
		for(int i = 0 ; i < sz; ++i) {
			average_x += x[i];
			average_y += y[i];
		}
		average_x /= sz;
		average_y /= sz;
		
		double upper = 0.0;
		double m_x = 0.0, m_y = 0.0;
		for(int i = 0 ; i < sz; ++i) {
			upper += (x[i] - average_x) * (y[i] - average_y);
			m_x += (x[i] - average_x) * (x[i] - average_x);
			m_y += (y[i] - average_y) * (y[i] - average_y);
		}
		if(m_x*m_y ==0 || m_x*m_y != m_x*m_y) corr = 1;
		else corr = upper / Math.sqrt(m_x * m_y);
		
		return corr;
	}
	
	

	/**
	 * Given a list of unit vectors, calculate the standard deviation
	 * @param unitVectors the list of unit vectors
	 * @return 2d array that stores the standard deviation value
	 */
	/*
	public static double[] standardDeviationUnitVector(List<PairDouble> unitVectors) {
		int sz = unitVectors.size();
		double[] average = new double[2];
		double[] res = new double [2];
		for (int i = 0; i < 2; i++)
		{
			average[i] = 0;
			res[i] = 0;
		}
		for (int i = 0; i < sz; i++)
		{
			average[0] += unitVectors.get(i).x;
			average[1] += unitVectors.get(i).y;
		}
		average[0] /= sz;
		average[1] /= sz;
		for (PairDouble vector : unitVectors)
		{
			res[0] += Math.pow(vector.x - average[0], 2.0);
			res[1] += Math.pow(vector.y - average[1], 2.0);
		}
		res[0] = Math.sqrt(res[0]/sz);
		res[1] = Math.sqrt(res[1]/sz);
		return res;
	}*/
	
	/*
	 * For a given trace (preferably the raw accelerometer data, but apply to all)
	 * return the standard deviation of the traces
	 * */
	
	public static double standardDeviationDegree(List<Trace> traces) {
		int sz = traces.size();
		double sum = 0.0;
		for (Trace t : traces)
		{
			sum += t.degree;
		}
		double average = sum / sz;
		double res = 0.0;
		for (Trace t : traces)
		{
			res += Math.pow(t.degree - average, 2.0);
		}
		res = Math.sqrt(res/sz);
		return res;
	}
	
	/**
	 * convert milliseconds to date and hours
	 * @param base
	 * @param time
	 * @return
	 */
	public static String displayTime(String base, Long time){
		long tmp = Long.valueOf(base.substring(0, base.length() - 3)).longValue();
		tmp = tmp + time;
	    Date date = new Date(tmp);
	    DateFormat formatter = new SimpleDateFormat("EEE MM/dd/yyyy HH:mm:ss");
//	    System.out.println(formatter.format(date));
	    return formatter.format(date);
	}
	
	/**
	 * convert milliseconds to date and hours
	 * @param base
	 * @param time
	 * @return
	 */
	public static String displayTime(Long time){
	    Date date = new Date(time);
	    DateFormat formatter = new SimpleDateFormat("EEE MM/dd/yyyy HH:mm:ss");
//	    System.out.println(formatter.format(date));
	    return formatter.format(date);
	}

	
	/**
	 * convert milliseconds to date and hours
	 * @param base
	 * @param time
	 * @return
	 */
	public static int formatTime(Long time){
	    Date date = new Date(time);
	    DateFormat formatter = new SimpleDateFormat("EEE MMdd/yyyy HH:mm:ss");
//	    System.out.println(formatter.format(date));
	    String dateinString = formatter.format(date);
	    int hour = Integer.parseInt(dateinString.substring(dateinString.length()-8, dateinString.length()-6));
	    int min = Integer.parseInt(dateinString.substring(dateinString.length()-5, dateinString.length()-3));
//	    Log.log(dateinString, hour, min);
	    int result;
	    if(min<15){
	    	result = hour*100;
	    }else if(min>=15 && min < 45){
	    	result = hour*100 + 30;
	    }else{
	    	result = (hour+1)*100;	    	
	    }
	    return result;
	}
	
	/**
	 * convert milliseconds to date and hours
	 * @param base
	 * @param time
	 * @return
	 */
	public static int formatDate(Long time){
	    Date date = new Date(time);
	    DateFormat formatter = new SimpleDateFormat("EEE MMdd/yyyy HH:mm:ss");
//	    System.out.println(formatter.format(date));
	    String dateinString = formatter.format(date);
	    int hour = Integer.parseInt(dateinString.substring(dateinString.length()-8, dateinString.length()-6));
	    int min = Integer.parseInt(dateinString.substring(dateinString.length()-5, dateinString.length()-3));
	    int day = Integer.parseInt(dateinString.substring(dateinString.length()-18, dateinString.length()-14));
//	    Log.log(dateinString, day);
	    return day;
	}
	
	/**
	 * convert milliseconds to date and hours
	 * @param base
	 * @param time
	 * @return
	 */
	public static int timeToWeek(Long time){
	    Date date = new Date(time);
	    DateFormat formatter = new SimpleDateFormat("EEE MM/dd/yyyy HH:mm:ss");
//	    System.out.println(formatter.format(date));
	    String dateinString = formatter.format(date);
	    String week = dateinString.substring(0, 3);
	    switch(week.toLowerCase()){
	    case "mon":
	    	return 1;
	    case "tue":
	    	return 2;
	    case "wed":
	    	return 3;
	    case "thu":
	    	return 4;
	    case "fri":
	    	return 5;
	    case "sat":
	    	return 6;
	    case "sun":
	    	return 7;	    	
	    }
	    return 0;
	}
	
	public static boolean isEven(double num){
		return ((num % 2) == 0); 
	}
	
	public static double roundToTen(double num){
		double number = Math.round(num/ 10.0) * 10.0;
		return number;
	}
	
	 public static double[] listTraceToDoubleArray(List<Trace> data, int dim){
		    double[] result = new double[data.size()];
		   	for(int i=0; i<=data.size()-1; i++){
		  		result[i] = data.get(i).values[dim];
		   	}
		   	return result;
		 }
	 
		public static int getMin(List<Trace> data, int dim){
			int pos = -1;
			double min = Double.POSITIVE_INFINITY;
			for(int i=0; i<data.size(); i++){
				Trace cur = data.get(i);
				if(cur.values[dim] < min){
					min = cur.values[dim];
					pos = i;
				}
			}
			return pos;
		}
		
		public static int getMax(List<Trace> data, int dim){
			int pos = -1;
			double max = Double.NEGATIVE_INFINITY;
			for(int i=0; i<data.size(); i++){
				Trace cur = data.get(i);
				if(cur.values[dim] > max){
					max = cur.values[dim];
					pos = i;
				}
			}
			return pos;
		}
		
		public static Trace turnAngle(List<Trace> gyroscope) {
			int sz = gyroscope.size();
			double[] rads = new double[gyroscope.get(0).dim];
			Arrays.fill(rads, 0.0);
			Trace tr = new Trace(gyroscope.get(0).dim);
			for(int i = 0; i < sz - 1; ++i) {
				Trace trace = gyroscope.get(i);
				long time_diff = gyroscope.get(i + 1).time - gyroscope.get(i).time;
				for(int j=0; j<trace.dim; j++){
					double x = trace.values[j];
					rads[j] += x * (time_diff/1000.0);
				}
			}
			for(int i=0; i< gyroscope.get(0).dim; i++){
				tr.values[i] = Math.toDegrees(rads[i]);
			}
			//Log.log("rads: " + Math.toDegrees(rads) + " truth: " + tr.values[0] + " error: " + tr.values[2]);
			return tr;
		}
		
		public static double turnAngle(List<Trace> gyroscope, int dim) {
			int sz = gyroscope.size();
			double rads = 0;
			for(int i = 0; i < sz - 1; ++i) {
				Trace trace = gyroscope.get(i);
				long time_diff = gyroscope.get(i + 1).time - gyroscope.get(i).time;
				double x = trace.values[dim];
				rads += x * (time_diff/1000.0);
			}
			return Math.toDegrees(rads);
		}
		
		/**
		 * type = 1 --> calculate difference 
		 * type = -1 --> calculate changing rate
		 * @param data
		 * @param type
		 * @return
		 */
		public static List<Trace> calcSlope(List<Trace> data, int type){
			List<Trace> slopes = new ArrayList<Trace>();
			int dim = data.get(0).dim;
			for(int i=0; i<data.size()-1; i++){
				Trace cur = data.get(i);
				Trace next = data.get(i+1);
				Trace slope = new Trace(dim);
				slope.time = cur.time;
				if(type > 0){
					for(int j=0; j<dim; j++){
						slope.values[j] = (next.values[j] - cur.values[j]);
					}
				}else{
					for(int j=0; j<dim; j++){
						slope.values[j] = (next.values[j] - cur.values[j])/(next.time - cur.time)*1000;
					}					
				}
				slopes.add(slope);
			}
			return slopes;
		}
		
		public static String secondToMinute(long l){
			String minute = null;
			minute = ((l/60)%60) + ":" + (l%60); 
			return minute;
		}
}
