package ch.wingo.pris;

import java.util.Date;
import java.util.HashMap;

public class WebSocketsCentralisation {
	// singleton of websocketcentralisation
	private static final WebSocketsCentralisation INSTANCE = new WebSocketsCentralisation();
	// HashMap regrouping all the sessions, identifiable by the MAC Addr
	private HashMap<String, RemoteSTBSocket> members = new HashMap<String, RemoteSTBSocket>();
	// Constant value if no user is found in members
	private static final String USER_NOT_FOUND = "{\"error\":\"user not found\"}";
	
	public static WebSocketsCentralisation getInstance(){
		return INSTANCE;
	}
	
	// Join: Add the session to the HashTable
	public void join(RemoteSTBSocket socket){
		System.out.println(new Date()+" New socket connexion added: "+socket.getMacAddr());
		// adding the socket to the hashtable
		members.put(socket.getMacAddr(), socket);
		// printing the connected boxes
		for(RemoteSTBSocket sockets:members.values()){
			System.out.println(new Date()+" Is connected: "+sockets.getMacAddr());
		}
	}
	
	// Leave: Remove the session from the HashTable
	public void leave(RemoteSTBSocket socket){
		System.out.println(new Date()+" Socket connexion removed: "+socket.getMacAddr());
		// removing the socket from the hashtable
		members.remove(socket.getMacAddr());
		// printing the connected boxes
		for(RemoteSTBSocket sockets:members.values()){
			System.out.println(new Date()+" Is connected: "+sockets.getMacAddr());
		}
	}
	
	// Will ask the correct Session to iperf
	public String iperf(String macAddr, String cmd) {
		// if the session exists
		if(members.containsKey(macAddr))
			// returning the iperf value from the session
			return members.get(macAddr).iperf(cmd);
		// else returning error message
		else return USER_NOT_FOUND;
	}
	
	// Will ask the correct Session to reboot
	public void reboot(String macAddr) {
		if(members.containsKey(macAddr))
			members.get(macAddr).reboot();
	}
	
	// Will ask the correct Session to tell if mute
	public String isMute(String macAddr) {
		if(members.containsKey(macAddr))
			return members.get(macAddr).isMute();
		else return USER_NOT_FOUND;
	}
	
	// Will ask the correct Session to ping
	public String ping(String macAddr, String cmd) {
		if(members.containsKey(macAddr))
			return members.get(macAddr).ping(cmd);
		else return USER_NOT_FOUND;
	}
}
