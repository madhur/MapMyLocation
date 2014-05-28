package in.co.madhur.mapmylocation.preferences;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.activity.FriendPickerActivity;
import in.co.madhur.mapmylocation.location.Coordinates;

public final class Preferences
{
	private SharedPreferences sharedPreferences;
	Context context;
	private final int FB_SELCTFRIENDS = 2;

	public enum Keys
	{

		ENABLE_TRACKME("pref_enable_trackme"),
		SECRET_CODE("pref_setsecretcode"),
		ALLOW_CONTACTS("pref_onlyallowcontacts"),
		SELECT_CONTACTS("pref_selectcontacts"),
		ENABLE_LIVETRACK("pref_enable_livetrack"),
		CONNECT_FB("pref_connectfb"),
		FB_INTERVAL("pref_fbinterval"),
		SELECT_FB_FRIENDS("pref_selectfbfriends"),
		FB_MESSAGE("pref_fbmessage"),
		ABOUT("pref_about"),
		FAQ("pref_faq"),
		SHOW_TRACKME_NOTIFICATION("pref_trackme_shownotification"),
		SHOW_LIVETRACK_NOTIFICATION("pref_livetrack_shownotification"),
		FB_FRIENDS("pref_selectfbfriends"),
		SETTINGS_TRACKME("pref_settings_trackme"),
		SETTINGS_LIVETRACK("pref_settings_livetrack"),
		FB_FRIENDS_CUSTOM("pref_fb_friends_custom"),
		LOC_TIMEOUT("pref_loctimeout"),
		THREAD_TIMEOUT("pref_threadimeout"),
		ENABLE_GEOCODE("pref_enablegeocode"),
		FB_USERNAME("fb_username"),
		LAST_LATITUDE("last_latitude"),
		LAST_LONGITUDE("last_longitude"),
		PREF_SHARE_LOC("pref_shareloc"),
		PREF_DONATE("pref_donate"),
		LAST_LOCATION_TIME("last_location_time");

		public final String key;

		private Keys(String key)
		{
			this.key = key;

		}

	};

	public Preferences(Context context)
	{
		this.context = context;
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void setCustomFriends(HashMap<String, String> friendsObject)
			throws JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		SharedPreferences.Editor editor = sharedPreferences.edit();

		String json = mapper.writeValueAsString(friendsObject);
		editor.putString(Keys.FB_FRIENDS_CUSTOM.key, json);
		editor.commit();
	}

	public Coordinates getLastLocation()
	{
		String latitude = sharedPreferences.getString(Keys.LAST_LATITUDE.key, "");
		String longitude = sharedPreferences.getString(Keys.LAST_LONGITUDE.key, "");

		double dlat, dlong;

		try
		{
			dlat = Double.parseDouble(latitude);
			dlong = Double.parseDouble(longitude);
		}
		catch (NumberFormatException e)
		{

			return null;
		}

		return new Coordinates(dlat, dlong);

	}

	public void setLastLocation(Coordinates result)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(Keys.LAST_LATITUDE.key, String.valueOf(result.getLatitude()));
		editor.putString(Keys.LAST_LONGITUDE.key, String.valueOf(result.getLongitude()));
		editor.commit();

	}

	public HashMap<String, String> getCustomFriends() throws JSONException,
			JsonParseException, JsonMappingException, IOException
	{
		String friendsObj = sharedPreferences.getString(Keys.FB_FRIENDS_CUSTOM.key, "");

		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> map = mapper.readValue(friendsObj, new TypeReference<HashMap<String, String>>()
		{
		});

		return map;
	}

	public boolean isGeoCodeEnabled()
	{
		return sharedPreferences.getBoolean(Keys.ENABLE_GEOCODE.key, Defaults.ENABLE_GEOCODE);
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

	public String getFBFriends()
	{

		return sharedPreferences.getString(Keys.FB_FRIENDS.key, Defaults.FB_FRIENDS);
	}

	public int getFBInterval()
	{
		return getStringAsInt(Keys.FB_INTERVAL.key, Defaults.FB_RATE);

	}
	
	public String getFBUserName()
	{
		return sharedPreferences.getString(Keys.FB_USERNAME.key, "");
		
	}
	
	public void setFBUserName(String name)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(Keys.FB_USERNAME.key, name);
		editor.commit();
	}

	private int getStringAsInt(String key, int def)
	{
		try
		{
			String s = sharedPreferences.getString(key, null);
			if (s == null)
				return def;

			return Integer.valueOf(s);
		}
		catch (NumberFormatException e)
		{
			return def;
		}
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{

		if (key.equals(Keys.FB_FRIENDS.key))
		{
			String fbFriendsVal = getFBFriends();
			if (fbFriendsVal.equals(Consts.FB_FRIENDS_FIRE))
			{
				if (context instanceof Activity)
				{
					Activity activity = (Activity) context;
					activity.startActivityForResult(new Intent(context, FriendPickerActivity.class), FB_SELCTFRIENDS);
				}
				else if (context instanceof Service)
				{
					Service service = (Service) context;
					service.startActivity(new Intent(context, FriendPickerActivity.class), null);

				}
				else
				{
					Log.e(App.TAG, "Could not start FB Friends Activity");
				}
			}
		}

	}

	public void setListener(OnSharedPreferenceChangeListener listener)
	{
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

	}
	
	

	public int getLocationTimeout()
	{
		String timeout = sharedPreferences.getString(Keys.LOC_TIMEOUT.key, Defaults.UPDATE_TIMEOUT);

		return Integer.parseInt(timeout) * 1000;

	}

	public int getThreadTimeout()
	{
		String timeout = sharedPreferences.getString(Keys.THREAD_TIMEOUT.key, Defaults.MAX_WAIT_TIME);

		return Integer.parseInt(timeout) * 1000;

	}

}
