package utility;

/**
 * Created by lkang on 11/19/17.
 */

public class RawFrame {
    private static String TAG = FrameData.class.getSimpleName();
    public long captureTime;
    public long dataSize = 0;
    public Trace gyro = new Trace(3, Trace.GYROSCOPE);
    public Trace gps = new Trace(3, Trace.GPS);

    public static final int requiredSpace = 500; // should be larger than the gson format itself
    public RawFrame(FrameData frameData, Trace gyro, Trace gps) {
        this.captureTime = frameData.getFrameSendTime();
        this.dataSize = frameData.getDataSize();
        if (gyro != null) {
            this.gyro = gyro;
        }
        if (gps != null) {
            this.gps = gps;
        }
    }
}
