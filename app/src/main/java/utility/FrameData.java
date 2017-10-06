package utility;


public class FrameData {

    public long videoSendTime;
    public long Sequence;
    public long roundLatency = 0;
    public byte[] frameData = null;
    public boolean isIFrame = false;
    public long originalDataSize = 0;
    public long compressedDataSize = 0;
    public long PCtime = 0;

    private int subIndex = 0;
    private int subSum = 0;

    public FrameData(){
    }

    public FrameData (boolean isIFrame, byte[] data){
        this.videoSendTime = System.currentTimeMillis();
        this.isIFrame = isIFrame;
        this.frameData = data;
        this.compressedDataSize = data.length;
    }

    public long getDataSize() {
        return frameData.length;
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
