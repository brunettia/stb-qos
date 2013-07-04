package ch.wingo.stb.core;

public class IperfResult {
	private String type;
	private String macAddr;
	private double throughput;
	private String unit;
	
	public IperfResult(){}
	
	public IperfResult(String type, String macAddr, double throughput, String unit){
		this.type = type;
		this.macAddr = macAddr;
		this.throughput = throughput;
		this.unit = unit;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMacAddr() {
		return macAddr;
	}
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	public double getThroughput() {
		return throughput;
	}
	public void setThroughput(double throughput) {
		this.throughput = throughput;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
