package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.preferences.Preferences.Keys.*;
import in.co.madhur.mapmylocation.service.LiveTrackService1;
import in.co.madhur.mapmylocation.util.AppLog;

import java.util.List;

import com.facebook.RequestAsyncTask;
import com.facebook.Session;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
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


public class MainActivity extends PreferenceActivity
{
	Preferences appPreferences;
	private final int FB_REQUESTCODE=1;
	private final int FB_SELCTFRIENDS=2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		appPreferences=new Preferences(this);
		
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
		switch(item.getItemId())
		{
			case R.id.action_viewlog:
				show(Dialogs.VIEW_LOG);
				return true;
				
				
			case R.id.action_about:
				show(Dialogs.ABOUT_DIALOG);
				
				return true;
				
				
			default:
				return super.onMenuItemSelected(featureId, item);
			
		}
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
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
	}
	
	
	private void UpdateTrackMeEnabled(Boolean enabledTrackme)
	{
		Preference settingsTrackme=findPreference(Preferences.Keys.SETTINGS_TRACKME.key);
		if(enabledTrackme==null)
			enabledTrackme=appPreferences.isTrackMeEnabled();
		
		if(enabledTrackme)
		{
			
			settingsTrackme.setEnabled(true);
		}
		else
		{
			settingsTrackme.setEnabled(false);
			
		}
	
	}
	
	private void UpdateLiveTrackEnabled(Boolean enabledLivetrack)
	{
		Preference settingsLivetrack=findPreference(Preferences.Keys.SETTINGS_LIVETRACK.key);
		if(enabledLivetrack==null)
			enabledLivetrack=appPreferences.isLiveTrackEnabled();
		
		if(enabledLivetrack)
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
		switch(Dialogs.values()[id])
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
			View v=getLayoutInflater().inflate(R.layout.about_content, null);
			WebView webView=(WebView) v.findViewById(R.id.about_content);
			
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
			
		default:
			return null;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode==FB_REQUESTCODE)
		{
			
			if(resultCode==RESULT_OK)
			{
				/*String access_token=data.getStringExtra(Consts.ACCESS_TOKEN);
				String access_expires=data.getStringExtra(Consts.ACCESS_EXPIRES);
				String username=data.getStringExtra(Consts.USER_NAME);
				
				appPreferences.setFBTokenData(access_token, access_expires, username);*/
				
			}
			else if(resultCode== RESULT_CANCELED)
			{
				// appPreferences.clearFBData();
				
			}
			
			UpdateFBConnected();
			
		}
		else if(requestCode == FB_SELCTFRIENDS)
		{
			if(resultCode==RESULT_OK)
			{
				
				
			}
			else if(resultCode==RESULT_CANCELED)
			{
			
				
			}
			
		}
	}
	
	private void UpdateFBConnected()
	{
		CheckBoxPreference fbConnected=(CheckBoxPreference) findPreference(Preferences.Keys.CONNECT_FB.key);
		Session fbSession=Session.getActiveSession();
		
		if(fbSession!=null && fbSession.isOpened())
			fbConnected.setChecked(true);
		else
			fbConnected.setChecked(false);

		
		String summary;
		if(fbConnected.isChecked())
			summary=getString(R.string.fb_already_connected);
		else
			summary=getString(R.string.fb_needs_connecting);
		
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
		
		
		ListPreference FBInterval=(ListPreference) findPreference(Preferences.Keys.FB_INTERVAL.key);
		if(interval==null)
		{
				String entry=(String) FBInterval.getEntry();
				FBInterval.setTitle(entry);
			
		}
		else
		{
			int index=FBInterval.findIndexOfValue(interval);
			if(index!=-1)
			{
				interval=(String) FBInterval.getEntries()[index];
				FBInterval.setTitle(interval);
			}
		}
				
		
	}
	

	protected void UpdateFBFriendsLabel(String friends)
	{
		ListPreference FBFriends=(ListPreference) findPreference(Preferences.Keys.FB_FRIENDS.key);
		if(friends==null)
		{
				String entry=(String) FBFriends.getEntry();
				FBFriends.setTitle(entry);
			
		}
		else
		{
			int index=FBFriends.findIndexOfValue(friends);
			if(index!=-1)
			{
				friends=(String) FBFriends.getEntries()[index];
				FBFriends.setTitle(friends);
			}
		}
		
	}
	
	private void UpdateMaxRateLabel(String rate )
	{
		ListPreference MaxRateInterval=(ListPreference) findPreference(Preferences.Keys.MAX_RATE.key);
		if(rate==null)
		{								
			MaxRateInterval.setTitle(MaxRateInterval.getEntry());					
			
		}
		else
		{
			int index=MaxRateInterval.findIndexOfValue(rate);
			if(index!=-1)
			{
				rate=(String) MaxRateInterval.getEntries()[index];
				MaxRateInterval.setTitle(rate);
			}
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
				return true;
			}
		});
		
		
		/*findPreference(Preferences.Keys.MAX_RATE.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				
				UpdateMaxRateLabel(newValue.toString());
				return true;
			}
		});*/
		
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
				boolean newVal=(Boolean) newValue;
				if(newVal)
				{
					startActivityForResult(new Intent(MainActivity.this, FBLogin.class), FB_REQUESTCODE);
					
				}
				else
					show(Dialogs.FB_DISCONNECT);
				return false;
			}
		});
		
		/*findPreference(Preferences.Keys.SELECT_FB_FRIENDS.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				startActivityForResult(new Intent(MainActivity.this, FriendPickerActivity.class), FB_SELCTFRIENDS);
				return true;
			}
		});*/
		
		findPreference(Preferences.Keys.ENABLE_TRACKME.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			Preference settingsTrackme=findPreference(Preferences.Keys.SETTINGS_TRACKME.key);
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean newVal=(Boolean) newValue;
				if(newVal)
				{
					
					preference.setSummary(R.string.pref_enable_trackme_desc_enabled);
					
				}
				else
				{
					preference.setSummary(R.string.pref_enable_trackme_desc);
				}
				UpdateTrackMeEnabled(newVal);
				
				return true;
			}
		});
		
		findPreference(Preferences.Keys.ENABLE_LIVETRACK.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			Preference settingsLivetrack=findPreference(Preferences.Keys.SETTINGS_LIVETRACK.key);
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean newVal=(Boolean) newValue;
				UpdateLiveTrackEnabled(newVal);
				AlarmManager alarmManager=(AlarmManager) getSystemService(ALARM_SERVICE);
				int recurTime=appPreferences.getFBInterval();
				
				Intent fbIntent=new Intent();
				fbIntent.setAction(Consts.FB_POST_ACTION);
				fbIntent.setClass(getBaseContext(), LiveTrackService1.class);
				
				PendingIntent fbPendingIntent=PendingIntent.getService(getBaseContext(), 0, fbIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				if(newVal)
				{
					
					alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, 10000, fbPendingIntent );
					Log.v(App.TAG, "Scheduling FB Posts" );
				}
				else
				{
					alarmManager.cancel(fbPendingIntent);
					Log.v(App.TAG, "Cancelled live track service" );
				}
				return true;
			}
		});
		
		
		findPreference(Preferences.Keys.ALLOW_CONTACTS.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean newVal=(Boolean) newValue;
				findPreference(Preferences.Keys.SELECT_CONTACTS.key).setEnabled(newVal);
				
				return true;
					
			}
		});
		
		/*findPreference(Preferences.Keys.ABOUT.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				show(Dialogs.ABOUT_DIALOG);
				return true;
				
			}
		});*/
	}


	

}
