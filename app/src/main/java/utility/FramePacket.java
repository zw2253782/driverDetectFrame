package utility;

import java.io.Serializable;


public class FramePacket implements Serializable {
    private static String TAG = FrameData.class.getSimpleName();
    
	public long packetSendTime = 0;
	public long frameSequence = 0; // uniquely identify a frame
	public int packetLength = 0;
	public int k = 0;
	public int n = 0;
	public int index = 0; // 0 ... n - 1

	public FramePacket() {
		
	}
	
	public FramePacket(long sendTime, long frameSequence, int len, int k, int n, int index) {
		this.packetSendTime = sendTime;
		this.frameSequence = frameSequence;
		this.packetLength = len;
		this.k = k;
		this.n = n;
		this.index = index;
	}
}
