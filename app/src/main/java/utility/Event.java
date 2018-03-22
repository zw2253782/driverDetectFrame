package utility;

public class Event {
	public long start_;
	public long end_;
	public int start_index_;
	public int end_index_;
	public String type_;
	public static final String LANECHANGE = "lane";
	public static final String TURN = "turn";
	public static final String BRAKE = "brake";
	public static final String STOP = "stop";
	public static final String SIGNAL = "signal";

	public Event() {
		
	};
	
	public Event(long start, long end, int start_index, int end_index, String type) {
		this.start_ = start;
		this.end_ = end;
		this.start_index_ = start_index;
		this.end_index_ = end_index;
		this.type_ = type;
	};
	public Event(long start, long end, String type) {
		this.start_ = start;
		this.end_ = end;
		this.type_ = type;
	};

}
