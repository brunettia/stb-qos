package ch.wingo.pris;


import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;

@WebSocket(maxMessageSize = 64 * 1024)
public class RemoteSTBSocket {
	private static final int SEND_KEEP_ALIVE_MS = 5000;
	private boolean firstConnection = true;
	private Session session;
	private String macAddr;
	private String pingResult = "";
	private String iperfResult = "";
	private String isMuteResult = "";
	private Timer keepAliveTimer = new Timer();
	
	
	
	@OnWebSocketMessage
    public void onText(Session session, String message) {
        if (session.isOpen()) {
        	if(firstConnection && isMacAddr(message)){
        		System.out.println(new Date()+" First message: should be mac addr: "+message);
        		firstConnection = false;
        		this.macAddr = message;
        		WebSocketsCentralisation.getInstance().join(this);
        		return;
        	}
        	System.out.println(new Date()+" Message received: "+message);
        	if(message.equals("hello")){
        		return;
        	}
        	
        	ObjectMapper mapper = new ObjectMapper(); // mapper to convert a String JSon to a map
			Map<String, Object> map; // map containing the result json in format key/value
			try {
				// converting the String json to a map
				map = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
				// getting the type of command
				String type = (String) map.get("type");
				if (type.equals("ping")) {
					System.out.println(new Date()+" Socket: ping result received");
					// updating pingResult with message, the result.
					// the sleeping thread will now awake!
					pingResult = message;
				} else if(type.equals("iperf")){
					System.out.println(new Date()+" Socket: iperf result received");
					// updating iperfResult with message, the result.
					// the sleeping thread will now awake!
					iperfResult = message;
				} else if(type.equals("isMute")){
					// updating isMuteResult with message, the result.
					// the sleeping thread will now awake!
					System.out.println(new Date()+" Socket: isMute result received");
					isMuteResult = message;
				} else if(type.equals("error")){
					String cmd = (String) map.get("command");
					// error during iperf
					if(cmd.equals("iperf")){
						iperfResult = message;
					}
				}
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
	
	
	@OnWebSocketConnect
	public void onConnect(Session session){
		System.out.println(new Date()+" Device connected: "+session.getRemoteAddress().getHostString());
		keepAliveTimer.schedule(new KeepAliveTask(), 0, SEND_KEEP_ALIVE_MS);
		this.session = session;
	}
	
	@OnWebSocketClose
	public void onClose(Session session, int closeCode, String closeReason){
			try {
				System.out.println( new Date()+" connexion closed. Reason: "+closeReason);
				keepAliveTimer.cancel();
				if(session.isOpen())
					session.disconnect();
				WebSocketsCentralisation.getInstance().leave(this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	@OnWebSocketError
	public void onError(Session session, Throwable cause){
		System.out.println(new Date()+" ERROR: "+ cause.getMessage());
	}
	
	
	// sending iperf request to the stb
	// waiting for the result
	// called by WebSocketCentralisation
	public String iperf(String cmd) {
		String result = ""; // result sends back
		// send command to the stb via websocket
		session.getRemote().sendStringByFuture(cmd);
		try{
			System.out.println(new Date()+ " Gonna wait");
			// while the iperf result is empty
			while (iperfResult.equals("")) {
				// I sleep for 10 ms
				Thread.sleep(10);
			}
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		// if i'm out, i have a result! Copying the iperResult
		result = iperfResult;
		// reinitialising iperfResult for next time
		iperfResult = "";
		System.out.println(new Date()+" No more sleeping, gonna send: "+result);
		// returning the result to WebSocketCentralisation
		return result;
	}

	public void reboot() {
		session.getRemote().sendStringByFuture("reboot");
	}
	
	// Task for timer to send hello messages to maintain 
	// the connexion
	private class KeepAliveTask extends TimerTask{
		
		@Override
		public void run() {
			session.getRemote().sendStringByFuture("hello");
		}
	}
	
	private boolean isMacAddr(String message) {
		Pattern p = Pattern.compile("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$");
		Matcher m = p.matcher(message);
		return m.matches();
	}
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}


	public String isMute() {
		String result = "";
		session.getRemote().sendStringByFuture("isMute");
		try {
			System.out.println(new Date()+" Gonna wait");
			while (isMuteResult.equals("")) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result = isMuteResult;
		isMuteResult="";
		System.out.println(new Date()+" No more sleeping, gonna send: "+result);
		return result;
	}

	
	public String ping(String cmd) {
		String result = "";
		session.getRemote().sendStringByFuture(cmd);
		try {
			System.out.println(new Date()+" Gonna wait");
			while (pingResult.equals("")) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result = pingResult;
		pingResult="";
		System.out.println(new Date()+" No more sleeping, gonna send: "+result);
		return result;
	}
}
