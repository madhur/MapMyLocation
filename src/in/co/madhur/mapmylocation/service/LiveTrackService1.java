package in.co.madhur.mapmylocation.service;

import in.co.madhur.mapmylocation.App;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Request.Callback;
import com.facebook.model.GraphObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class LiveTrackService1 extends IntentService
{

	public LiveTrackService1(String name)
	{
		super("Live Track Service");
		Log.v(App.TAG, "Starting live track service");
		// TODO Auto-generated constructor stub
	}
	
	public LiveTrackService1()
	{
		
		super("Live Track Service");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Session session = Session.openActiveSessionFromCache(this);

		if (session == null)
			session = Session.getActiveSession();

		if (session == null)
		{

			Log.e(App.TAG, "Cannot get session");
			stopSelf();
		}

		GraphObject graphObject = GraphObject.Factory.create();

		graphObject
				.setProperty(
						"link",
						"https://www.google.com/maps?q=pitampura&ll=28.702182,77.142563&spn=0.13506,0.264187&t=m&hnear=Pitampura,+New+Delhi,+North+West+Delhi,+Delhi,+India&z=13&iwloc=A");
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

		Request myRequest = Request.newPostRequest(session, "/me/feed/",
				graphObject, new Callback()
				{

					@Override
					public void onCompleted(Response response)
					{
						Log.v("Tag", response.toString());

					}
				});
		
		try
		{
		Response fbResponse = myRequest.executeAndWait();
		}
		catch(Exception e)
		{
			Log.e(App.TAG, e.getMessage());
		}

	}

}
