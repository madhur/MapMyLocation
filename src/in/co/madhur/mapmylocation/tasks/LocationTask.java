package in.co.madhur.mapmylocation.tasks;

import java.util.concurrent.atomic.AtomicInteger;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.activity.ToastActivity;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationGetter;
import in.co.madhur.mapmylocation.location.LocationResolver;
import in.co.madhur.mapmylocation.location.LocationResult;
import in.co.madhur.mapmylocation.service.SMSService;
import in.co.madhur.mapmylocation.util.AppLog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.InboxStyle;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;

import static in.co.madhur.mapmylocation.App.LOG;
import static in.co.madhur.mapmylocation.App.LOCAL_LOGV;
import static in.co.madhur.mapmylocation.App.TAG;

public class LocationTask extends AsyncTask<Integer, Integer, Coordinates> 
{
	SMSService context;
	boolean showNotification;
	String sender, dispSender;
	int notificationId;
	int msgStamp;
	BroadcastReceiver smsSentReciever, smsDeliveredReciver;
	
	
	private Location location = null;
	private final Object gotLocationLock = new Object();
	private Coordinates coordinates;
	private final LocationResult locationResult = new LocationResultChild();
	AppLog appLog;
	//private static AtomicInteger notificationCounter;
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		
		if(showNotification)
		{
			NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			
			NotificationCompat.Builder builder=GetNotificationBuilder(sender , NotificationType.INCOMING_SMS);
			
			nm.notify((int) msgStamp, builder.build());
			
		}
	}
	
	
	
	public LocationTask(SMSService context, boolean showNotification, String sender, String dispSender, int msgStamp, AppLog appLog)
	{
		this.context=context;		
		this.showNotification=showNotification;
		this.sender=sender;
		this.msgStamp=msgStamp;
		this.notificationId=msgStamp;
		this.dispSender=dispSender;
		this.appLog=appLog;
	}

	@Override
	protected Coordinates doInBackground(Integer... params)
	{
		if(LOCAL_LOGV)
			Log.v(TAG, "doInBackground("+params+")");
		
		Coordinates location=getLocation(Consts.MAX_WAIT_TIME, Consts.UPDATE_TIMEOUT);
		return location;
	}
	
	@Override
	protected void onPostExecute(Coordinates result)
	{
		if(LOCAL_LOGV)
			Log.v(TAG, "onPostExecute("+result+")");
		
		final NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		if(result==null && showNotification)
		{
			if(LOCAL_LOGV)
				Log.v(App.TAG, "PostExecute: Could not obtain location, returning");
			
			appLog.append("PostExecute: Could not obtain location");
			
			NotificationCompat.Builder builder=GetNotificationBuilder(sender, NotificationType.LOCATION_FAILURE);
			
			if(LOCAL_LOGV)
				Log.v(App.TAG, "Notification for failure: " + msgStamp);
			nm.notify((int) msgStamp, builder.build());
			
			return;

		}
		else if(showNotification)
		{
			smsDeliveredReciver=new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					// TODO Auto-generated method stub
					
					switch(getResultCode())
					{
					case Activity.RESULT_OK:
						if(LOCAL_LOGV)
							Log.v(App.TAG, "Notification for delivered: " + msgStamp);
						NotificationCompat.Builder builder=GetNotificationBuilder(sender, NotificationType.OUTGOING_SMS);
						nm.notify((int) msgStamp, builder.build());
						appLog.append("SMS sent with location");
						
						break;
						
					case Activity.RESULT_CANCELED:
						
						break;
						
					default:
						if(LOCAL_LOGV)
							Log.v(App.TAG, String.valueOf(getResultCode()));
						
					}
					
					arg0.unregisterReceiver(this);

				}
			};
			
			
			smsSentReciever =new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					// TODO Auto-generated method stub
					switch(getResultCode())
					{
					case Activity.RESULT_OK:
						if(LOCAL_LOGV)
							Log.v(App.TAG, "Notification for sent: " + msgStamp);
						break;
						
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					case SmsManager.RESULT_ERROR_NO_SERVICE:
					case SmsManager.RESULT_ERROR_NULL_PDU:
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						appLog.append("Error sending SMS. Result code: " + getResultCode());
					
					break;
					
					default:
					
					}
					
					
					arg0.unregisterReceiver(this);
				}
			};
			
			
			context.registerReceiver(smsSentReciever, new  IntentFilter(Consts.SENT));
			context.registerReceiver(smsDeliveredReciver, new IntentFilter(Consts.DELIVERED));
			
		}
		
		sendSMS(result);
		
	}
	
	private synchronized Coordinates getLocation(int maxWaitingTime, int updateTimeout)
	{
		try
		{
			if(LOCAL_LOGV)
			Log.v(App.TAG, "getLocation(" + String.valueOf(maxWaitingTime)+","+String.valueOf(updateTimeout)+")");
			
			final int updateTimeoutPar = updateTimeout;
			synchronized (gotLocationLock)
			{
				new Thread()
				{
					public void run()
					{
						if(LOCAL_LOGV)
						{
						Log.v(TAG, "Creating looper");
						Log.v(TAG, "Thread Name: " + Thread.currentThread().getName()
								+ "Thread ID: " + Thread.currentThread().getId());
						}
						
						
						
						Looper.prepare();
						LocationResolver locationResolver = new LocationResolver(context, appLog);
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
			Log.e(TAG, e1.getMessage());
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
					LocationTask.this.location = location;
					gotLocationLock.notifyAll();
					Looper.myLooper().quit();
				}
			

		}

	}

	
	private void sendSMS(Coordinates result)
	{
		PendingIntent sentPI, deliveredPI;
		
		sentPI=PendingIntent.getBroadcast(context, 0, new Intent(Consts.SENT), 0);
		deliveredPI=PendingIntent.getBroadcast(context, 0, new Intent(Consts.DELIVERED), 0);
		
		SmsManager smsManager=SmsManager.getDefault();
		
		String message= String.format(Consts.GOOGLE_MAPS_URL,result.getLatitude(), result.getLongitude());
		
		smsManager.sendTextMessage(sender, null, message, sentPI, deliveredPI);
		
	}
	
//	private void log(Context context, String message)
//	{
//		Log.d(TAG, message);
//			new AppLog(DateFormat.getDateFormatOrder(context))
//					.appendAndClose(message);
//	}

	
	private NotificationCompat.Builder GetNotificationBuilder(String sender, NotificationType type)
	{
		NotificationCompat.Builder noti=new NotificationCompat.Builder(context);
		noti.setContentTitle(dispSender);
		noti.setAutoCancel(true);
		String contentText = null;
		
		switch(type)
		{
		case INCOMING_SMS:
			contentText=context.getString(R.string.noti_loc_request);
			noti.setTicker(dispSender+": "+context.getString(R.string.noti_loc_request));		
			
			break;
			
		case OUTGOING_SMS:
			contentText=context.getString(R.string.noti_loc_response);
			noti.setTicker(dispSender+": "+context.getString(R.string.noti_loc_response));		
			
			break;
			
		case  LOCATION_FAILURE:
			contentText=context.getString(R.string.noti_loc_failure);
			noti.setTicker(dispSender+": "+context.getString(R.string.noti_loc_failure));		
			
			break;
			
		default:
			break;
		
		}
		
		noti.setContentText(contentText);
		noti.setSmallIcon(R.drawable.ic_notification);
				
		noti.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		
		Intent toastIntent=new Intent();
		toastIntent.putExtra("message", contentText);
		toastIntent.setClass(context, ToastActivity.class);
		PendingIntent notifyIntent=PendingIntent.getActivity(context, 0, toastIntent, 0);
		
		noti.setContentIntent(notifyIntent);
		
		return noti;
	}
	
	
}