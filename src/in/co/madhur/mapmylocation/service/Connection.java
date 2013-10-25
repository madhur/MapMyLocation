package in.co.madhur.mapmylocation.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Connection
{

	public static ConnectivityManager getConnectivityManager(Context context)
	{
		return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
	}
	
	private static boolean isBackGroundDataEnabled(Context context)
	{
			return getConnectivityManager(context).getBackgroundDataSetting();
	}
	
	private static boolean isConnected(Context context) 
	{
		
		NetworkInfo networkInfo= getConnectivityManager(context).getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isConnected())
			return true;
		
		return false;
	}
	
	public static boolean isNetworkGood(Context context)
	{
		if(isConnected(context) && isBackGroundDataEnabled(context))
			return true;
		return false;
	}
}


