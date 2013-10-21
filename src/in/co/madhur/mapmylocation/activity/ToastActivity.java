package in.co.madhur.mapmylocation.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ToastActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent notiIntent=getIntent();
		if(notiIntent!=null)
		{
			String message=notiIntent.getStringExtra("message");
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			
		}
		
		finish();
	}
}
