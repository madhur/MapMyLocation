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
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationResolver;
import in.co.madhur.mapmylocation.location.LocationResult;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.tasks.LocationTask;
import in.co.madhur.mapmylocation.tasks.NotificationType;
import in.co.madhur.mapmylocation.tasks.LocationTask.LocationResultChild;
import in.co.madhur.mapmylocation.util.AppLog;
import in.co.madhur.mapmylocation.activity.ToastActivity;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Request.Callback;
import com.facebook.model.GraphObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

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
		// TODO Auto-generated method stub
		super.onCreate();

		appLog = new AppLog(DateFormat.getDateFormatOrder(this));
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
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

		// Check if Facebook is connected. If not, post a notification to
		// TODO: connect to Facebook. This should open the app, instead of
		// dismissing
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

		Coordinates result = getLocation(Consts.MAX_WAIT_TIME, Consts.UPDATE_TIMEOUT);

		if (result == null)
		{
			appLog.append("Failure: Retrieving location while posting");
			if (showNotification)
			{
				new Notifications(this);
				NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.LOCATION_FAILURE);
				nm.notify(0, builder.build());
			}

			stopSelf();
			return;
		}

		String fbUrl = String.format(Consts.GOOGLE_MAPS_URL, location.getLatitude(), location.getLongitude());
		String fbMessage = appPrefences.getFBMessage();
		String fbPrivacy = appPrefences.getFBFriends();

		JSONObject privacyOptions = null;
		try
		{
			privacyOptions = GetPrivacyJson(fbPrivacy);
			Log.v(App.TAG, privacyOptions.toString());
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			Log.e(App.TAG, e.getMessage());
		}

		Response fbResponse = PostToFB(session, fbMessage, privacyOptions, fbUrl);

		if (fbResponse.getError() == null)
		{

			if (showNotification)
			{
				new Notifications(this);
				NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.FB_POSTED);
				nm.notify(0, builder.build());
			}
		}
		else
		{
			appLog.append(fbResponse.getError().getErrorMessage());
			if (showNotification)
			{
				new Notifications(this);
				NotificationCompat.Builder builder = Notifications.GetNotificationBuilder(this, NotificationType.FB_FAILURE, fbResponse.getError().getErrorMessage());
				nm.notify(0, builder.build());
			}
		}

	}

	private JSONObject GetPrivacyJson(String fbPrivacy) throws JSONException
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
				customFriends=appPrefences.getCustomFriends();
			}
			catch (JsonParseException e)
			{
				Log.e(App.TAG, e.getMessage());
			}
			catch (JsonMappingException e)
			{
				Log.e(App.TAG, e.getMessage());
			}
			catch (IOException e)
			{
				Log.e(App.TAG, e.getMessage());
			}
			
			String commaSeperatedIds=GetCommaSeparetedIds(customFriends);
			jsonObject.put("allow", commaSeperatedIds);
			
			return jsonObject;
			
		}

	}

	private String GetCommaSeparetedIds(HashMap<String, String> customFriends)
	{
		StringBuilder sbr=new StringBuilder();
		Set<String> ids=customFriends.keySet();
		Iterator<String> itr=ids.iterator();
		while(itr.hasNext())
		{
			
			sbr.append(itr.next());
			sbr.append(',');
			
		}
		
		return sbr.toString().substring(0, sbr.length()-1);
	}

	private Session GetSession()
	{

		Session session = Session.openActiveSessionFromCache(this);

		if (session == null)
			session = Session.getActiveSession();

		return session;

	}

	private Response PostToFB(Session session, String message, JSONObject privacyOptions, String locationUrl)
	{
		GraphObject graphObject = GraphObject.Factory.create();

		if (!TextUtils.isEmpty(message))
			graphObject.setProperty("message", message);
		graphObject.setProperty("link", locationUrl);
		graphObject.setProperty("privacy", privacyOptions.toString());

		Request myRequest = Request.newPostRequest(session, "/me/feed/", graphObject, new Callback()
		{

			@Override
			public void onCompleted(Response response)
			{
				Log.v("Tag", response.toString());

			}
		});

		Response fbResponse = null;
		try
		{
			fbResponse = myRequest.executeAndWait();
		}
		catch (Exception e)
		{
			Log.e(App.TAG, e.getMessage());
			appLog.append(e.getMessage());
		}

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
