package ch.wingo.stb.receiver;

import ch.wingo.stb.service.AutoStartService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AutoStart extends BroadcastReceiver{
	private final String TAG="wingo.stb.qos.AutoStartBroadcastReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intentToService = new Intent(context, AutoStartService.class);
		context.startService(intentToService);
		Log.i(TAG, "Service launched from AutoStart");
	}	
}
