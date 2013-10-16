package in.co.madhur.mapmylocation.tasks;

import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationGetter;

import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.telephony.SmsManager;


public class LocationTask extends AsyncTask<Integer, Integer, Coordinates> 
{
	Context context;
	boolean showNotification;
	String sender;
	int notificationId;
	long timeStamp;
	BroadcastReceiver smsSentReciever, smsDeliveredReciver;
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		
		if(showNotification)
		{
			NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			
			Builder builder=GetNotificationBuilder(sender , NotificationType.INCOMING_SMS);
			
			nm.notify((int) timeStamp, builder.build());
			
		}
	}
	
	public LocationTask(Context context, boolean showNotification, String sender, long timeStamp)
	{
		this.context=context;		
		this.showNotification=showNotification;
		this.sender=sender;
		this.timeStamp=timeStamp;
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
			
			Builder builder=GetNotificationBuilder(sender, NotificationType.LOCATION_FAILURE);
			
			nm.notify((int) timeStamp, builder.build());
			
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
						Builder builder=GetNotificationBuilder(sender, NotificationType.OUTGOING_SMS);
						
						nm.notify((int) timeStamp, builder.build());

						break;
						
					case Activity.RESULT_CANCELED:
						
						break;
						
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
						
						break;
						
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						
						break;
						
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						
						break;

					case SmsManager.RESULT_ERROR_NULL_PDU:
						
						break;

					case SmsManager.RESULT_ERROR_RADIO_OFF:
					
					break;

					
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

	private Builder GetNotificationBuilder(String sender, NotificationType type)
	{
		Builder noti=new Notification.Builder(context);
		
		noti.setAutoCancel(true);
		
		if(type==NotificationType.INCOMING_SMS)
		{
			noti.setTicker(context.getString(R.string.noti_loc_request) + sender);		
		
			noti.setContentTitle(context.getString(R.string.noti_loc_request) + sender);
		}
		else if(type==NotificationType.OUTGOING_SMS)
		{
			
			
			noti.setTicker(context.getString(R.string.noti_loc_response) + sender);		
			
			noti.setContentTitle(context.getString(R.string.noti_loc_response) + sender);
		}
		
		//noti.setContentText("Hey this is my notification text");
		
		noti.setSmallIcon(R.drawable.ic_notification);
				
		noti.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		
		PendingIntent notifyIntent=PendingIntent.getActivity(context, 0, new Intent(), 0);
		
		noti.setContentIntent(notifyIntent);
		
		return noti;
		
		
	}
	
	
	
	
}