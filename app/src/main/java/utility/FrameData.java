package utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import api.NativeClassAPI;


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

    public int rawFrameIndex = 0;
    public byte[] rawFrameData = null;
    public byte[] fecFrameData = null;
    public static long sequenceIndex = 0;

    public int splitTotal = 0; // 0 means no split
    public int splitIndex = 0; // index should be no larger than splitTotal

    public FrameData (int rawFrameIndex, boolean isIFrame, byte[] data, int originalSize){
        this.type = "frame_data_from_car";
        this.frameSendTime = System.currentTimeMillis();
        this.isIFrame = isIFrame;
        this.rawFrameIndex = rawFrameIndex;

        this.rawFrameData = data;
        // copy the first 65000
        this.compressedDataSize = this.rawFrameData.length;
        this.originalDataSize = originalSize;
        this.transmitSequence = FrameData.sequenceIndex++;

        double compressRatio = this.compressedDataSize * 100.0/this.originalDataSize;
    }

    public double getCompressRatio () {
        return this.compressedDataSize * 100.0/this.originalDataSize;
    }
    public int getDataSize() {
        return rawFrameData.length;
    }


    public long getFrameSendTime() {
        return this.frameSendTime;
    }


    public List<FramePacket> encodeToFramePackets(double lossRate) {
        List<FramePacket> packets = new ArrayList<FramePacket>();
        int sz = this.rawFrameData.length;
        final int referencePktSize = 2000;
        // minimize padding
        int needPadding = sz % referencePktSize == 0 ? 0 : 1;
        int k = sz / referencePktSize + needPadding;
        int blockSize = sz / k + needPadding;
        int n = (int) Math.round(k * (1.0 + lossRate * 5.0));

        this.fecFrameData = new byte[n * blockSize];
        byte[] padding = new byte[k * blockSize - sz];
        System.arraycopy(this.rawFrameData, 0, this.fecFrameData, 0, sz);
        System.arraycopy(padding, 0, this.fecFrameData, sz, k * blockSize - sz);

        for (int i = 0; i < k; ++i) {
            FramePacket packet = new FramePacket(this.frameSendTime, this.transmitSequence, blockSize, k, n, i);
            System.arraycopy(this.fecFrameData, i * blockSize, packet.data, 0, blockSize);
            packets.add(packet);
        }
        if (n == k) {
            return packets;
        }

        byte [] fec = NativeClassAPI.fecEncode(this.fecFrameData, blockSize, n - k);
        System.arraycopy(fec, 0, this.fecFrameData, k * blockSize, (n - k) * blockSize);
        for (int i = 0; i < n - k; ++i) {
            FramePacket packet = new FramePacket(this.frameSendTime, this.transmitSequence, blockSize, k, n, i + k);
            System.arraycopy(fec, i * blockSize, packet.data, 0, blockSize);
            packets.add(packet);
        }

        return packets;
    }
}
