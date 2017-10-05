package utility;


import java.util.ArrayList;
import java.util.List;

public class FrameData {

    public long timeStamp_;
    public long SequenceNo_;
    public long latency_;
    public byte[] video_ = null;
    public boolean isIFrame = false;
    public long originalDataSize = 0;
    public long compressedDataSize = 0;

    private int subIndex = 0;
    private int subSum = 0;

    public FrameData(){
        //I set the length as 8 digit.
    }
    public FrameData(long order, byte[] video) {
        //I set the length as 8 digit.
        this.SequenceNo_ = order;
        //this.latency_ = latency;
        this.video_ = video;
    }

    public FrameData (boolean bool, byte[] data){
        this.timeStamp_ = System.currentTimeMillis();
        this.isIFrame = bool;
        this.video_ = data;
        this.compressedDataSize = data.length;
    }
    public FrameData(long timeStamp, long order, long latency, long dataLength) {
        this.timeStamp_ = timeStamp;
        //I set the length as 8 digit.
        this.SequenceNo_ = order;
        this.latency_ = latency;
        this.compressedDataSize = dataLength;
    }



    public long getDataSize() {
        return video_.length;
    }

/*    public FrameData generateSubFrame(int index) {
        FrameData frame = new FrameData();
// copy the data
        frame.subIndex = index;
        return frame;
    }

    public List<FrameData> split() {
        List<FrameData> res = new ArrayList<FrameData>();

        subSum = video_.length/5000 + (int)(video_.length%5000 == 0 ? 0 : 1);
        for (int i = 0; i < subSum; ++i) {
            byte[] newData = new byte[5000];
            System.arraycopy(this.video_, i * 5000, newData, 0, 5000);
            FrameData newFrame = this.generateSubFrame(i);
            res.add(newFrame);
        }
        return res;
    }*/
}
