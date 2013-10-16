package in.co.madhur.mapmylocation.service;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.tasks.LocationTask;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SMSService extends Service
{

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.v(App.TAG, "Service oncreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Bundle data=intent.getExtras();
		boolean showNotification=data.getBoolean("showNotification");
		String sender=data.getString("sender");
		String msgStamp= data.getString("msgStamp");
		String dispSender=data.getString("dispSender");
		
		int uniqueId;
		try
		{
		uniqueId=Integer.valueOf(msgStamp);
		}
		catch(NumberFormatException e)
		{
			uniqueId=msgStamp.hashCode();
		}
		
		Log.v(App.TAG, "Executing location task");
		new LocationTask(this, showNotification, sender, dispSender, uniqueId).execute(startId);
		
		return START_NOT_STICKY;
	}
	
	
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
