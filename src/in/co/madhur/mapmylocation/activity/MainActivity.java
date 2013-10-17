package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.preferences.Preferences.Keys.*;
import in.co.madhur.mapmylocation.util.AppLog;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;


public class MainActivity extends PreferenceActivity
{
	Preferences appPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		appPreferences=new Preferences(this);
		
		SetListeners();
		
		UpdateFBIntervalLabel(null);
		UpdateMaxRateLabel(null);
		
		
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
			
		default:
			return null;
		}
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
				FBInterval.setTitle(FBInterval.getEntry());					
			
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
		
		
		findPreference(Preferences.Keys.MAX_RATE.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				
				UpdateMaxRateLabel(newValue.toString());
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
					
					// preference.setsu
				}
				return true;
			}
		});
		
		findPreference(Preferences.Keys.ENABLE_TRACKME.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean newVal=(Boolean) newValue;
				if(newVal)
				{
					
					preference.setSummary(R.string.pref_enable_trackme_desc_enabled);
				}
				else
					preference.setSummary(R.string.pref_enable_trackme_desc);
				return true;
			}
		});
		
		findPreference(Preferences.Keys.ENABLE_LIVETRACK.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
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
		
		findPreference(Preferences.Keys.ABOUT.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				show(Dialogs.ABOUT_DIALOG);
				return true;
				
			}
		});
	}
	

}
