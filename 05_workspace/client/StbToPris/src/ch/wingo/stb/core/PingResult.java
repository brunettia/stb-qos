package ch.wingo.stb.core;

public class PingResult{
	private String type;
	private String macAddr;
	private int data_size;
	private String data_size_unit;
	private String ip_dest;
	private int icmp_seq;
	private int ttl;
	private double time;
	private String time_unit;
	private double round_trip_min;
	private double round_trip_avg;
	private double round_trip_max;
	private double round_trip_stddev;
	private int packet_transmitted;
	private int packet_received;
	private double packet_loss;
	
	public int getPacket_transmitted() {
		return packet_transmitted;
	}
	public void setPacket_transmitted(int packet_transmitted) {
		this.packet_transmitted = packet_transmitted;
	}
	public int getPacket_received() {
		return packet_received;
	}
	public void setPacket_received(int packet_received) {
		this.packet_received = packet_received;
	}
	public double getPacket_loss() {
		return packet_loss;
	}
	public void setPacket_loss(double packet_loss) {
		this.packet_loss = packet_loss;
	}
	public PingResult(String type, String macAddr, double time, 
			String time_unit, int data_size, 
			String data_size_unit, String ip_dest, int icmp_seq,
			int ttl, double round_trip_min, 
			double round_trip_avg, double round_trip_max, double round_trip_stddev,
			int packet_transmitted, int packet_received, double packet_loss){
		
		this.type = type;
		this.macAddr = macAddr;
		this.time = time;
		this.time_unit = time_unit;
		this.data_size = data_size;
		this.data_size_unit = data_size_unit;
		this.ip_dest = ip_dest;
		this.icmp_seq = icmp_seq;
		this.ttl = ttl;
		this.round_trip_avg = round_trip_avg;
		this.round_trip_max = round_trip_max;
		this.round_trip_min = round_trip_min;
		this.round_trip_stddev = round_trip_stddev;
		this.packet_transmitted = packet_transmitted;
		this.packet_received = packet_received;
		this.packet_loss = packet_loss;
		
	}
	public PingResult(){}
	
	public String getMacAddr() {
		return macAddr;
	}
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public int getData_size() {
		return data_size;
	}
	public void setData_size(int data_size) {
		this.data_size = data_size;
	}
	public String getData_size_unit() {
		return data_size_unit;
	}
	public void setData_size_unit(String data_size_unit) {
		this.data_size_unit = data_size_unit;
	}
	public String getIp_dest() {
		return ip_dest;
	}
	public void setIp_dest(String ip_dest) {
		this.ip_dest = ip_dest;
	}
	public int getIcmp_seq() {
		return icmp_seq;
	}
	public void setIcmp_seq(int icmp_seq) {
		this.icmp_seq = icmp_seq;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public String getTime_unit() {
		return time_unit;
	}
	public void setTime_unit(String time_unit) {
		this.time_unit = time_unit;
	}
	public double getRound_trip_min() {
		return round_trip_min;
	}
	public void setRound_trip_min(double round_trip_min) {
		this.round_trip_min = round_trip_min;
	}
	public double getRound_trip_avg() {
		return round_trip_avg;
	}
	public void setRound_trip_avg(double round_trip_avg) {
		this.round_trip_avg = round_trip_avg;
	}
	public double getRound_trip_max() {
		return round_trip_max;
	}
	public void setRound_trip_max(double round_trip_max) {
		this.round_trip_max = round_trip_max;
	}
	public double getRound_trip_stddev() {
		return round_trip_stddev;
	}
	public void setRound_trip_stddev(double round_trip_stddev) {
		this.round_trip_stddev = round_trip_stddev;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
