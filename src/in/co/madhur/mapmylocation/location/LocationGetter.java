package in.co.madhur.mapmylocation.location;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class LocationGetter 
{
	LocationManager lm;
	Context context;
	Location bestLocation, lastknownGPS, lastknownNetwork, locGPS, locNetwork; 
	Date locTime;
	Timer t;
	
	public LocationGetter(Context context)
	{
		this.context=context;
		lm=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public Location GetLocation(Location location, String provider)
	{
		
		
		if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) )
		{
			Log.v(App.TAG, "No provider is enabled");
			return null;
		}
		
		if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, new GPSListener(), null);
				
		if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new NetworkListener(), null);
		
		
		t=new Timer();
		
		t.schedule(new TimerTask()
		{
			
			@Override
			public void run()
			{
				if(locNetwork!=null)
				{
					bestLocation=locNetwork;
					return;
					
				}
				if(locGPS!=null)
				{
					bestLocation=locGPS;
					return;
					
				}
				
				lastknownGPS=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				lastknownNetwork=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
				if(lastknownGPS!=null && lastknownNetwork!=null)
				{
					bestLocation=GetBestLocation(lastknownGPS, lastknownNetwork);
					return;
				}
				
				if(lastknownGPS!=null)
				{
					bestLocation=lastknownGPS;
					return;
				}
				
				if(lastknownNetwork!=null)
				{
					bestLocation=lastknownNetwork;
					return;
				}
				
			}
		}, System.currentTimeMillis(), Consts.GPS_FIX_TIME);
		
		
		return bestLocation;
	}
	
	private Location GetBestLocation(Location locGPS, Location locNetwork)
	{
		if(locGPS.getTime() > locNetwork.getTime())
			return locGPS;
		else
			return locNetwork;
	}

	private class GPSListener implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location)
		{
			locGPS=location;
			
			
			
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	private class NetworkListener implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location)
		{
			locNetwork=location;
			
			
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub
			
		}
		
		
		
	}

}
