package in.co.madhur.mapmylocation.location;



import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

public class LocationGetter
{
	private final Context context;
	private Location location = null;
	private final Object gotLocationLock = new Object();
	private Coordinates coordinates;
	private final LocationResult locationResult = new LocationResultChild();
	

	public LocationGetter(Context context)
	{
		if (context == null)
			throw new IllegalArgumentException("context == null");

		this.context = context;
	}

	public synchronized Coordinates getLocation(int maxWaitingTime, int updateTimeout)
	{
		try
		{

			Log.v("Tag", "Main Thread Name: " + Thread.currentThread().getName()
					+ "Thread ID: " + Thread.currentThread().getId());
			
			final int updateTimeoutPar = updateTimeout;
			synchronized (gotLocationLock)
			{
				new Thread()
				{
					public void run()
					{
						
						Log.v("Tag", "Thread Name: " + Thread.currentThread().getName()
								+ "Thread ID: " + Thread.currentThread().getId());

						Log.v("Tag", "Creating looper");
						
						
						Looper.prepare();
						LocationResolver locationResolver = new LocationResolver();
						locationResolver.prepare();
						locationResolver.getLocation(context, locationResult, updateTimeoutPar);
						Looper.loop();
					}
				}.start();

				gotLocationLock.wait(maxWaitingTime);
			}
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}

		if (location != null)
			coordinates = new Coordinates(location.getLatitude(), location.getLongitude());
		else
			coordinates = Coordinates.UNDEFINED;
		return coordinates;
	}
	
	public class LocationResultChild extends LocationResult
	{

		@Override
		public void gotLocation(Location location)
		{
			
				synchronized (gotLocationLock)
				{
					LocationGetter.this.location = location;
					gotLocationLock.notifyAll();
					Looper.myLooper().quit();
				}
			

		}

	}

	
	public boolean isProvidersEnabled()
	{
		LocationManager locationManager=(LocationManager) context.getSystemService(context.LOCATION_SERVICE);
		boolean gpsProvider = false, networkProvider = false;
		try
		{
			gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		catch (Exception ex1)
		{
			Log.v("Tag", "Provider not permitted");
		}
		try
		{
			networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (Exception ex)
		{
			Log.v("Tag", "Provider not permitted");
		}

		// don't start listeners if no provider is enabled
		if (!gpsProvider && !networkProvider)
		{
			Log.v("Tag", "No Provider is enabled");
			return false;
			
		}
		
		return true;
	}
	
	
	
}