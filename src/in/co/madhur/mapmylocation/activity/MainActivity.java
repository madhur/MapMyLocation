package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.preferences.Preferences.Keys;
import in.co.madhur.mapmylocation.recievers.SMSReciever;
import in.co.madhur.mapmylocation.service.Alarms;
import in.co.madhur.mapmylocation.util.AppLog;
import in.co.madhur.mapmylocation.util.Util;

import com.facebook.Session;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import static in.co.madhur.mapmylocation.App.TAG;

public class MainActivity extends PreferenceActivity
{
	Preferences appPreferences;
	private final int FB_REQUESTCODE = 1;
	private final int FB_SELCTFRIENDS = 2;

	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener()
	{

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{

			if (key.equals(Keys.FB_FRIENDS.key))
			{
				String fbFriendsVal = appPreferences.getFBFriends();
				if (fbFriendsVal.equals(Consts.FB_FRIENDS_FIRE))
				{
					MainActivity.this.startActivityForResult(new Intent(MainActivity.this, FriendPickerActivity.class), FB_SELCTFRIENDS);
				}
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		appPreferences = new Preferences(this);
		appPreferences.setListener(listener);

		setupStrictMode();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void setupStrictMode()
	{
		if (App.DEBUG
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyFlashScreen().build());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, (android.view.Menu) menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_viewlog:
				show(Dialogs.VIEW_LOG);
				return true;

			case R.id.action_about:
				show(Dialogs.ABOUT_DIALOG);

				return true;

			case R.id.action_advpref:
				Intent i = new Intent();
				i.setClass(this, AdvancedPreferencesActivity.class);
				startActivity(i);

			default:
				return super.onMenuItemSelected(featureId, item);

		}

	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Connect preference listeners
		SetListeners();

		// Update the label of FB interval
		UpdateFBIntervalLabel(null);

		// Update the label of SMS interval
		// UpdateMaxRateLabel(null);

		// Update the checkbox and summary of ENABLE Live Track (FB Connection)
		UpdateFBConnected();

		UpdateTrackMeEnabled(null);
		UpdateLiveTrackEnabled(null);

		UpdateFBFriendsLabel(null);

		CheckLocation();

		CheckLastLocationEnabled();
	}

	private void CheckLastLocationEnabled()
	{
		Preference shareLocPreference = (Preference) findPreference(Preferences.Keys.PREF_SHARE_LOC.key);
		Coordinates result = appPreferences.getLastLocation();
		if (result == null)
			shareLocPreference.setEnabled(false);
		else
			shareLocPreference.setEnabled(true);
	}

	private void CheckLocation()
	{
		boolean locationEnabled = Util.isLocationEnabled(this);
		if (!locationEnabled)
			show(Dialogs.NO_PROVIDER_ENABLED);

	}

	private void UpdateTrackMeEnabled(Boolean enabledTrackme)
	{
		Preference settingsTrackme = findPreference(Preferences.Keys.SETTINGS_TRACKME.key);
		Preference settingsEnableTrackme = findPreference(Preferences.Keys.ENABLE_TRACKME.key);

		ComponentName compName = new ComponentName(this, SMSReciever.class);
		if (enabledTrackme == null)
			enabledTrackme = appPreferences.isTrackMeEnabled();

		if (enabledTrackme)
		{
			getPackageManager().setComponentEnabledSetting(compName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
			settingsTrackme.setEnabled(true);
			settingsEnableTrackme.setSummary(R.string.pref_enable_trackme_desc_enabled);
		}
		else
		{
			getPackageManager().setComponentEnabledSetting(compName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			settingsTrackme.setEnabled(false);
			settingsEnableTrackme.setSummary(R.string.pref_enable_trackme_desc);

		}

	}

	private void UpdateLiveTrackEnabled(Boolean enabledLivetrack)
	{
		Preference settingsLivetrack = findPreference(Preferences.Keys.SETTINGS_LIVETRACK.key);
		if (enabledLivetrack == null)
			enabledLivetrack = appPreferences.isLiveTrackEnabled();

		if (enabledLivetrack)
		{

			settingsLivetrack.setEnabled(true);
		}
		else
		{
			settingsLivetrack.setEnabled(false);

		}

	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(final int id)
	{
		switch (Dialogs.values()[id])
		{
			case NO_PROVIDER_ENABLED:
				return new AlertDialog.Builder(this).setMessage(R.string.location_error_desc).setTitle(R.string.location_error).setNeutralButton("Go to Settings", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
						dismissDialog(id);

					}
				}).setPositiveButton("OK", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dismissDialog(id);

					}
				}).create();

			case ABOUT_DIALOG:
				View v = getLayoutInflater().inflate(R.layout.about_content, null);
				WebView webView = (WebView) v.findViewById(R.id.about_content);

				webView.loadUrl("file:///android_asset/about.html");

				return new AlertDialog.Builder(this).setPositiveButton(android.R.string.ok, null).setView(v).create();

			case VIEW_LOG:
				return AppLog.displayAsDialog(App.LOG, this);

			case FB_DISCONNECT:
				return new AlertDialog.Builder(this).setMessage(R.string.ui_dialog_disconnect_msg).setTitle(null).setPositiveButton(android.R.string.ok, new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// appPreferences.clearFBData();

						Session session = Session.getActiveSession();
						if (!session.isClosed())
						{
							session.closeAndClearTokenInformation();
						}

						UpdateFBConnected();

					}
				}).setNegativeButton(android.R.string.cancel, new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{

					}
				}).create();

