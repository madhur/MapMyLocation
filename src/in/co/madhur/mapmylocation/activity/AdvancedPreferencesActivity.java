package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AdvancedPreferencesActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.advanced_preference);
		
	}
}
