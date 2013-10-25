package in.co.madhur.mapmylocation.recievers;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.sms.SMSHandler;
import in.co.madhur.mapmylocation.util.AppLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;

import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;

public class SMSReciever extends BroadcastReceiver
{
	Preferences appPreferences;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle data=intent.getExtras();
		SmsMessage []messages=null;
		String sender = "", dispSender = "", dispMessage = "";
		String message = "";
		appPreferences=new Preferences(context);
		long timeStamp = 0;
		
		
		if(data!=null)
		{
			Object[] pdus=(Object[]) data.get("pdus");
			
			if(LOCAL_LOGV)
				Log.v(App.TAG, "Sms Recieved pdu length: " + pdus.length);
			
			
						
			messages=new SmsMessage[pdus.length];
			
			for(int i=0;i<pdus.length; ++i)
			{				
				messages[i]=SmsMessage.createFromPdu((byte[]) pdus[i]);
				
				sender=messages[i].getOriginatingAddress();
				Log.v(App.TAG,sender);
				dispSender=messages[i].getDisplayOriginatingAddress();
				
				message=message+messages[i].getMessageBody();
				Log.v(App.TAG,message);
				dispMessage=dispMessage+messages[i].getDisplayMessageBody();
				
				
				if(!message.equalsIgnoreCase(appPreferences.getSecretCode()))
					return;
				else
				{
					log(context,"Hermes SMS Recieved");
					abortBroadcast();
					break;
					
				}
			}
			
			SMSHandler.HandleIncomingSMS(context, message, sender);
		}
		
	}
	
	private void log(Context context, String message)
	{
		Log.d(TAG, message);
			new AppLog(DateFormat.getDateFormatOrder(context))
					.appendAndClose(message);
	}

}
