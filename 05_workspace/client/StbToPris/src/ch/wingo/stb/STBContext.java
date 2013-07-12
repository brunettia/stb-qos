package ch.wingo.stb;

import android.app.Application;
import android.content.Context;
import android.util.Log;


public class STBContext extends Application{
	private static final String TAG = "ch.wingo.stb.STBContext";
	private static Context context;

    public void onCreate(){
    	Log.d(TAG, "STBContext initilized");
        super.onCreate();
        STBContext.context = getApplicationContext();
        Log.d(TAG, "Context: "+context.toString());
    }

    public static Context getAppContext() {
        return STBContext.context;
    }
}
