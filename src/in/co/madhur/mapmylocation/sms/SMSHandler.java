package in.co.madhur.mapmylocation.sms;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.service.SMSService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.widget.Toast;

import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;

public class SMSHandler
{
	static Preferences appPreferences;

	
	public static void HandleIncomingSMS(Context context, String message, String sender)
	{
		appPreferences=new Preferences(context);
		boolean showNotification;
		RequesterInfo reqInfo = null;
		
		if(!appPreferences.isTrackMeEnabled())
		{
			if(LOCAL_LOGV)
				Log.v(App.TAG, "Track me not enabled, returning");
			// Not a possible condition since reciver is disabled if track me is disabled
			
			return;
		}
		
		
		reqInfo=contactExists(context, sender);
		if(appPreferences.isOnlyAllowContacts() && reqInfo==null)
		{
				if(LOCAL_LOGV)
					Log.v(App.TAG, "Sender not in the contacts, returning");
				Toast.makeText(context, context.getString(R.string.noti_sender_notin_contacts), Toast.LENGTH_SHORT).show();
				return;
			
		}
		
		if(reqInfo==null)
			reqInfo=new RequesterInfo(sender, sender, sender);
		
		showNotification=appPreferences.showTrackMeNotifications();
		ReplyToMessage(context, reqInfo, showNotification);
		
	}
	
	
	private static void ReplyToMessage(Context context, RequesterInfo reqInfo,  boolean showNotification)
	{
		
			Intent smsService=new Intent();
			Bundle data=new Bundle();
			
			data.putString("sender", reqInfo.RequesterNumber);
			data.putBoolean("showNotification", showNotification);
			data.putString("dispSender", reqInfo.RequesterName);					
			data.putString("msgStamp", reqInfo.RequesterID);

			smsService.putExtras(data);
			smsService.setClass(context.getApplicationContext(), SMSService.class);
			
			context.startService(smsService);
		
	}


	private static RequesterInfo contactExists(Context context, String number) 
	{
		// / number is the phone number
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
		String[] mPhoneNumberProjection = { PhoneLookup._ID,PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
		try
		{
			if (cur.moveToFirst())
			{
				if(LOCAL_LOGV)
					Log.v(App.TAG, "Contact matched");
				return new RequesterInfo(cur.getString(0), cur.getString(1), cur.getString(2));
				
			}
		}
		finally
		{
			if (cur != null)
				cur.close();
		}
		return null;
	}
	
	

	
}
