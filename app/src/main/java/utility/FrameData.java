package utility;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import api.NativeClassAPI;


public class FrameData implements Serializable {
    private static String TAG = FrameData.class.getSimpleName();

    public long frameSendTime;
    public long transmitSequence;
    public long roundLatency = 0;
    public boolean isIFrame = false;
    public long originalDataSize = 0;
    public long compressedDataSize = 0;
    public long serverTime = 0;

    public double lossRate = 0.0;
    public int N = 0;
    public int K = 0;

    public byte[] rawFrameData = null;
    public byte[] fecFrameData = null;
    public static long sequenceIndex = 0;
    public static final int referencePktSize = 2000;
    public FrameData (boolean isIFrame, byte[] data, int originalSize){
        this.frameSendTime = System.currentTimeMillis();
        this.isIFrame = isIFrame;
        this.rawFrameData = data;
        this.compressedDataSize = this.rawFrameData.length;
        this.originalDataSize = originalSize;
        this.transmitSequence = FrameData.sequenceIndex++;
    }
    public int getDataSize() {
        return rawFrameData.length;
    }

    public long getFrameSendTime() {
        return this.frameSendTime;
    }


    public List<FramePacket> encodeToFramePackets(double loss) {
        List<FramePacket> packets = new ArrayList<FramePacket>();

        int sz = this.rawFrameData.length;
        int needPadding = sz % referencePktSize == 0 ? 0 : 1;
        this.K = sz / referencePktSize + needPadding;
        this.N = (int) Math.round(this.K * (1.0 + loss * 5.0));

        int blockSize = sz / this.K + needPadding;
        this.fecFrameData = new byte[N * blockSize];
        byte[] padding = new byte[K * blockSize - sz];
        System.arraycopy(this.rawFrameData, 0, this.fecFrameData, 0, sz);
        System.arraycopy(padding, 0, this.fecFrameData, sz, K * blockSize - sz);

        for (int i = 0; i < K; ++i) {
            FramePacket packet = new FramePacket(this.frameSendTime, this.transmitSequence, blockSize, this.K, this.N, i);
            System.arraycopy(this.fecFrameData, i * blockSize, packet.data, 0, blockSize);
            packets.add(packet);
        }
        if (this.N == this.K) {
            return packets;
        }
        int extra = this.N - this.K;
        byte [] fec = NativeClassAPI.fecEncode(this.fecFrameData, blockSize, extra);
        System.arraycopy(fec, 0, this.fecFrameData, this.K * blockSize, extra * blockSize);
        for (int i = 0; i < extra; ++i) {
            FramePacket packet = new FramePacket(this.frameSendTime, this.transmitSequence, blockSize, this.K, this.N, i + this.K);
            System.arraycopy(fec, i * blockSize, packet.data, 0, blockSize);
            packets.add(packet);
        }

        return packets;
    }
}
