package utility;

import java.io.Serializable;

public class TraceSensor implements Serializable {
	public long time;
	public double[] values = null;
	public int dim;
	public double degree = 0.0;
	public String type;

	public int index = -1;
    public static String ACCELEROMETER = "accelerometer";
    public static String GYROSCOPE = "gyroscope";
    public static String MAGNETOMETER = "magnetometer";
    public static String ROTATION_MATRIX = "rotation_matrix";
    public static String GPS = "gps";
    public static String ORIENTATION = "orientation";

	public TraceSensor() {
		index = 0;
		time = 0;
		dim = 3;
		values = new double[dim];
	}

	public TraceSensor(int d) {
		time = 0;
		dim = d;
		values = new double[dim];
	}

	public TraceSensor(long timestamp, double x, double y, double z){
		values = new double[3];
		time = timestamp;
		values[0] = x;
		values[1] = y;
		values[2] = z;
		dim = 3;
	}

	public void setValues(double x, double y, double z) {
		values[0] = x;
		values[1] = y;
		values[2] = z;
		dim = 3;
	}

	public void copyTrace(TraceSensor trace) {
		this.time = trace.time;
		this.dim = trace.dim;
		this.values = new double[dim];
		this.index = trace.index;
		for (int i = 0; i < dim; ++i) {
			this.values[i] = trace.values[i];
		}
	}
	
	/**
	 * Set the index of the trace in the entire trace list
	 * @param index
	 */
	public void setTraceIndex(int index)
	{
		this.index = index;
	}

	public void getTrace(String line) {
		String[] res = line.split(Constants.kInputSeperator);
		try {

//			time = Long.parseLong(res[0]);
		    time = (long) (Double.parseDouble(res[0]));
		} catch (NumberFormatException e) {
			// handle error time format
			time = 0;
		}
		// Log.log(dim, line);
		for (int i = 0; i < dim; ++i) {
			try {
				values[i] = Double.parseDouble(res[i + 1]);
			} catch (NumberFormatException e) {
				values[i] = 0.0;
			}
		}

		// System.out.println(time + Constants.kSeperator + values[0]);
	}

	public String toString() {
		String res = new String("");
		res = res.concat(String.valueOf(time));
		for (int i = 0; i < dim; ++i) {
			res = res.concat(Constants.kOutputSeperator
					+ String.valueOf(values[i]));
		}
		return res;
	}

}