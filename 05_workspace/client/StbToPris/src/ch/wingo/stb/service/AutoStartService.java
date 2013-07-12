package ch.wingo.stb.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;

import ch.wingo.stb.R;
import ch.wingo.stb.STBContext;
import ch.wingo.stb.core.IperfResult;
import ch.wingo.stb.core.IsMuteResult;
import ch.wingo.stb.core.PingResult;
import ch.wingo.stb.utils.NetworkUtils;
import de.tavendo.autobahn.WebSocket.ConnectionHandler;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

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
	private final int WAIT_WITHOUT_MESSAGE = 65000;
		
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
	
//	private void testIperf() {
//
//		Process process = null;
//		try {
//			String cmd = "iperf -c " + getString(R.string.server_addr_ip)
//					+ " -i 10";
//			String[] commands = cmd.split(" ");
//			List<String> commandList = new ArrayList<String>(
//					Arrays.asList(commands));
//			// If the first parameter is "iperf", it is removed
//			if (commandList.get(0).equals((String) "iperf")) {
//				commandList.remove(0);
//			}
//			// The execution command is added first in the list for the shell
//			// interface.
//			commandList.add(0, getString(R.string.iperf_data_folder));
//			// The process is now being run with the verified parameters.
//			process = new ProcessBuilder().command(commandList).start();
//			// A buffered output of the stdout is being initialized so the iperf
//			// output could be displayed on the screen.
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					process.getInputStream()));
//			String str1 = "";
//			String iperfResult = "";
//
//			while ((str1 = reader.readLine()) != null) {
//				iperfResult += str1+"\n";
//			}
//			reader.close();
//			process.destroy();
//			Log.d(TAG, "Iperf result: \n" + iperfResult);
//		} catch (IOException e) {
//			Log.e(TAG, e.getMessage());
//		}
//		// return iperfResult;
//	}

	private void connectToServer() {
		
		try {
			
			mConnection.connect(getString(R.string.server_addr), new ConnectionHandler(){
				
				// when the connexion is established
				@Override
				public void onOpen(){
					Log.d(TAG, "Connected");
					Log.d(TAG, "First connexion, sending MAC @");
					Log.d(TAG, "My MAC Addr: "+ macAddr);
					mConnection.sendTextMessage(macAddr);
				}
				// Message reception
				@Override
		         public void onTextMessage(String payload) {
		            Log.d(TAG, "Message received: "+payload);
		            // if we have JSon
		            if(payload.startsWith("{")){
		            	ObjectMapper mapper = new ObjectMapper(); // mapper String Json to map
		            	Map<String, Object> map; // map key/value of the json recept
		            	try {
		            		// converting the string to map
							map = mapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
							// if we have a ping
							if(map.get("cmd").equals("ping")){
								// mapper Java Object to Json
								ObjectMapper toJson = new ObjectMapper();
								// result sent
								String json ="";
								try {
									Log.d(TAG, "Ping request received");
									// we get the options of the command
									String opt = (String) map.get("opt");
									// we launch the ping method. Return a PingResult instance, converted to json
									json = toJson.writerWithDefaultPrettyPrinter().writeValueAsString(ping(opt));
									Log.d(TAG, "Ping done, result: "+json);
								} catch (JsonGenerationException e) {
									Log.e(TAG, e.toString());
								} catch (JsonMappingException e) {
									Log.e(TAG, e.toString());
								} catch (IOException e) {
									Log.e(TAG, e.toString());
								}
								// sending the result to the server !
								mConnection.sendTextMessage(json);
							}
							// if we have an iperf
							else if(map.get("cmd").equals("iperf")){
								Log.d(TAG, "Iperf request received");
								// we get des options of the command
								String opt = (String) map.get("opt");
								// mapper Java Object to JSon
								ObjectMapper toJson = new ObjectMapper();
								// the result sent back
								String json = "";
								try {
									// IperResult instantiate feed!
									IperfResult ir = iperf(opt);
									// if everything was good:
									if(ir != null){
										json = toJson.writerWithDefaultPrettyPrinter().writeValueAsString(ir);
									}
									// else the iperf server was certainly down. Creating a new JSon.
									else {
										JSONObject job = new JSONObject();
										job.put("message", "Error during iperf. Check if iperf server is up!");
										job.put("type", "error");
										job.put("command", "iperf");
										// make it pretty, not in a single line
										json = job.toString(2);
									}
									
								} catch (JsonGenerationException e) {
									e.printStackTrace();
								} catch (JsonMappingException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} catch (JSONException e) {
									e.printStackTrace();
								}
								// sending back the result to the server
								mConnection.sendTextMessage(json);
							}
						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {							
							e.printStackTrace();
						}
		            }
					else if(payload.equals("reboot")){
						Log.d(TAG, "Reboot request received");
						reboot();
					}
					else if(payload.equals("isMute")){
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
						// cancelling the timer
						tt.cancel();
						timeWithoutMessage.purge();
						// sending hello back to the server
						mConnection.sendTextMessage(payload);
						// lauching the timer again
						tt = createTimeWithoutMessageTask();
						timeWithoutMessage.schedule(tt, WAIT_WITHOUT_MESSAGE);
					}
		         }
				
				// closing the connection
		         @Override
		         public void onClose(int code, String reason) {
		            Log.d(TAG, "Connection lost. "+reason+" error code : "+code);
		            // under 4000 it's managed by the library.
		            // we can custom our own codes if we want
		            if(code<4000){
		            	reconnectToServer();
		            }
		         }
				@Override
				public void onBinaryMessage(byte[] arg0) {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRawTextMessage(byte[] arg0) {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (WebSocketException e) {
			
			Log.e(TAG, "Error on connect: "+e.toString());
			Log.d(TAG, "is connected: "+mConnection.isConnected());
			
		} catch (Exception e){
			Log.d(TAG, e.getLocalizedMessage());
		}
		
	}
	
	// when de 65 secondes are done
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
			// boolean to check that the task is launched
			// only one time
			if(goConnect){
				goConnect = false;
				Thread.sleep(1000);
				Log.d(TAG, "ReconnectTimer Launched");
				// launching the Task
				new ReconnectTask().run();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	private void reboot() {
		
		((PowerManager)STBContext.getAppContext().getSystemService(Context.POWER_SERVICE)).reboot(null);
	}
	// Test implementation ping from http://learn-it-stuff.blogspot.ch/
	private PingResult ping(String opt) {
		PingResult pp = null;
		try {
			String pingCmd = "ping -c 1 -n " + opt;
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(pingCmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String inputLine;
			String[] roundtripArray = null;
			String[] pingResult = null;
			String[] pingStat = null;
			String logResult = "";
			while ((inputLine = in.readLine()) != null) {
				logResult+=inputLine+"\n";
				if(inputLine.matches("[\\d\\sa-z:_=.\\-()]+") && !inputLine.startsWith("-")){
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
			Log.d(TAG, logResult);
			in.close();
//			for(int i=0;i<pingResult.length;i++)
//				System.out.println(pingResult[i]);
//			for(int i=0;i<pingStat.length;i++)
//				System.out.println(pingStat[i]);
//			for(int i=0;i<roundtripArray.length;i++)
//				System.out.println(roundtripArray[i]);
			
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
	private IperfResult iperf(String opt) {
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
			InputStream is = new FileInputStream(getString(R.string.iperf_data_folder));
			is.close();
		} catch (FileNotFoundException e1) {
			try {
				Log.e(TAG, e1.toString());
				//The file named "iperf" is created in a system designated folder for this application.
				OutputStream out = new FileOutputStream(getString(R.string.iperf_data_folder), false); 
				// Transfer bytes from "in" to "out"
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				//After the copy operation is finished, we give execute permissions to the "iperf" executable using shell commands.
				Process processChmod = Runtime.getRuntime().exec(getString(R.string.iperf_chmod_744) +getString(R.string.iperf_data_folder)); 
				// Executes the command and waits until it finishes.
				processChmod.waitFor();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} catch (InterruptedException e) {
				Log.e(TAG, e.toString());
			}		
			//Creates an instance of the class IperfTask for running an iperf test, then executes.
			//testIperf();
			return execIperf(opt);				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
		} 
		//Creates an instance of the class IperfTask for running an iperf test, then executes.
		//testIperf();
		return execIperf(opt);
		
	}

	private IperfResult execIperf(String opt) {
		IperfResult iperfResult = null;
		try{
			String cmd = "iperf -c " + opt;
			String[] commands = cmd.split(" ");
			List<String> commandList = new ArrayList<String>(
					Arrays.asList(commands));
			// If the first parameter is "iperf", it is removed
			if (commandList.get(0).equals((String) "iperf")) {
				commandList.remove(0);
			}
			// The execution command is added first in the list for the shell
			// interface.
			commandList.add(0, getString(R.string.iperf_data_folder));
			// The process is now being run with the verified parameters.
			process = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();
			// A buffered output of the stdout is being initialized so the iperf
			// output could be displayed on the screen.
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String str1 = ""; // will contain each line
			String[] arrayOfString = null; // will contain the results
			while ((str1 = reader.readLine()) != null) {
				Log.d(TAG, "Entering iperf result process");
				Log.d(TAG, str1+"\n");
				// regex for a line with our result
				if (str1.matches("\\[[ \\d]+\\]\\s*[\\d]+.*")) {
					Log.d(TAG, "We got a match, filtering...");
					// regex for the between of each value
					Pattern localPattern = Pattern.compile("[-\\[\\]\\s]+");
					// we split the line with our pattern. We get the results
					arrayOfString = localPattern.split(str1);
					break;
				}
				// regex to catch if the server it down
				if (str1.matches("[a-zC:\\s]+")) {
					Log.d(TAG, "Connection refused - no server found");
					reader.close();
					process.destroy();
					return null;
				}
			}

			reader.close();
			process.destroy();
			double throughput = Double.parseDouble(arrayOfString[7]);
			String unit = arrayOfString[8];
			iperfResult = new IperfResult("iperf", macAddr, throughput, unit);
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
	
	// task launching one time, when connetion is lost
	private class ReconnectTask extends TimerTask{
		@Override
		public void run() {
			try{
				// if we don't wait for an hour
				if(totalWaitTime<HOUR_TO_MS){
					// if we are still deconnected
					if(!mConnection.isConnected()){
						// random waiting time between a min and max value
						int waitTime= random.nextInt(MAX_TO_WAIT - MIN_TO_WAIT + 1) + MIN_TO_WAIT;
						Log.d(TAG, "Next tentative to connect in "+waitTime+" ms");
						// calculate the total waiting time
						totalWaitTime +=waitTime;
						// schedule a new reconnection waitTime later
						reconnectTimer.schedule(new ReconnectTask(), waitTime);
						// trying to connect
						connectToServer();
					}else{
						// if the task was launched but we are connected now
						Log.d(TAG, "Connected to the server again");
						// reinitializing parameters for next interruption
						reinitializeReconnection();
					}
				}else throw new InterruptedException("Attempt to connect to the server during 1 hours without success");
			}catch(InterruptedException e){
				Log.d(TAG, e.getMessage());
			}
		}
		
	}
}
