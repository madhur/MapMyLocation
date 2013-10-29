package in.co.madhur.mapmylocation.activity;


import java.util.Arrays;

import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import android.app.Activity;
import android.content.Intent;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.Session.StatusCallback;
import com.facebook.widget.LoginButton;

import static in.co.madhur.mapmylocation.App.LOG;
import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;

public class FBLogin extends Activity
{
	private UiLifecycleHelper uiHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook);
		uiHelper = new UiLifecycleHelper(this, statusCallback);
		
		LoginButton authButton = (LoginButton) this.findViewById(R.id.authButton);
		
		authButton.setPublishPermissions(Arrays.asList("publish_actions"));
		
		
	}

	Session.StatusCallback statusCallback = new StatusCallback()
	{

		@Override
		public void call(Session session, SessionState state,
				Exception exception)
		{
			onSessionStateChange(session, state, exception);

		}
	};

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception)
	{
		Intent returnIntent=new Intent();
			
		
		if (state.isOpened())
		{
			Log.i(TAG, "Logged in...");
			if(LOCAL_LOGV)
			{
				Log.v(TAG, session.getAccessToken());
				Log.v(TAG, session.getExpirationDate().toString());
				
			}
			returnIntent.putExtra(Consts.ACCESS_TOKEN, session.getAccessToken());
			returnIntent.putExtra(Consts.ACCESS_EXPIRES, session.getExpirationDate().toString());
			
			setResult(RESULT_OK, returnIntent);
			finish();
			
			
		}
		else if (state.isClosed())
		{
			Log.i(TAG, "Logged out...");
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	@Override
	public void onResume()
	{
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}


}
