package utility;


public class DataProcess {

    private long timeStamp_;
    private long SequenceNo_;
    private long latency_;
    private byte[] video_;
    private long dataLength_;

    public DataProcess(long order, byte[] video) {
        this.timeStamp_ = System.currentTimeMillis();
        //I set the length as 8 digit.
        this.SequenceNo_ = order;
        //this.latency_ = latency;
        this.video_ = video;
    }

    public DataProcess(long timeStamp, long order, long latency, long dataLength) {
        this.timeStamp_ = timeStamp;
        //I set the length as 8 digit.
        this.SequenceNo_ = order;
        this.latency_ = latency;
        this.dataLength_ = dataLength;
    }
}
