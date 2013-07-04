package ch.wingo.stb.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import ch.wingo.stb.R;
import ch.wingo.stb.STBContext;
import ch.wingo.stb.core.IperfResult;
import ch.wingo.stb.core.IsMuteResult;
import ch.wingo.stb.core.PingResult;
import ch.wingo.stb.utils.NetworkUtils;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

@SuppressLint("NewApi")
public class AutoStartService extends Service {
	private final String TAG = "wingo.stb.qos.AutoStartService";
	
	// Constantes
	private final int MIN_TO_WAIT = 9000;
	private final int MAX_TO_WAIT = 11000;
	private final int HOUR_TO_MS = 3600000;
	
	private final WebSocketConnection mConnection = new WebSocketConnection();
	private String macAddr = NetworkUtils.getMACAddr(NetworkUtils.getActiveInterface(STBContext.getAppContext()));
	
	// used for reconnection to Pris
	private int totalWaitTime = 0;
	private boolean goConnect = true;
	private Timer reconnectTimer = new Timer();
	private Random random = new Random();
	private Timer timeWithoutMessage = new Timer();
	private TimerTask tt = createTimeWithoutMessageTask();

	
	// used for ping
	private double min = 99999;
	private double max = 0;
	private double avg = 0;
	private double stddev = 0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// When the service is launched. Will establish the connection
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "Service onStart, little sleep and connection to the server");
		try {
			Thread.sleep(1000);
			reinitializeReconnection();
			connectToServer();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void connectToServer() {
		
		try {
			
			mConnection.connect(getString(R.string.server_addr), new WebSocketHandler(){
				
				// connection au serveur
				@Override
				public void onOpen(){
					Log.d(TAG, "Connected");
					Log.d(TAG, "First connexion, sending MAC @");
					Log.d(TAG, "My MAC Addr: "+ macAddr);
					mConnection.sendTextMessage(macAddr);
				}
				// reception d'un message text
				@Override
		         public void onTextMessage(String payload) {
		            Log.d(TAG, "Message received: "+payload);
					if(payload.equals("ping")){
						ObjectMapper mapper = new ObjectMapper();
						String json ="";
						try {
							Log.d(TAG, "Ping request received");
							json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ping());
							//json = mapper.writeValueAsString(ping());
							Log.d(TAG, "Ping done, result: "+json);
						} catch (JsonGenerationException e) {
							Log.e(TAG, e.toString());
						} catch (JsonMappingException e) {
							Log.e(TAG, e.toString());
						} catch (IOException e) {
							Log.e(TAG, e.toString());
						}
						mConnection.sendTextMessage(json);
					}
					else if(payload.equals("reboot")){
						Log.d(TAG, "Reboot request received");
						reboot();
					}
					else if(payload.equals("iperf")){
						Log.d(TAG, "Iperf request received");
						ObjectMapper mapper = new ObjectMapper();
						String json = "";
						try {
							IperfResult ir = iperf();
							if(ir != null){
								json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ir);
							}else {
								json = "Error during iperf. Try to relaunch the command";
							}
							
						} catch (JsonGenerationException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mConnection.sendTextMessage(json);
					}else if(payload.equals("isMute")){
						Log.d(TAG, "IsMute request received");
						ObjectMapper mapper = new ObjectMapper();
						String json = "";
						try {
							json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(isMute());
							
						} catch (JsonGenerationException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mConnection.sendTextMessage(json);
					}
					else if(payload.equals("hello")){
						tt.cancel();
						timeWithoutMessage.purge();
						mConnection.sendTextMessage(payload);
						tt = createTimeWithoutMessageTask();
						timeWithoutMessage.schedule(tt, 65000);
					}
		         }
				
				// fermeture de la connexion
		         @Override
		         public void onClose(int code, String reason) {
		            Log.d(TAG, "Connection lost. "+reason+" error code : "+code);
		            if(code<4000){
		            	reconnectToServer();
		            }
		         }
			});
		} catch (WebSocketException e) {
			
			Log.e(TAG, "Error on connect: "+e.toString());
			Log.d(TAG, "is connected: "+mConnection.isConnected());
			
		} catch (Exception e){
			Log.d(TAG, e.getLocalizedMessage());
		}
		
	}
	
	private TimerTask createTimeWithoutMessageTask(){
		return new TimerTask() {
			
			@Override
			public void run() {
				Log.d(TAG, "Waiting for a minute, trying de reconnect");
				mConnection.disconnect();
				reconnectToServer();
			}
		};
	}
	private void reconnectToServer() {
		try {
			if(goConnect){
				goConnect = false;
				Thread.sleep(1000);
				Log.d(TAG, "DISCONNECT:");
				// mConnection.disconnect();
				Log.d(TAG, "ReconnectTimer Launched");
				new ReconnectTask().run();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void reboot() {
		
		((PowerManager)STBContext.getAppContext().getSystemService(Context.POWER_SERVICE)).reboot(null);
	}
	// Test implementation ping from http://learn-it-stuff.blogspot.ch/
	private PingResult ping() {
		PingResult pp = null;
		try {
			String pingCmd = "ping -c 1 " + R.string.server_addr_ip;
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(pingCmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String inputLine;
			String[] roundtripArray = null;
			String[] pingResult = null;
			String[] pingStat = null;
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.matches("[\\d\\sa-z:_=.]+")){
					Pattern pattern = Pattern.compile("[\\sa-z:_=]+");
					pingResult = pattern.split(inputLine);
				}
				else if(inputLine.matches("[a-z-/\\s=\\d\\.\\d]+")){
					Pattern pattern = Pattern.compile("[a-z-/\\s=]+");
					roundtripArray = pattern.split(inputLine);
				}
				else if(inputLine.matches("[\\w\\s,.%]+")){
					Pattern pattern = Pattern.compile("[\\sa-z,%]+");
					pingStat = pattern.split(inputLine);
				}
			}
			in.close();
			
			// calculate ping avg
			double rt_avg = Double.parseDouble(roundtripArray[2]);
			avg = (avg == 0) ? rt_avg : (avg+rt_avg)/2;
			double factor = 1e3;
			double avg_rounded = Math.round(avg * factor) / factor;
			
			// search min
			double rt_min  = Double.parseDouble(roundtripArray[1]);
			min = rt_min < min ? rt_min : min;
			
			// search max
			double rt_max = Double.parseDouble(roundtripArray[3]);
			max = rt_max > max ? rt_max : max;
			
			pp = new PingResult("ping",macAddr,
					Double.parseDouble(pingResult[4]),"ms", 
					Integer.parseInt(pingResult[0]),"bytes", 
					pingResult[1], Integer.parseInt(pingResult[2]), Integer.parseInt(pingResult[3]),
					min,avg_rounded,max,stddev,
					Integer.parseInt(pingStat[0]),Integer.parseInt(pingStat[1]), Double.parseDouble(pingStat[2]));
		}
		
		catch (IOException e) {
		Log.e(TAG, e.toString());
		}
		return pp;
	}
	
	
	Process process = null;
	private IperfResult iperf() {
		InputStream in;
		try {
			//The asset "iperf" (from assets folder) inside the activity is opened for reading.
			in = getResources().getAssets().open("iperf");
		} catch (IOException e2) {
			Log.e(TAG, e2.toString());
			//tv.append("\nError occurred while accessing system resources, please reboot and try again.");
			return null;			
		}
		try {
			//Checks if the file already exists, if not copies it.
			InputStream is = new FileInputStream("/data/data/ch.wingo.stb/iperf");
			is.close();
		} catch (FileNotFoundException e1) {
			try {
				Log.e(TAG, e1.toString());
				//The file named "iperf" is created in a system designated folder for this application.
				OutputStream out = new FileOutputStream("/data/data/ch.wingo.stb/iperf", false); 
				// Transfer bytes from "in" to "out"
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				//After the copy operation is finished, we give execute permissions to the "iperf" executable using shell commands.
				Process processChmod = Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/ch.wingo.stb/iperf"); 
				// Executes the command and waits until it finishes.
				processChmod.waitFor();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} catch (InterruptedException e) {
				Log.e(TAG, e.toString());
			}		
			//Creates an instance of the class IperfTask for running an iperf test, then executes.
			return execIperf();				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
		} 
		//Creates an instance of the class IperfTask for running an iperf test, then executes.
		return execIperf();
		
	}

	private IperfResult execIperf() {
		IperfResult iperfResult = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("iperf -c "+getString(R.string.server_addr_ip)+" -i 10");
			// A buffered output of the stdout is being initialized so the iperf
			// output could be displayed on the screen.
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String str1="";
			String[] arrayOfString = null;
			while ((str1 = reader.readLine()) != null) {
				Log.d(TAG, "Entering iperf result process");
				if (str1.matches("\\[[ \\d]+\\]\\s*[\\d]+.*")) {
					Log.d(TAG, "We got a match, filtering...");
					Pattern localPattern = Pattern.compile("[-\\[\\]\\s]+");
					arrayOfString = localPattern.split(str1);
					break;
				}
			}
			
			reader.close();
			//process.destroy();
			Log.d(TAG, "Iperf result: "+arrayOfString.toString());
			double throughput = Double.parseDouble(arrayOfString[7]);
			String unit = arrayOfString[8];
			iperfResult = new IperfResult("iperf",macAddr, throughput, unit);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
		return iperfResult;
	}
	
	private IsMuteResult isMute(){
		AudioManager audioManager = (AudioManager)STBContext.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		boolean isMute = audioManager.getStreamVolume(3) == 0 ? true : false;
		return new IsMuteResult(macAddr, isMute, "isMute");
	}
	
	private void reinitializeReconnection(){
		timeWithoutMessage.purge();
		reconnectTimer.purge();
		goConnect = true;
		totalWaitTime = 0;
	}
	private class ReconnectTask extends TimerTask{
		@Override
		public void run() {
			try{
				if(totalWaitTime<HOUR_TO_MS){
					if(!mConnection.isConnected()){
						int waitTime= random.nextInt(MAX_TO_WAIT - MIN_TO_WAIT + 1) + MIN_TO_WAIT;
						Log.d(TAG, "Next tentative to connect in "+waitTime+" ms");
						totalWaitTime +=waitTime;
						reconnectTimer.schedule(new ReconnectTask(), waitTime);
						connectToServer();
					}else{
						Log.d(TAG, "Connected to the server again");
						reinitializeReconnection();
					}
				}else throw new InterruptedException("Attempt to connect to the server during 1 hours without success");
			}catch(InterruptedException e){
				Log.d(TAG, e.getMessage());
			}
			
		}
		
	}
}
