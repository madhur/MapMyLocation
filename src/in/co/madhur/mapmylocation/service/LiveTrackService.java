package in.co.madhur.mapmylocation.service;

import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationResolver;
import in.co.madhur.mapmylocation.location.LocationResult;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.tasks.NotificationType;
import in.co.madhur.mapmylocation.util.AppLog;
import in.co.madhur.mapmylocation.exceptions.EmptyFriendsException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Request.Callback;
import com.facebook.model.GraphObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.location.Geocoder;

public class LiveTrackService extends IntentService
{
	private Location location = null;
	private final Object gotLocationLock = new Object();
	private Coordinates coordinates;
	private final LocationResult locationResult = new LocationResultChild();
	Preferences appPrefences;
	private AppLog appLog;

	public LiveTrackService()
	{
		super("Hermes Live Track Service");
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		appLog = new AppLog(DateFormat.getDateFormatOrder(this));
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (appLog != null)
			appLog.close();
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		appLog.append("Starting service to post to Facebook");
		appPrefences = new Preferences(this);

		boolean showNotification = appPrefences.showLiveTrackNotifications();
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Check if somehow live track is disabled. If yes, silently return
		boolean liveTrackEanbled = appPrefences.isLiveTrackEnabled();
		if (!liveTrackEanbled)
			return;

		boolean fbConnected = appPrefences.isFBConnected();
		if (!fbConnected)
		{
			appLog.append("Facebook not connected in settings");

			if (showNotification)
			{
				NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.FB_NOT_CONNECTED);
				nm.notify(0, builder.build());
			}

			stopSelf();
			return;
		}

		Session session = GetSession();
		if (session.isClosed())
		{
			Log.e(App.TAG, "Cannot get session");
			appLog.append("Failure: Facebook Session while posting");
			if (showNotification)
			{
				NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.FB_SESSION_FAILURE);
				nm.notify(0, builder.build());
			}

