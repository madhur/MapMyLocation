package in.co.madhur.mapmylocation.sms;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationGetter;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.service.SMSService;
import in.co.madhur.mapmylocation.tasks.LocationTask;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

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
			Log.v(App.TAG, "Track me not enabled, returning");
			return;
		}
		
		reqInfo=contactExists(context, sender);
		if(appPreferences.isOnlyAllowContacts())
		{
			if(reqInfo==null)
			{
				Log.v(App.TAG, "Sender not in the contacts, returning");
				return;
			}
			else
				reqInfo=new RequesterInfo(sender, sender, sender);
			
		}
		else if(reqInfo==null)
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
			
			
			ComponentName startedService=context.startService(smsService);
			
		//	Log.v(App.TAG, startedService.getShortClassName());
			
			// Log.v(App.TAG, "service started");
		
	
		
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
