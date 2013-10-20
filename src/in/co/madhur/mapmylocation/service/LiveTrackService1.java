package in.co.madhur.mapmylocation.service;

import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;

import java.util.List;

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

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Request.Callback;
import com.facebook.model.GraphObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

public class LiveTrackService1 extends IntentService
{
	private Location location = null;
	private final Object gotLocationLock = new Object();
	private Coordinates coordinates;
	private final LocationResult locationResult = new LocationResultChild();
	Preferences appPrefences;

	public LiveTrackService1(String name)
	{
		super("Live Track Service");
		Log.v(App.TAG, "Starting live track service");
		// TODO Auto-generated constructor stub
	}
	
	public LiveTrackService1()
	{
		
		super("Live Track Service");
		appPrefences=new Preferences(this);
		
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		boolean showNotification=appPrefences.showLiveTrackNotifications();
		NotificationManager nm=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Session session=GetSession();
		if (session == null)
		{
			Log.e(App.TAG, "Cannot get session");
			
			if(showNotification)
			{
				NotificationCompat.Builder builder=GetNotificationBuilder(NotificationType.FB_SESSION_FAILURE);
				nm.notify(0, builder.build());
			}
			
			stopSelf();
			return;
		}
			
			
		
		Coordinates result=getLocation(Consts.MAX_WAIT_TIME, Consts.UPDATE_TIMEOUT);

		if(result == null)
		{
			if(showNotification)
			{
			
				if(showNotification)
				{
					NotificationCompat.Builder builder=GetNotificationBuilder(NotificationType.LOCATION_FAILURE);
					nm.notify(0, builder.build());
				}
			}
			
			stopSelf();
			return;
		}
		
		
		String fbUrl=String.format(Consts.GOOGLE_MAPS_URL, location.getLatitude(), location.getLongitude());
		String fbMessage=appPrefences.getFBMessage();
		String fbPrivacy=appPrefences.getFBFriends();
		
		JSONObject privacyOptions = null;
		try
		{
			privacyOptions = GetPrivacyJson(fbPrivacy);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Response fbResponse=PostToFB(session, fbMessage, privacyOptions, fbUrl);
		
		if(fbResponse.getError()==null)
		{
			
			if(showNotification)
			{
				NotificationCompat.Builder builder=GetNotificationBuilder(NotificationType.FB_POSTED);
				nm.notify(0, builder.build());
			}
		}
		else
		{
			
			if(showNotification)
			{
				NotificationCompat.Builder builder=GetNotificationBuilder(NotificationType.FB_FAILURE);
				nm.notify(0, builder.build());
			}
		}

	}
	
	
	private JSONObject GetPrivacyJson(String fbPrivacy) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		
		if(!fbPrivacy.equals(Consts.FBPrivacies.CUSTOM))
		{
			jsonObject.put("value", fbPrivacy);
			return jsonObject;
		}
		
		return null;
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
	
		if(!TextUtils.isEmpty(message))
			graphObject.setProperty("message", message);
		graphObject.setProperty("link", locationUrl);
		graphObject.setProperty("privacy", privacyOptions.toString());


		Request myRequest = Request.newPostRequest(session, "/me/feed/",
				graphObject, new Callback()
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
		}

		return fbResponse;
	}
	
	
	private synchronized Coordinates getLocation(int maxWaitingTime, int updateTimeout)
	{
		try
		{
			if(LOCAL_LOGV)
			Log.v(App.TAG, "getLocation(" + String.valueOf(maxWaitingTime)+","+String.valueOf(updateTimeout)+")");
			
			final int updateTimeoutPar = updateTimeout;
			synchronized (gotLocationLock)
			{
				new Thread()
				{
					public void run()
					{
						if(LOCAL_LOGV)
						{
						Log.v(TAG, "Creating looper");
						Log.v(TAG, "Thread Name: " + Thread.currentThread().getName()
								+ "Thread ID: " + Thread.currentThread().getId());
						}
						
						
						
						Looper.prepare();
						LocationResolver locationResolver = new LocationResolver(LiveTrackService1.this);
						locationResolver.prepare();
						locationResolver.getLocation(LiveTrackService1.this, locationResult, updateTimeoutPar);
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
					LiveTrackService1.this.location = location;
					gotLocationLock.notifyAll();
					Looper.myLooper().quit();
				}
			

		}

	}
	
	private NotificationCompat.Builder GetNotificationBuilder(NotificationType type)
	{
		NotificationCompat.Builder noti=new NotificationCompat.Builder(this);
		// noti.setContentTitle(dispSender);
		noti.setAutoCancel(true);
		
		switch(type)
		{
		case LOCATION_FAILURE:
			noti.setContentText(getString(R.string.noti_loc_response));
			
			break;
			
		case FB_POSTED:
			noti.setContentText(getString(R.string.noti_fb_posted));
			break;
			
		case  FB_FAILURE:
			noti.setContentText(getString(R.string.noti_fb_failure));
			
			break;
			
		case FB_SESSION_FAILURE:
			noti.setContentText(getString(R.string.noti_fb_session_failure));
			
		default:
			break;
		
		}
		
		
		noti.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		
		PendingIntent notifyIntent=PendingIntent.getActivity(this, 0, new Intent(), 0);
		
		noti.setContentIntent(notifyIntent);
		
		return noti;
	}
	


}