			stopSelf();
			return;
		}

		Coordinates result = getLocation(appPrefences.getThreadTimeout(), appPrefences.getLocationTimeout());
		
		if(result!=null)
		{
			appPrefences.setLastLocation(result);
			
		}
		else if (result == null)
		{
			appLog.append("Failure: Retrieving location while posting");
			if (showNotification)
			{
//				new Notifications(this);
				NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.LOCATION_FAILURE);
				nm.notify(0, builder.build());
			}

			stopSelf();
			return;
		}
		
		
		if(!Connection.isNetworkGood(this))
		{
			appLog.append("No Connection to network or background data disabled");
			stopSelf();
			return;
		}
		
		List<Address> addresses = null;
		String name = null, caption=null, description=null;
		if(appPrefences.isGeoCodeEnabled())
		{
			Geocoder geoCoder=new Geocoder(this);
			try
			{
				addresses=geoCoder.getFromLocation(result.getLatitude(), result.getLongitude(), 1);
			}
			catch (IOException e)
			{
				appLog.append("Error Gecoding: "+ e.getMessage());
				e.printStackTrace();
			}
			
			
			if(addresses!=null && addresses.size() > 0)
			{
				
				Address address=addresses.get(0);
				name=address.getSubLocality();
				caption=address.getSubLocality();
				int lines=address.getMaxAddressLineIndex();
				
				if(lines!=-1)
				{
					StringBuilder sbr=new StringBuilder();
					for (int i=0;i<lines;++i)
					{
						sbr.append(address.getAddressLine(i));
						sbr.append(" ");
						Log.v(TAG, "Address line :"+ i +address.getAddressLine(i));
					}
					
					description=sbr.toString();
				}
				
				
				
				
				if(App.LOCAL_LOGV)
				{
					
					if(address.getLocality()!=null)
					Log.v(TAG, "Locality:"+address.getLocality());
					if(address.getFeatureName()!=null)
					Log.v(TAG, "Feature Name:"+address.getFeatureName());
					if(address.getAdminArea()!=null)
					Log.v(TAG, "Admin area:"+address.getAdminArea());
					if(address.getSubAdminArea()!=null)
					Log.v(TAG, "Subadmin area:"+address.getSubAdminArea());
					if(address.getSubLocality()!=null)
					Log.v(TAG, "Sublocality:"+address.getSubLocality());
					if(address.getThoroughfare()!=null)
						Log.v(TAG, "Thoroughfare:"+address.getThoroughfare());
					if(address.getSubThoroughfare()!=null)
						Log.v(TAG, "SubThoroughfare:"+address.getSubThoroughfare());
					if(address.getPremises()!=null)
						Log.v(TAG, "Premises:"+address.getPremises());
					
					int lines1=address.getMaxAddressLineIndex();
					if(lines1!=-1)
					{
						
						for (int i=0;i<lines1;++i)
						{
							Log.v(TAG, "Address line :"+ i +address.getAddressLine(i));
						}
					}
					
				}
			}
			
			
		}

		String fbUrl = String.format(Consts.GOOGLE_MAPS_URL, location.getLatitude(), location.getLongitude());
		String fbMessage = appPrefences.getFBMessage();
		String fbPrivacy = appPrefences.getFBFriends();

		JSONObject privacyOptions = null;
		try
		{
			privacyOptions = GetPrivacyJson(fbPrivacy);
		}
		catch (JSONException e)
		{
			appLog.append(e.getMessage());
			Log.e(App.TAG, e.getMessage());
		}
		catch (EmptyFriendsException e)
		{
			NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.FB_FAILURE, this.getString(e.errorResourceId()));
			nm.notify(0, builder.build());
			stopSelf();
			return;
		}

		Response fbResponse = null;
		try
		{
			fbResponse = PostToFB(session, fbMessage, privacyOptions, fbUrl, name , caption, description);
		}
		catch (Exception e)
		{
			Log.e(App.TAG, e.getMessage());
			appLog.append(e.getMessage());
		}

		if (fbResponse != null)
		{
			if (fbResponse.getError() == null)
			{

				if (showNotification)
				{
					NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.FB_POSTED);
					nm.notify(0, builder.build());
				}
			}
			else
			{
				appLog.append("Error while posting to Facebook:"
						+ fbResponse.getError().getErrorMessage());
				if (showNotification)
				{
					NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.FB_FAILURE);
					nm.notify(0, builder.build());
				}
			}
		}

	}

	private JSONObject GetPrivacyJson(String fbPrivacy) throws JSONException,
			EmptyFriendsException
	{
		JSONObject jsonObject = new JSONObject();

		if (!fbPrivacy.equals(Consts.FBPrivacies.CUSTOM.toString()))
		{

			jsonObject.put("value", fbPrivacy);
			return jsonObject;
		}
		else
		{
			jsonObject.put("value", "CUSTOM");

			HashMap<String, String> customFriends = null;
			try
			{
				customFriends = appPrefences.getCustomFriends();
				if (customFriends == null || customFriends.size() == 0)
					throw new EmptyFriendsException();
			}
			catch (JsonParseException e)
			{
				Log.e(App.TAG, e.getMessage());
			}
			catch (JsonMappingException e)
			{
				Log.e(App.TAG, e.getMessage());
				throw new EmptyFriendsException();
			}
			catch (IOException e)
			{
				Log.e(App.TAG, e.getMessage());
			}

			String commaSeperatedIds = GetCommaSeparetedIds(customFriends);
			jsonObject.put("allow", commaSeperatedIds);

			return jsonObject;

		}

	}

	private String GetCommaSeparetedIds(HashMap<String, String> customFriends)
	{
		StringBuilder sbr = new StringBuilder();
		Set<String> ids = customFriends.keySet();
		Iterator<String> itr = ids.iterator();
		while (itr.hasNext())
		{

			sbr.append(itr.next());
			sbr.append(',');

		}

		return sbr.toString().substring(0, sbr.length() - 1);
	}

	private Session GetSession()
	{

		Session session = Session.openActiveSessionFromCache(this);

		if (session == null)
			session = Session.getActiveSession();

		return session;

	}

	private Response PostToFB(Session session, String message, JSONObject privacyOptions, String locationUrl, String name, String caption, String description)
	{
		GraphObject graphObject = GraphObject.Factory.create();

		if (!TextUtils.isEmpty(message))
			graphObject.setProperty("message", message);
		graphObject.setProperty("link", locationUrl);
		graphObject.setProperty("privacy", privacyOptions.toString());
		graphObject.setProperty("name", name);
		graphObject.setProperty("caption", caption);
		graphObject.setProperty("description", description);

		Request myRequest = Request.newPostRequest(session, "/me/feed/", graphObject, new Callback()
		{

			@Override
			public void onCompleted(Response response)
			{
				if(LOCAL_LOGV)
					Log.v(App.TAG, response.toString());

			}
		});

		Response fbResponse = myRequest.executeAndWait();

		return fbResponse;
	}

	private synchronized Coordinates getLocation(int maxWaitingTime, int updateTimeout)
	{
		try
		{
			if (LOCAL_LOGV)
				Log.v(App.TAG, "getLocation(" + String.valueOf(maxWaitingTime)
						+ "," + String.valueOf(updateTimeout) + ")");

			final int updateTimeoutPar = updateTimeout;
			synchronized (gotLocationLock)
			{
				new Thread()
				{
					public void run()
					{
						if (LOCAL_LOGV)
						{
							Log.v(TAG, "Creating looper");
							Log.v(TAG, "Thread Name: "
									+ Thread.currentThread().getName()
									+ "Thread ID: "
									+ Thread.currentThread().getId());
						}

						Looper.prepare();
						LocationResolver locationResolver = new LocationResolver(LiveTrackService.this, appLog);
						locationResolver.prepare();
						locationResolver.getLocation(LiveTrackService.this, locationResult, updateTimeoutPar);
						Looper.loop();
					}
				}.start();

				gotLocationLock.wait(maxWaitingTime);
			}
		}
		catch (InterruptedException e1)
		{
			Log.e(TAG, e1.getMessage());
		}

		if (location != null)
			coordinates = new Coordinates(location.getLatitude(), location.getLongitude());
		else
			coordinates = Coordinates.UNDEFINED;
		return coordinates;
	}

	public class LocationResultChild extends LocationResult
	{

		@Override
		public void gotLocation(Location location)
		{

			synchronized (gotLocationLock)
			{
				LiveTrackService.this.location = location;
				gotLocationLock.notifyAll();
				Looper.myLooper().quit();
			}

		}

	}

}
