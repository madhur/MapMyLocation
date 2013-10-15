package in.co.madhur.mapmylocation.recievers;

import in.co.madhur.mapmylocation.App;
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

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle data=intent.getExtras();
		SmsMessage []messages=null;
		String sender = null, dispSender, dispMessage = null;
		String message = null;
		if(data!=null)
		{
			Object[] pdus=(Object[]) data.get("pdus");
			Log.v(App.TAG, "Sms Recieved");
			Log.v(App.TAG, String.valueOf(pdus.length));
			messages=new SmsMessage[pdus.length];
			
			for(int i=0;i<pdus.length; ++i)
			{				
				messages[i]=SmsMessage.createFromPdu((byte[]) pdus[i]);
				
				sender=messages[i].getOriginatingAddress();
				Log.v(App.TAG,sender);
				dispSender=messages[i].getDisplayOriginatingAddress();
				Log.v(App.TAG,dispSender);
				message=message+messages[i].getMessageBody();
				Log.v(App.TAG,message);
				dispMessage=dispMessage+messages[i].getDisplayMessageBody();
				Log.v(App.TAG,dispMessage);
			}
		}
		
		if(sender!=null && message!=null)
			SMSHandler.HandleIncomingSMS(context, message, sender);
				

	}
		

}
