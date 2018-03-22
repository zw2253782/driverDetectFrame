package utility;

import android.util.Log;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Arrays;


public class FramePacket implements Serializable {
    private static String TAG = FrameData.class.getSimpleName();
    
	public long packetSendTime = 0;
	public long frameSequence = 0; // uniquely identify a frame
	public int packetLength = 0;
	public int k = 0;
	public int n = 0;
	public int index = 0; // 0 ... n - 1
	public byte[] data = null;
	public String type = "frame_data_from_car";
	//zw0321
	public long parketeventStart;
	public long parketeventEnd;
	public String parketeventType;

	public static final int requiredSpace = 500; // should be larger than the gson format itself
	public FramePacket(long eventStart, long eventEnd, String eventType, long sendTime, long frameSequence, int len, int k, int n, int index) {
		this.packetSendTime = sendTime;
		this.frameSequence = frameSequence;
		this.packetLength = len;
		this.k = k;
		this.n = n;
		this.index = index;
		this.data = new byte[len];
		//zw
		this.parketeventStart = eventStart;
		this.parketeventEnd = eventEnd;
		this.parketeventType = eventType;
	}

	public byte[] toBytePacket() {
		Gson gson = new Gson();
		byte [] body = this.data;
		this.data = null;
		byte [] header = gson.toJson(this).getBytes();
		byte [] headerPadding = new byte[FramePacket.requiredSpace - header.length];
		byte [] payload = new byte[FramePacket.requiredSpace + body.length];
		Arrays.fill(headerPadding, (byte)' ');
		System.arraycopy(header, 0, payload, 0, header.length);
		System.arraycopy(headerPadding, 0, payload, header.length, headerPadding.length);
		System.arraycopy(body, 0, payload, FramePacket.requiredSpace, body.length);
		return payload;
	}

}
