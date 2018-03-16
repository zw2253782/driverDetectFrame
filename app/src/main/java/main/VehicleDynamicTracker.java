package main;

import utility.OriginalTrace;

/**
 * Created by lkang on 12/4/17.
 */

public class VehicleDynamicTracker {
    private double distance_;
    private OriginalTrace gps_;
    private double degree_;
    private OriginalTrace gyro_ = null;
    private long timestamp_ = -1;
    private static double threshold_distance = 2.5; // meter
    private static double threshold_angle = 0.15; // degree
    private static long threshold_time = 10000; //ms

    private long lastIFrameTime = 0;
    public VehicleDynamicTracker () {
        this.timestamp_ = -1;
    }

    // check if a I frame is required
    public boolean requireKeyFrame(long now, OriginalTrace gps, OriginalTrace gyro) {
        boolean init = false;
        if (timestamp_ == -1) {
            init = true;
        } else {
            double duration = (double)(now - this.timestamp_) / 1000.0;
            this.distance_ += this.gps_.values[2] * duration;
            this.degree_ += this.getAngle(this.gyro_) * duration;
        }
        this.timestamp_ = now;
        this.gps_ = gps;
        this.gyro_ = gyro;
        if (init || this.distance_ >= threshold_distance || this.degree_ >= threshold_angle || now - this.lastIFrameTime >= threshold_time) {
            this.distance_ = 0.0;
            this.degree_ = 0.0;
            this.lastIFrameTime = now;
            return true;
        } else {
            return false;
        }
    }

    private double getAngle(OriginalTrace gyro) {
        double sum = 0.0;
        for (int i = 0; i < gyro.values.length; ++i) {
            sum += Math.pow(gyro.values[i], 2.0);
        }
        return Math.sqrt(sum);
    }

}
