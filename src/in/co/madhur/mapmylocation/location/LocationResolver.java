package in.co.madhur.mapmylocation.location;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class LocationResolver 
{
	private Timer timer;
	private LocationManager locationManager;
	private LocationResult locationResult;
	private boolean gpsEnabled = false;
	private boolean networkEnabled = false;
	private Handler locationTimeoutHandler;

	private final Callback locationTimeoutCallback = new Callback()
	{

		private void locationTimeoutFunc()
		{
			locationManager.removeUpdates(locationListenerGps);
			locationManager.removeUpdates(locationListenerNetwork);

			Location networkLocation = null, gpsLocation = null;
			if (gpsEnabled)
				gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (networkEnabled)
				networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			// We got both GPS and network location
			Log.v("Tag", "Got both gps and network location");
			if (gpsLocation != null && networkLocation != null)
			{
				if (gpsLocation.getTime() > networkLocation.getTime())
					locationResult.gotLocation(gpsLocation);
				else
					locationResult.gotLocation(networkLocation);
				return;
			}

			// We got only GPS location
			if (gpsLocation != null)
			{
				Log.v("Tag", "returning GPS last location");
				locationResult.gotLocation(gpsLocation);
				return;
				
			}
			
			// We got only network location
			if (networkLocation != null)
			{
				Log.v("Tag", "returning network last location");
				locationResult.gotLocation(networkLocation);
				return;
			}
			
			Log.v("Tag", "Could not find any location");
			// We could not find out the location
			locationResult.gotLocation(null);
		}

		@Override
		public boolean handleMessage(Message msg)
		{
			Log.v("Tag", "Executing handle message in Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());

			locationTimeoutFunc();
			return true;
		}
	};
	
	
	private final LocationListener locationListenerGps = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			Log.v("Tag", "Got Location here: Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());

			
			Log.v("Tag", "Got GPS location");
			
			timer.cancel();
			locationResult.gotLocation(location);
			locationManager.removeUpdates(this);
			locationManager.removeUpdates(locationListenerNetwork);
		}

		public void onProviderDisabled(String provider)
		{
		}

		public void onProviderEnabled(String provider)
		{
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	};
	
	
	private final LocationListener locationListenerNetwork = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			Log.v("Tag", "Got Location here: Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());
			
			Log.v("Tag", "Got network location");
			timer.cancel();
			locationResult.gotLocation(location);
			locationManager.removeUpdates(this);
			locationManager.removeUpdates(locationListenerGps);
		}

		public void onProviderDisabled(String provider)
		{
		}

		public void onProviderEnabled(String provider)
		{
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	};

	public void prepare()
	{
		Log.v("Tag", "Preparing handler here: Thread Name: " + Thread.currentThread().getName()
				+ "Thread ID: " + Thread.currentThread().getId());

		locationTimeoutHandler = new Handler(locationTimeoutCallback);
	}

	public synchronized boolean getLocation(Context context, LocationResult result, int maxMillisToWait)
	{
		locationResult = result;
		if (locationManager == null)
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		
		try
		{
			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		catch (Exception ex)
		{
			Log.v("Tag", "Provider not permitted");
		}
		try
		{
			networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (Exception ex)
		{
			Log.v("Tag", "Provider not permitted");
		}

		// don't start listeners if no provider is enabled
		if (!gpsEnabled && !networkEnabled)
		{
			Log.v("Tag", "No Provider is enabled");
			return false;
			
		}

		if (gpsEnabled)
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,locationListenerGps, Looper.myLooper());

		if (networkEnabled)
			locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListenerNetwork, Looper.myLooper());

		timer = new Timer();
		timer.schedule(new GetLastLocationTask(), maxMillisToWait);
		return true;
	}

	private class GetLastLocationTask extends TimerTask
	{
		@Override
		public void run()
		{
			Log.v("Tag", "Running  timer here: Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());
			locationTimeoutHandler.sendEmptyMessage(0);
		}
	}


	
}