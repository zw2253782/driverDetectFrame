package utility;


import android.text.style.CharacterStyle;
import android.util.Base64;
import android.util.Log;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class FrameData implements Serializable {
    private static String TAG = FrameData.class.getSimpleName();

    private long videoSendTime;
    public long Sequence;
    public long roundLatency = 0;
    public boolean isIFrame = false;
    public long originalDataSize = 0;
    public long compressedDataSize = 0;
    public long PCtime = 0;

    public byte[] rawFrameData = null;

    private int subIndex = 0;
    private int subSum = 0;

    public FrameData(){
    }

    public FrameData (boolean isIFrame, byte[] data){
        this.videoSendTime = System.currentTimeMillis();
        this.isIFrame = isIFrame;
        this.compressedDataSize = data.length;

        this.rawFrameData = data;
        //Log.d(TAG, data.length + " convert to " + this.frameData.length());
    }

    public int getDataSize() {
        return rawFrameData.length;
    }


    public long getVideoSendTime() {
        return this.videoSendTime;
    }
    public void setVideoSendTime() {
        this.videoSendTime = System.currentTimeMillis();
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
