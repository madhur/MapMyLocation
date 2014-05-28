package in.co.madhur.mapmylocation.service;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.tasks.LocationTask;
import in.co.madhur.mapmylocation.util.AppLog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;

public class SMSService extends WakefulIntentService
{
	public SMSService(String name)
	{
		super(name);
	}

	public SMSService()
	{
		super("SMSService");
	}

	private AppLog appLog;

	@Override
	public void onCreate()
	{
		super.onCreate();

		appLog = new AppLog(DateFormat.getDateFormatOrder(this));
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (LOCAL_LOGV)
			Log.v(App.TAG, "Service destroyed");

		if (appLog != null)
			appLog.close();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	protected void doWakefulWork(Intent intent)
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

		if (LOCAL_LOGV)
			Log.v(App.TAG, "Executing location task");
		appLog.append("Starting task to get location");

		new LocationTask(this, showNotification, sender, dispSender, uniqueId, appLog, new Preferences(this)).execute();

	}

}
