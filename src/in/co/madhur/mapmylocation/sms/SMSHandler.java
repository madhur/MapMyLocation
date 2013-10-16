package in.co.madhur.mapmylocation.sms;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationGetter;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.tasks.LocationTask;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class SMSHandler
{
	static Preferences appPreferences;

	public static void HandleIncomingSMS(Context context, String message, String sender, long timeStamp)
	{
		appPreferences=new Preferences(context);
		boolean showNotification;
		
		if(!appPreferences.isTrackMeEnabled())
		{
			Log.v(App.TAG, "Track me not enabled, returning");
			return;
		}
		
		if(appPreferences.isOnlyAllowContacts())
		{
			boolean result=contactExists(context, sender);
			if(!result)
			{
				Log.v(App.TAG, "Sender not in the contacts, returning");
				return;
			}			
		}
		
		showNotification=appPreferences.showTrackMeNotifications();
		
		ReplyToMessage(context, sender, showNotification, timeStamp);
		
	}
	
	
	private static void ReplyToMessage(Context context, String sender, boolean showNotification, long timeStamp)
	{
		try
		{
			
			new LocationTask(context, showNotification, sender, timeStamp).execute(0);
		}
		catch(Exception e)
		{
			
			Log.e(App.TAG, e.getMessage());
		}
		
	}


	private static boolean contactExists(Context context, String number) 
	{
		// / number is the phone number
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
		String[] mPhoneNumberProjection = { PhoneLookup._ID,PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
		try
		{
			if (cur.moveToFirst())
			{
				return true;
			}
		}
		finally
		{
			if (cur != null)
				cur.close();
		}
		return false;
	}
	
	

	
}
