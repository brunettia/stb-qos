package ch.wingo.stb.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import ch.wingo.stb.STBContext;

public class NetworkUtils {
	
	private static final String TAG = "ch.wingo.stb.utils.NetworkUtils";

	public static String getActiveInterface(Context context){
		// getting the connectiviy manager from the context
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// each interface has a 
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
			// getting all the interfaces
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			
			for (NetworkInterface intf : interfaces) {
				if (interfaceName != null) {
					// checking that the interface given exists
					if (!intf.getName().equalsIgnoreCase(interfaceName))
						continue;
				}
				// getting the mac address on byte array format
				byte[] mac = intf.getHardwareAddress();
				if (mac == null)
					return "";
				StringBuilder buf = new StringBuilder();
				// converting byte array to readable String
				for (int idx = 0; idx < mac.length; idx++)
					buf.append(String.format("%02X:", mac[idx]));
				if (buf.length() > 0)
					buf.deleteCharAt(buf.length() - 1);
				return buf.toString();
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
		}
		return "";
	}
	
	//If connected to Wifi, return the gateway ip addr
	public static String getWifiDefaultGateway(){
		// getting the WifiManager from context
		WifiManager wifi = (WifiManager) STBContext.getAppContext().getSystemService(Context.WIFI_SERVICE);
		// returning the gateway converted to String, readable like an ip address
		return intToIp(wifi.getDhcpInfo().gateway);
	}
	
	// Convert int format to string ip
	public static String intToIp(int addr) {
		return ((addr & 0xFF) + "." + ((addr >>>= 8) & 0xFF) + "."
				+ ((addr >>>= 8) & 0xFF) + "." + ((addr >>>= 8) & 0xFF));
	}
}
