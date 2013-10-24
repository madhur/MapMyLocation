package in.co.madhur.mapmylocation.recievers;

import in.co.madhur.mapmylocation.service.Alarms;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Alarms alarms=new Alarms(context);
		if(alarms.shouldSchedule())
		{
			alarms.Schedule();
		}
	}

}
