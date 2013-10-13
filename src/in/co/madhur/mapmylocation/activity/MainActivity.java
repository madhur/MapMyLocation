package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.preferences.Preferences.Keys.*;
import java.util.List;



import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;


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
		
		//getSherlock().getActionBar().show();
	}
	
	@Override
	public void onBuildHeaders(List<Header> target)
	{
		// TODO Auto-generated method stub
		super.onBuildHeaders(target);
		
		// loadHeadersFromResource(R.xml.headers, target);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, (android.view.Menu) menu);
		
		return true;
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
	}
	

}
