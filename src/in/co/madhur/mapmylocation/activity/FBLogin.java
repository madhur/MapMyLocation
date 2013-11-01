package in.co.madhur.mapmylocation.activity;

import java.util.Arrays;
import java.util.List;

import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.preferences.Preferences;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.Session.StatusCallback;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import static in.co.madhur.mapmylocation.App.TAG;

public class FBLogin extends Activity
{
	private UiLifecycleHelper uiHelper;
	private ToggleButton fbPostPermission;
	private Preferences appPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook);
		uiHelper = new UiLifecycleHelper(this, statusCallback);
		appPreferences=new Preferences(this);

		LoginButton authButton = (LoginButton) this.findViewById(R.id.authButton);
		fbPostPermission = (ToggleButton) this.findViewById(R.id.fbpostPermissionSwitch);

		UpdateSessionScreen();

		fbPostPermission.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{

				if (isChecked)
				{
					Session session = Session.getActiveSession();
					if (session != null && session.isOpened())
					{

						List<String> permissions = session.getPermissions();

						if (!permissions.contains("publish_actions"))
						{
							Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(FBLogin.this, Arrays.asList("publish_actions"));

							session.requestNewPublishPermissions(newPermissionsRequest);
						}
					}
				}

			}

		});

		authButton.setReadPermissions(Arrays.asList("basic_info"));

	}

	Session.StatusCallback statusCallback = new StatusCallback()
	{

		@Override
		public void call(Session session, SessionState state, Exception exception)
		{
			onSessionStateChange(session, state, exception);

		}
	};

	private void UpdateSessionScreen()
	{
		Session session = Session.getActiveSession();

		if (session == null || session.isClosed())
			fbPostPermission.setEnabled(false);
		else
		{

			fbPostPermission.setEnabled(true);
			List<String> permissions = session.getPermissions();

			if (permissions.contains("publish_actions"))
			{
				fbPostPermission.setChecked(true);
			}
			else
			{
				fbPostPermission.setChecked(false);
			}

		}

	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception)
	{
		Intent returnIntent = new Intent();

		if (state.isOpened())
		{
			List<String> permissions = session.getPermissions();

			if (permissions.contains("publish_actions"))
			{
				returnIntent.putExtra(Consts.ACCESS_TOKEN, session.getAccessToken());
				returnIntent.putExtra(Consts.ACCESS_EXPIRES, session.getExpirationDate().toString());

				setResult(RESULT_OK, returnIntent);
				finish();
			}
			else
			{
				Request meRequest=Request.newMeRequest(session, new GraphUserCallback()
				{
					
					@Override
					public void onCompleted(GraphUser user, Response response)
					{
						if(response.getError()==null)
						{
							
							appPreferences.setFBUserName(user.getName());
						}
						
					}
				});
				
				RequestAsyncTask asyncTask=meRequest.executeAsync();
				
				
			}

		}
		else if (state.isClosed())
		{
			Log.i(TAG, "Logged out...");
			// setResult(RESULT_CANCELED, returnIntent);
			// finish();
		}
		
		UpdateSessionScreen();

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
