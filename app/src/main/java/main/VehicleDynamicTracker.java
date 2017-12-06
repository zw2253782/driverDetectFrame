package main;

import utility.Trace;

/**
 * Created by lkang on 12/4/17.
 */

public class VehicleDynamicTracker {
    private double distance_;
    private Trace gps_;
    private double degree_;
    private Trace gyro_ = null;
    private long timestamp_ = -1;
    private static double threshold_distance = 10.0;
    private static double threshold_angle = 0.15;
    public VehicleDynamicTracker () {
    }

    // check if a I frame is required
    public boolean requireKeyFrame(Trace gps, Trace gyro) {
        long now = System.currentTimeMillis();
        boolean init = false;
        if (timestamp_ == -1) {
            init = true;
        } else {
            long duration = now - this.timestamp_;
            this.distance_ += this.gps_.values[2] * duration;
            this.degree_ += this.gyro_.values[2] * duration;
        }
        this.timestamp_ = now;
        this.gps_ = gps;
        this.gyro_ = gyro;
        if (init || this.distance_ >= threshold_distance || this.degree_ >= threshold_angle) {
            this.distance_ = 0.0;
            this.degree_ = 0.0;
            return true;
        } else {
            return false;
        }
    }

}
