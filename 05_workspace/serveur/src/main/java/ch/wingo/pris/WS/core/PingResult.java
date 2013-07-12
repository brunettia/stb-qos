package ch.wingo.pris.WS.core;

public class PingResult{
	private String type;
	private String macAddr;
	private double time;
	private String unit;
	
	public PingResult(String type, String macAddr, double time, String unit){
		this.type = type;
		this.macAddr = macAddr;
		this.time = time;
		this.unit = unit;
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
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
