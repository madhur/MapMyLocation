package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.App;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ToastActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent notiIntent=getIntent();
		if(notiIntent!=null)
		{
			
			String message=notiIntent.getStringExtra("message");
			if(App.LOCAL_LOGV)
			Log.v(App.TAG, "Toast triggered " + message);
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			
		}
		
		finish();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(App.LOCAL_LOGV)
		Log.v(App.TAG, "Toast destroyed");
	}
	
}
