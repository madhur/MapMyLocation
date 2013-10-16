package in.co.madhur.mapmylocation.tasks;

import java.util.concurrent.atomic.AtomicInteger;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationGetter;

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
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;


public class LocationTask extends AsyncTask<Integer, Integer, Coordinates> 
{
	Context context;
	boolean showNotification;
	String sender, dispSender;
	int notificationId;
	int msgStamp;
	BroadcastReceiver smsSentReciever, smsDeliveredReciver;
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
	
	public LocationTask(Context context, boolean showNotification, String sender, String dispSender, int msgStamp)
	{
		this.context=context;		
		this.showNotification=showNotification;
		this.sender=sender;
		this.msgStamp=msgStamp;
		this.notificationId=msgStamp;
		this.dispSender=dispSender;
	}

	@Override
	protected Coordinates doInBackground(Integer... params)
	{
		LocationGetter getter=new LocationGetter(context);
		Coordinates location=getter.getLocation(params[0], params[0]);
		return location;
	}
	
	@Override
	protected void onPostExecute(Coordinates result)
	{
		
		final NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		if(result==null && showNotification)
		{
			Log.v(App.TAG, "Location Task: Could not obtain location, returning");
			
			NotificationCompat.Builder builder=GetNotificationBuilder(sender, NotificationType.LOCATION_FAILURE);
			
			Log.v(App.TAG, "Notification1: " + msgStamp);
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
						Log.v(App.TAG, "Notification2: " + msgStamp);
						NotificationCompat.Builder builder=GetNotificationBuilder(sender, NotificationType.OUTGOING_SMS);
						
						nm.notify((int) msgStamp, builder.build());

						break;
						
					case Activity.RESULT_CANCELED:
						
						break;
						
					default:
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
						NotificationCompat.Builder builder=GetNotificationBuilder(sender, NotificationType.OUTGOING_SMS);
						
						Log.v(App.TAG, "Notification3: " + msgStamp);
						nm.notify((int) msgStamp, builder.build());
						break;
						
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						
						break;
						
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						
						break;

					case SmsManager.RESULT_ERROR_NULL_PDU:
						
						break;

					case SmsManager.RESULT_ERROR_RADIO_OFF:
					
					break;
					
					default:
						Log.v(App.TAG, String.valueOf(getResultCode()));

					
					}
					
					
					arg0.unregisterReceiver(this);
				}
			};
			
			
			context.registerReceiver(smsSentReciever, new  IntentFilter(Consts.SENT));
			context.registerReceiver(smsDeliveredReciver, new IntentFilter(Consts.DELIVERED));
			
		}
		
		sendSMS(result);
		
	}
	
	private void sendSMS(Coordinates result)
	{
		PendingIntent sentPI, deliveredPI;
		
		
		sentPI=PendingIntent.getBroadcast(context, 0, new Intent(Consts.SENT), 0);
		deliveredPI=PendingIntent.getBroadcast(context, 0, new Intent(Consts.DELIVERED), 0);
		
		SmsManager smsManager=SmsManager.getDefault();
		
		String message=Consts.GOOGLE_MAPS_URL+result.getLatitude()+","+result.getLongitude();
		
		smsManager.sendTextMessage(sender, null, message, sentPI, deliveredPI);
		
	}

	
	private NotificationCompat.Builder GetNotificationBuilder(String sender, NotificationType type)
	{
		NotificationCompat.Builder noti=new NotificationCompat.Builder(context);
		noti.setContentTitle(dispSender);
		noti.setAutoCancel(true);
		
		switch(type)
		{
		case INCOMING_SMS:
			noti.setTicker(dispSender+": "+context.getString(R.string.noti_loc_request));		
			noti.setContentText(context.getString(R.string.noti_loc_response));
			
			break;
			
		case OUTGOING_SMS:
			noti.setTicker(dispSender+": "+context.getString(R.string.noti_loc_response));		
			noti.setContentText(context.getString(R.string.noti_loc_response));
			break;
			
		case  LOCATION_FAILURE:
			noti.setTicker(dispSender+": "+context.getString(R.string.noti_loc_failure));		
			noti.setContentText(context.getString(R.string.noti_loc_failure));
			
			break;
			
		default:
			break;
		
		}
		
		
		noti.setSmallIcon(R.drawable.ic_notification);
				
		noti.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		
		PendingIntent notifyIntent=PendingIntent.getActivity(context, 0, new Intent(), 0);
		
		noti.setContentIntent(notifyIntent);
		
		return noti;
	}
	
//	private void createCompatibleSecondLine(CharSequence pTitle,
//			Notification.Builder pBuilder, InboxStyle pInboxStyle)
//	{
//		// set the text for pre API 16 devices (or for expanded)
//		if (android.os.Build.VERSION.SDK_INT < 16)
//		{
//			Log.v(App.TAG, "setcontenttext");
//			pBuilder.setContentText(pTitle);
//		}
//		else
//		{
//			Log.v(App.TAG, "setsummarytext");
//			pInboxStyle.setSummaryText(pTitle);
//			
//		}
//	}
	
	
	
}