package in.co.madhur.mapmylocation.location;

import in.co.madhur.mapmylocation.util.AppLog;

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

import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;

public class LocationResolver 
{
	private Timer timer;
	private LocationManager locationManager;
	private LocationResult locationResult;
	private boolean gpsEnabled = false;
	private boolean networkEnabled = false;
	private Handler locationTimeoutHandler;
	private AppLog appLog;

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
			
			if (gpsLocation != null && networkLocation != null)
			{
				if(LOCAL_LOGV)
					Log.v(TAG, "Got last location from both GPS and network");
				appLog.append("Retrieved last location from both GPS and network");
				
				if (gpsLocation.getTime() > networkLocation.getTime())
				{
					appLog.append("Using GPS location");
					locationResult.gotLocation(gpsLocation);
				}
				else
				{
					appLog.append("Using network location");
					locationResult.gotLocation(networkLocation);
				}
				return;
			}

			// We got only GPS location
			if (gpsLocation != null)
			{
				if(LOCAL_LOGV)
					Log.v(TAG, "Returning GPS last location");
				appLog.append("Using last GPS location");
				locationResult.gotLocation(gpsLocation);
				return;
				
			}
			
			// We got only network location
			if (networkLocation != null)
			{
				Log.v(TAG, "Returning network last location");
				appLog.append("Using last network location");
				
				locationResult.gotLocation(networkLocation);
				return;
			}
			
			if(LOCAL_LOGV)
				Log.v(TAG, "Could not find any location");
			
			appLog.append("Failure: Retrieving last locations.");
			
			// We could not find out the location
			locationResult.gotLocation(null);
		}

		@Override
		public boolean handleMessage(Message msg)
		{
			if(LOCAL_LOGV)
				Log.v(TAG, "Executing handle message in Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());

			appLog.append("Timeout retrieving location through listeners. Fallback to last location");
			locationTimeoutFunc();
			return true;
		}
	};
	
	
	private final LocationListener locationListenerGps = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			if(LOCAL_LOGV)
			{
				Log.v(TAG, "Got Location here: Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());

			
				Log.v(TAG, "Got GPS location");
			}
			
			appLog.append("Retrieved location through GPS");
			
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
			if (LOCAL_LOGV)
			{
				Log.v(TAG, "Got Location here: Thread Name: "
						+ Thread.currentThread().getName() + "Thread ID: "
						+ Thread.currentThread().getId());
				Log.v(TAG, "Got network location");
			}

			appLog.append("Retrieved location through network");
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

	public LocationResolver(Context context2, AppLog appLog)
	{
		this.appLog=appLog;
		
	}

	public void prepare()
	{
		if(LOCAL_LOGV)
		Log.v(TAG, "Preparing handler here: Thread Name: " + Thread.currentThread().getName()
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
			Log.e(TAG, "GPS Provider not permitted");
		}
		try
		{
			networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (Exception ex)
		{
			Log.e(TAG, "Network Provider not permitted");
		}

		// don't start listeners if no provider is enabled
		if (!gpsEnabled && !networkEnabled)
		{
			Log.v(TAG, "No Provider is enabled");
			appLog.append("None of the providers are enabled");
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
			if(LOCAL_LOGV)
				Log.v(TAG, "Running timeout of update listender: Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());
			locationTimeoutHandler.sendEmptyMessage(0);
		}
	}

	
//	private void log(Context context, String message)
//	{
//		Log.d(TAG, message);
//			new AppLog(DateFormat.getDateFormatOrder(context))
//					.appendAndClose(message);
//	}
	
}