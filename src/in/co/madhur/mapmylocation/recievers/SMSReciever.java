package in.co.madhur.mapmylocation.recievers;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.sms.SMSHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;

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
					abortBroadcast();
					break;
					
				}
			}
			
			SMSHandler.HandleIncomingSMS(context, message, sender);
		}
		
	}
		

}
