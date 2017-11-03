package utility;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class FrameData implements Serializable {
    private static String TAG = FrameData.class.getSimpleName();

    public String type;
    private long videoSendTime;
    public long Sequence;
    public long roundLatency = 0;
    public boolean isIFrame = false;
    public long originalDataSize = 0;
    public long compressedDataSize = 0;
    public long PCtime = 0;

    public byte[] rawFrameData = null;
    public static long sequence = 0;

    public int splitTotal = 0; // 0 means no split
    public int splitIndex = 0; // index should be no larger than splitTotal

    public FrameData (boolean isIFrame, byte[] data, int originalSize){
        this.type = "frame_data_from_car";
        this.videoSendTime = System.currentTimeMillis();
        this.isIFrame = isIFrame;

        this.rawFrameData = data;
        // copy the first 65000
        this.compressedDataSize = this.rawFrameData.length;
        this.originalDataSize = originalSize;
        this.Sequence = FrameData.sequence ++;
    }

    public double getCompressRatio () {
        return this.compressedDataSize * 100.0/this.originalDataSize;
    }
    public int getDataSize() {
        return rawFrameData.length;
    }


    public long getVideoSendTime() {
        return this.videoSendTime;
    }

    public void setSplitParams(int cnt, int index) {
        this.splitTotal = cnt;
        this.splitIndex = index;
    }

    public List<FrameData> split() {
        List<FrameData> res = new ArrayList<FrameData>();
        int len = 60000;
        splitTotal = rawFrameData.length/len + (int)(rawFrameData.length%len == 0 ? 0 : 1);
        double ratio = this.getCompressRatio();
        int totalLen = this.rawFrameData.length;
        for (int i = 0; i < splitTotal; ++i) {
            int curLen = Math.min(totalLen - len * i, len);
            byte[] newData = new byte[curLen];
            System.arraycopy(this.rawFrameData, i * len, newData, 0, curLen);
            FrameData newFrame = new FrameData(this.isIFrame, newData, (int)(curLen / this.getCompressRatio()));
            newFrame.setSplitParams(this.splitTotal - 1, i);
            res.add(newFrame);
        }
        return res;
    }
}
