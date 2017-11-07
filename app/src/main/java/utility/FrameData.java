package utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class FrameData implements Serializable {
    private static String TAG = FrameData.class.getSimpleName();

    public String type;
    private long frameSendTime;
    public long transmitSequence;
    public long roundLatency = 0;
    public boolean isIFrame = false;
    public long originalDataSize = 0;
    public long compressedDataSize = 0;
    public long serverTime = 0;

    public byte[] rawFrameData = null;
    public static long sequenceIndex = 0;

    public int splitTotal = 0; // 0 means no split
    public int splitIndex = 0; // index should be no larger than splitTotal

    public FrameData (boolean isIFrame, byte[] data, int originalSize){
        this.type = "frame_data_from_car";
        this.frameSendTime = System.currentTimeMillis();
        this.isIFrame = isIFrame;

        this.rawFrameData = data;
        // copy the first 65000
        this.compressedDataSize = this.rawFrameData.length;
        this.originalDataSize = originalSize;
        this.transmitSequence = FrameData.sequenceIndex++;

        double compressRatio = this.compressedDataSize * 100.0/this.originalDataSize;
        //Log.d(TAG, this.transmitSequence + ", I:" + this.isIFrame + "," + "ratio:" + String.format("%.2f", compressRatio) + ",size:" + this.compressedDataSize);
    }

    public int getDataSize() {
        return rawFrameData.length;
    }


    public long getFrameSendTime() {
        return this.frameSendTime;
    }

    public void setSplitParams(int cnt, int index) {
        this.splitTotal = cnt;
        this.splitIndex = index;
    }

    public List<FrameData> split() {
        List<FrameData> res = new ArrayList<FrameData>();
        int len = 60000;
        splitTotal = rawFrameData.length/len + (int)(rawFrameData.length%len == 0 ? 0 : 1);
        int totalLen = this.rawFrameData.length;
        for (int i = 0; i < splitTotal; ++i) {
            int curLen = Math.min(totalLen - len * i, len);
            byte[] newData = new byte[curLen];
            System.arraycopy(this.rawFrameData, i * len, newData, 0, curLen);
            FrameData newFrame = new FrameData(this.isIFrame, newData, this.rawFrameData.length);
            newFrame.setSplitParams(this.splitTotal - 1, i);
            res.add(newFrame);
        }
        return res;
    }
}
