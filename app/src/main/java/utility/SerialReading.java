package utility;

/**
 * Created by wei on 4/18/17.
 */

//make obj for sending HallData
public class SerialReading {
    public double speed_;
    public int rotation_;
    public long time_;

    public SerialReading(double speed, int rotation, long time){
        this.speed_ = speed;
        this.rotation_ = rotation;
        this.time_ = time;
    }
}