package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AdvancedPreferencesActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.advanced_preference);
		
//		EditTextPreference loctimeoutPreference=findPreference(Consts.AdvancedPreferenceKeys.LOC_TIMEOUT.key);
//		EditTextPreference threadtimeoutPreference=findPreference(Consts.AdvancedPreferenceKeys.THREAD_TIMEOUT.key);
//		
//		EditText locTimeoutText=loctimeoutPreference.getEditText();
//		EditText threadTimeoutText=loctimeoutPreference.getEditText();
//		
//		locTimeoutText.getcl
	}
}
