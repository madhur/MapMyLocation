package in.co.madhur.mapmylocation.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Request.Callback;
import com.facebook.model.GraphObject;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.preferences.Preferences;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LiveTrackService extends Service
{
	Preferences appPrefences;
	 private Session.StatusCallback statusCallback = new SessionStatusCallback();
	 
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		
		appPrefences=new Preferences(this);
		
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub

		Session session = Session.openActiveSessionFromCache(this);
		
		if(session==null)
			session=Session.getActiveSession();
		
		if(session==null)
		{
			
			Log.e(App.TAG, "Cannot get session");
			stopSelf();
		}
		
		GraphObject graphObject=GraphObject.Factory.create();
		
		graphObject.setProperty("link", "https://www.google.com/maps?q=pitampura&ll=28.702182,77.142563&spn=0.13506,0.264187&t=m&hnear=Pitampura,+New+Delhi,+North+West+Delhi,+Delhi,+India&z=13&iwloc=A");
		graphObject.setProperty("message", "Hi its me");
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("value", "CUSTOM");
			jsonObject.put("allow", "100000918024839");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			Log.e(App.TAG, e.getMessage());
		}
		
		
		graphObject.setProperty("privacy", jsonObject.toString());
		Log.v("TAG", jsonObject.toString());
		
		
		Request myRequest=Request.newPostRequest(session, "/me/feed/", graphObject, new Callback()
		{
			
			@Override
			public void onCompleted(Response response)
			{
				Log.v("Tag", response.toString());
				
			}
		});
		
		
		RequestAsyncTask fbResponse=myRequest.executeAsync();
		
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	
	

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	 private class SessionStatusCallback implements Session.StatusCallback {
	        @Override
	        public void call(Session session, SessionState state, Exception exception) 
	        {
	           
	        }
	    }

}
