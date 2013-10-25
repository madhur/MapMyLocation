package in.co.madhur.mapmylocation.location;

import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.util.AppLog;
import android.content.Context;
import android.location.LocationManager;
import android.text.format.DateFormat;
import android.util.Log;

public class LocationGetter
{
		
	
	public static boolean isProvidersEnabled(Context context)
	{
		LocationManager locationManager=(LocationManager) context.getSystemService(context.LOCATION_SERVICE);
		boolean gpsProvider = false, networkProvider = false;
		try
		{
			gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		catch (Exception ex1)
		{
			Log.e(App.TAG, "Provider not permitted");
			log(context, "GPS Provider not permitted");
		}
		try
		{
			networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (Exception ex)
		{
			Log.e(App.TAG, "Provider not permitted");
			log(context, "Network Provider not permitted");
		}

		// don't start listeners if no provider is enabled
		if (!gpsProvider && !networkProvider)
		{
			if(LOCAL_LOGV)
				Log.v("Tag", "No Provider is enabled");
			return false;
			
		}
		
		return true;
	}
	
	private static void log(Context context, String message)
	{
		Log.d(TAG, message);
			new AppLog(DateFormat.getDateFormatOrder(context))
					.appendAndClose(message);
	}
	
	
	
}