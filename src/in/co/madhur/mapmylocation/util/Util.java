package in.co.madhur.mapmylocation.util;

import in.co.madhur.mapmylocation.App;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

public class Util
{
	public static boolean isLocationEnabled(Context context)
	{
		LocationManager lm = null;
		boolean gps_enabled = false, network_enabled = false;
		if (lm == null)
			lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
		try
		{
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		catch (Exception ex)
		{
			Log.e(App.TAG, ex.getMessage());
		}
		try
		{
			network_enabled = lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (Exception ex)
		{
			Log.e(App.TAG, ex.getMessage());
		}

		if (!gps_enabled && !network_enabled)
		{
			return false;
		}
		
		return true;

	}
}
