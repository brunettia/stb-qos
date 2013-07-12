package ch.wingo.stb.core;

public class IsMuteResult {
	private String macAddr;
	private boolean isMute;
	private String type;
	
	public IsMuteResult (String macAddr, boolean isMute, String type){
		this.macAddr = macAddr;
		this.isMute = isMute;
		this.type = type;
	}

	public String getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}

	public boolean isMute() {
		return isMute;
	}

	public void setMute(boolean isMute) {
		this.isMute = isMute;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
