package ch.wingo.stb.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NetworkUtils {
	
	private static final String TAG = "ch.wingo.stb.utils.NetworkUtils";

	public static String getActiveInterface(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		switch (cm.getActiveNetworkInfo().getType()) {
		case ConnectivityManager.TYPE_ETHERNET:
			return "eth0";
		case ConnectivityManager.TYPE_WIFI:
			return "ra0";

		default:
			break;
		}
		return null;
	}
	
	// return the MAC address of the device
	@SuppressLint("NewApi")
	public static String getMACAddr(String interfaceName) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				if (interfaceName != null) {
					if (!intf.getName().equalsIgnoreCase(interfaceName))
						continue;
				}
				byte[] mac = intf.getHardwareAddress();
				if (mac == null)
					return "";
				StringBuilder buf = new StringBuilder();
				for (int idx = 0; idx < mac.length; idx++)
					buf.append(String.format("%02X:", mac[idx]));
				if (buf.length() > 0)
					buf.deleteCharAt(buf.length() - 1);
				return buf.toString();
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
		} // for now eat exceptions
		return "";
	}
	
	//If connected to Wifi, return the gateway ip addr
	public static String getWifiDefaultGateway(Context context){
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return intToIp(wifi.getDhcpInfo().gateway);
	}
	
	// Convert int format to string ip
	public static String intToIp(int addr) {
		return ((addr & 0xFF) + "." + ((addr >>>= 8) & 0xFF) + "."
				+ ((addr >>>= 8) & 0xFF) + "." + ((addr >>>= 8) & 0xFF));
	}
}
