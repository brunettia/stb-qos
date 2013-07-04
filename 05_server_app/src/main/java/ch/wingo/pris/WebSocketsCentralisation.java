package ch.wingo.pris;

import java.util.Date;
import java.util.HashMap;

public class WebSocketsCentralisation {
	private static final WebSocketsCentralisation INSTANCE = new WebSocketsCentralisation();
	private HashMap<String, RemoteSTBSocket> members = new HashMap<String, RemoteSTBSocket>();
	
	public static WebSocketsCentralisation getInstance(){
		return INSTANCE;
	}
	
	public void join(RemoteSTBSocket socket){
		System.out.println(new Date()+" New socket connexion added: "+socket.getMacAddr());
		members.put(socket.getMacAddr(), socket);
		for(RemoteSTBSocket sockets:members.values()){
			System.out.println(new Date()+" Is connected: "+sockets.getMacAddr());
		}
	}
	
	public void leave(RemoteSTBSocket socket){
		System.out.println(new Date()+" Socket connexion removed: "+socket.getMacAddr());
		members.remove(socket.getMacAddr());
		for(RemoteSTBSocket sockets:members.values()){
			System.out.println(new Date()+" Is connected: "+sockets.getMacAddr());
		}
	}
	public String ping(String macAddr){
		return members.get(macAddr).ping();
	}

	public String iperf(String macAddr) {
		return members.get(macAddr).iperf();
	}

	public void reboot(String macAddr) {
		members.get(macAddr).reboot();
	}

	public String isMute(String macAddr) {
		return members.get(macAddr).isMute();
	}
}
