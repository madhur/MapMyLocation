package in.co.madhur.mapmylocation.preferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.preferences.Preferences.Keys.*;

public final class Preferences
{
	private Context context;
	private SharedPreferences sharedPreferences;

	public enum Keys
	{
		
		ENABLE_TRACKME("pref_enable_trackme_desc"),
		SECRET_CODE("pref_setsecretcode"),
		ALLOW_CONTACTS("pref_onlyallowcontacts"),
		SELECT_CONTACTS("pref_selectcontacts"),
		MAX_RATE("pref_max_rate"),
		ENABLE_LIVETRACK("pref_enable_livetrack_desc"),
		CONNECT_FB("pref_connectfb"),
		FB_INTERVAL("pref_fbinterval"),
		SELECT_FB_FRIENDS("pref_selectfbfriends"),
		FB_MESSAGE("pref_fbmessage"),
		ABOUT("pref_about_desc"),
		FAQ("pref_faq_desc"),
		SHOW_TRACKME_NOTIFICATION("pref_trackme_shownotification"),
		SHOW_LIVETRACK_NOTIFICATION("pref_livetrack_shownotification");
		
		public final String key;
		private Keys(String key)
		{
			this.key=key;
			
		}
		
		
	};
	
	public Preferences(Context context)
	{
		this.context=context;
		this.sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
		
	}
	
	public String getSecretCode()
	{
		return sharedPreferences.getString(Keys.SECRET_CODE.key, null);
		
	}
	
	public String getFBMessage()
	{
		return sharedPreferences.getString(Keys.FB_MESSAGE.key, null);
		
	}
	
	public boolean isTrackMeEnabled()
	{
		return sharedPreferences.getBoolean(Keys.ENABLE_TRACKME.key, false);
		
	}
	
	public boolean isLiveTrackEnabled()
	{
		return sharedPreferences.getBoolean(Keys.ENABLE_LIVETRACK.key, false);
		
	}
	
	public boolean isFBConnected()
	{
		return sharedPreferences.getBoolean(Keys.CONNECT_FB.key, false);
		
	}
	
	public String getMaxRate()
	{
		return sharedPreferences.getString(Keys.MAX_RATE.key, "");
		
		
	}
	
	public String getFBInterval()
	{
		return sharedPreferences.getString(Keys.FB_INTERVAL.key, "");
		
	}
	
	public boolean isOnlyAllowContacts()
	{
		return sharedPreferences.getBoolean(Keys.ALLOW_CONTACTS.key, false);
		
	}
	
	public boolean showLiveTrackNotifications()
	{
		return sharedPreferences.getBoolean(Keys.SHOW_LIVETRACK_NOTIFICATION.key, true);
		
	}
	
	public boolean showTrackMeNotifications()
	{
		return sharedPreferences.getBoolean(Keys.SHOW_TRACKME_NOTIFICATION.key, true);
		
	}
	
	
	
	



}
	
	