			case EMPTY_SECRET:
				return new AlertDialog.Builder(this).setMessage(R.string.pref_emptysecret_msg).setTitle(R.string.app_name).setPositiveButton(android.R.string.ok, null).create();

			default:
				return null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == FB_REQUESTCODE)
		{

			if (resultCode == RESULT_OK)
			{
				/*
				 * String access_token=data.getStringExtra(Consts.ACCESS_TOKEN);
				 * String
				 * access_expires=data.getStringExtra(Consts.ACCESS_EXPIRES);
				 * String username=data.getStringExtra(Consts.USER_NAME);
				 * 
				 * appPreferences.setFBTokenData(access_token, access_expires,
				 * username);
				 */

			}
			else if (resultCode == RESULT_CANCELED)
			{
				// appPreferences.clearFBData();

			}

			UpdateFBConnected();

		}
		else if (requestCode == FB_SELCTFRIENDS)
		{
			if (resultCode == RESULT_OK)
			{

			}
			else if (resultCode == RESULT_CANCELED)
			{

			}

		}
	}

	private void UpdateFBConnected()
	{
		CheckBoxPreference fbConnected = (CheckBoxPreference) findPreference(Preferences.Keys.CONNECT_FB.key);

		String summary;
		Session fbSession = Session.openActiveSessionFromCache(this);

		if (fbSession == null)
			fbSession = Session.getActiveSession();

		if (fbSession != null && fbSession.isOpened() && fbSession.getPermissions().contains("publish_actions"))
		{
			fbConnected.setChecked(true);
			if(appPreferences.getFBUserName().equals(""))
				summary = getString(R.string.fb_already_connected);
			else
				summary=String.format(getString(R.string.fb_already_connectedas), appPreferences.getFBUserName());
			
		}
		else
		{
			if (fbSession != null)
			{
				Log.v(TAG, fbSession.getState().toString());

			}
			else
			{
				Log.v(TAG, "session is null, removing checkbox");

			}
			fbConnected.setChecked(false);
			summary = getString(R.string.fb_needs_connecting);
		}

		fbConnected.setSummary(summary);

	}

	public void show(Dialogs d)
	{
		showDialog(d.ordinal());
	}

	public void dismiss(Dialogs d)
	{
		try
		{
			dismissDialog(d.ordinal());
		}
		catch (IllegalArgumentException e)
		{
			// ignore
		}
	}

	private void UpdateFBIntervalLabel(String interval)
	{

		ListPreference FBInterval = (ListPreference) findPreference(Preferences.Keys.FB_INTERVAL.key);
		if (interval == null)
		{
			interval = FBInterval.getValue();
		}
		
		int index = FBInterval.findIndexOfValue(interval);
		if (index != -1)
		{
			interval = (String) FBInterval.getEntries()[index];
			FBInterval.setTitle(interval);
		}

	}

	protected void UpdateFBFriendsLabel(String friends)
	{
		ListPreference FBFriends = (ListPreference) findPreference(Preferences.Keys.FB_FRIENDS.key);
		if (friends == null)
		{
			friends = FBFriends.getValue();

		}
		
		int index = FBFriends.findIndexOfValue(friends);
		if (index != -1)
		{
			friends = (String) FBFriends.getEntries()[index];
			FBFriends.setTitle(friends);
		}

	}

	private void SetListeners()
	{

		findPreference(Preferences.Keys.FB_INTERVAL.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{

				UpdateFBIntervalLabel(newValue.toString());
				Alarms alarms = new Alarms(MainActivity.this, appPreferences);
				alarms.cancel();
				alarms.Schedule();
				return true;
			}
		});

		findPreference(Preferences.Keys.SECRET_CODE.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				String newSecret = (String) newValue;

				if (TextUtils.isEmpty(newSecret))
				{
					show(Dialogs.EMPTY_SECRET);
					return false;
				}
				return true;
			}
		});

		
		findPreference(Preferences.Keys.FB_FRIENDS.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				// TODO Auto-generated method stub
				UpdateFBFriendsLabel(newValue.toString());
				return true;
			}
		});

		findPreference(Preferences.Keys.CONNECT_FB.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean newVal = (Boolean) newValue;
				if (newVal)
				{
					startActivityForResult(new Intent(MainActivity.this, FBLogin.class), FB_REQUESTCODE);

				}
				else
					show(Dialogs.FB_DISCONNECT);
				return false;
			}
		});


		findPreference(Preferences.Keys.ENABLE_TRACKME.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			Preference settingsTrackme = findPreference(Preferences.Keys.SETTINGS_TRACKME.key);

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean newVal = (Boolean) newValue;
				UpdateTrackMeEnabled(newVal);

				return true;
			}
		});

		findPreference(Preferences.Keys.ENABLE_LIVETRACK.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean newVal = (Boolean) newValue;
				UpdateLiveTrackEnabled(newVal);

				if (newVal)
				{
					new Alarms(MainActivity.this, appPreferences).Schedule();
				}
				else
				{
					new Alarms(MainActivity.this, appPreferences).cancel();

				}
				showToastRequired(newVal);

				return true;
			}
		});

		findPreference(Preferences.Keys.ALLOW_CONTACTS.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				return true;
			}
		});

		findPreference(Preferences.Keys.ABOUT.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				show(Dialogs.ABOUT_DIALOG);
				return true;

			}
		});
		
		findPreference(Preferences.Keys.PREF_SHARE_LOC.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Coordinates result = appPreferences.getLastLocation();
				if (result != null)
				{
					Intent i = new Intent();
					i.setAction(Intent.ACTION_SEND);
					i.setType("text/plain");
					String message = String.format(Consts.GOOGLE_MAPS_URL, result.getLatitude(), result.getLongitude());

					i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
					i.putExtra(Intent.EXTRA_TEXT, message);

					startActivity(i);

				}
				return false;
			}
		});
	}

	protected void showToastRequired(boolean newVal)
	{
		if (newVal)
		{
			boolean isFbConected = appPreferences.isFBConnected();
			ListPreference intervalPreference = (ListPreference) findPreference(Keys.FB_INTERVAL.key);
			String interval = (String) intervalPreference.getEntry();
			StringBuilder sbr = new StringBuilder();

			if (isFbConected)
			{

				sbr.append(String.format(getString(R.string.toast_livetrackenabled), interval));
			}
			else
			{

				sbr.append(getString(R.string.toast_livetrackenabled_fbdisconnected));
			}
			Toast.makeText(this, sbr.toString(), Toast.LENGTH_SHORT).show();

		}

	}

}
