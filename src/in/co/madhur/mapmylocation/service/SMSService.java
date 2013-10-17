package in.co.madhur.mapmylocation.service;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.tasks.LocationTask;
import in.co.madhur.mapmylocation.util.AppLog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

import static in.co.madhur.mapmylocation.App.LOG;
import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;


public class SMSService extends Service
{
	private AppLog appLog;

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		
		appLog=new AppLog(DateFormat.getDateFormatOrder(this));
		appLog.append("Starting service to get location");
		
		if(LOCAL_LOGV)
			Log.v(TAG, "Starting service to get location");
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Bundle data = intent.getExtras();
		boolean showNotification = data.getBoolean("showNotification");
		String sender = data.getString("sender");
		String msgStamp = data.getString("msgStamp");
		String dispSender = data.getString("dispSender");

		int uniqueId;
		try
		{
			uniqueId = Integer.valueOf(msgStamp);
		}
		catch (NumberFormatException e)
		{
			uniqueId = msgStamp.hashCode();
		}
		
		if(LOCAL_LOGV)
			Log.v(App.TAG, "Executing location task");
		appLog.append("Executing Location task");
		
		
		new LocationTask(this, showNotification, sender, dispSender, uniqueId)
				.execute(startId);

		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(LOCAL_LOGV)
			Log.v(App.TAG, "Service destroyed");
		
		appLog.append("Stopping service");
		
		if(appLog!=null)
			appLog.close();
	}
	
	public void log(Context context, String message)
	{
		Log.d(TAG, message);
		new AppLog(DateFormat.getDateFormatOrder(context))
				.appendAndClose(message);
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
