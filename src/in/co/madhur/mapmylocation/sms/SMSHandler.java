package in.co.madhur.mapmylocation.sms;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.preferences.Preferences;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class SMSHandler
{
	static Preferences appPreferences;

	public static void HandleIncomingSMS(Context context, String message, String sender)
	{
		appPreferences=new Preferences(context);
		
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
		
		String secretCode=appPreferences.getSecretCode();
		if(secretCode.isEmpty())
		{
			Log.e(App.TAG, "Secret code is empty, returning");
			return;
		}
		
		if(!message.startsWith(secretCode))
		{
			Log.v(App.TAG, "Message does not start with secret code");
			return;
			
		}
		
		// All conditions passed
		
		ReplyToMessage(context, sender);
		
	}
	
	
	private static void ReplyToMessage(Context context, String sender)
	{
		
		
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
