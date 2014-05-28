package in.co.madhur.mapmylocation.tasks;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.Consts;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.location.Coordinates;
import in.co.madhur.mapmylocation.location.LocationResolver;
import in.co.madhur.mapmylocation.location.LocationResult;
import in.co.madhur.mapmylocation.preferences.Preferences;
import in.co.madhur.mapmylocation.service.Notifications;
import in.co.madhur.mapmylocation.service.SMSService;
import in.co.madhur.mapmylocation.util.AppLog;
import android.app.Activity;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

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
	Preferences preferences;

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		if (showNotification)
		{
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			NotificationCompat.Builder builder = Notifications.GetNotificationBuilderSMS(context, dispSender, NotificationType.INCOMING_SMS);

			nm.notify((int) msgStamp, builder.build());

		}
	}

	public LocationTask(SMSService context, boolean showNotification, String sender, String dispSender, int msgStamp, AppLog appLog, Preferences appPreferences)
	{
		this.context = context;
		this.showNotification = showNotification;
		this.sender = sender;
		this.msgStamp = msgStamp;
		this.notificationId = msgStamp;
		this.dispSender = dispSender;
		this.appLog = appLog;
		this.preferences = appPreferences;
	}

	@Override
	protected Coordinates doInBackground(Integer... params)
	{
		if (LOCAL_LOGV)
			Log.v(TAG, "doInBackground(" + params + ")");

		Coordinates location = getLocation(preferences.getThreadTimeout(), preferences.getLocationTimeout());
		return location;
	}

	@Override
	protected void onPostExecute(Coordinates result)
	{
		if (LOCAL_LOGV)
			Log.v(TAG, "onPostExecute(" + result + ")");

		final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if (result == null && showNotification)
		{
			if (LOCAL_LOGV)
				Log.v(App.TAG, "PostExecute: Could not obtain location, returning");

			appLog.append("PostExecute: Could not obtain location");

			NotificationCompat.Builder builder = Notifications.GetNotificationBuilderSMS(context, dispSender, NotificationType.LOCATION_FAILURE);

			if (LOCAL_LOGV)
				Log.v(App.TAG, "Notification for failure: " + msgStamp);
			nm.notify((int) msgStamp, builder.build());

			return;

		}
		else if (showNotification)
		{
			smsDeliveredReciver = new BroadcastReceiver()
			{
				NotificationCompat.Builder builder;

				@Override
				public void onReceive(Context arg0, Intent arg1)
				{
					switch (getResultCode())
					{
						case Activity.RESULT_OK:
							if (LOCAL_LOGV)
								Log.v(App.TAG, "Notification for delivered: "
										+ msgStamp);
							builder = Notifications.GetNotificationBuilderSMS(context, dispSender, NotificationType.OUTGOING_SMS);
							nm.notify((int) msgStamp, builder.build());
							appLog.append("SMS sent with location");

							break;

						case Activity.RESULT_CANCELED:
						default:
							builder = Notifications.GetNotificationBuilder(context, dispSender, context.getString(R.string.noti_sms_deliverederror));
							nm.notify((int) msgStamp, builder.build());

							if (LOCAL_LOGV)
								Log.v(App.TAG, String.valueOf(getResultCode()));

					}

					arg0.unregisterReceiver(this);

				}
			};

			smsSentReciever = new BroadcastReceiver()
			{
				NotificationCompat.Builder builder;

				@Override
				public void onReceive(Context arg0, Intent arg1)
				{
					switch (getResultCode())
					{
						case Activity.RESULT_OK:
							if (LOCAL_LOGV)
								Log.v(App.TAG, "Notification for sent: "
										+ msgStamp);
							break;

						case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						case SmsManager.RESULT_ERROR_NO_SERVICE:
						case SmsManager.RESULT_ERROR_NULL_PDU:
						case SmsManager.RESULT_ERROR_RADIO_OFF:
							appLog.append("Error sending SMS. Result code: "
									+ getResultCode());

							builder = Notifications.GetNotificationBuilder(context, dispSender, context.getString(R.string.noti_sms_senterror));
							nm.notify((int) msgStamp, builder.build());
							break;

						default:

					}

					arg0.unregisterReceiver(this);
				}
			};

			context.registerReceiver(smsSentReciever, new IntentFilter(Consts.SENT));
			context.registerReceiver(smsDeliveredReciver, new IntentFilter(Consts.DELIVERED));

		}

		sendSMS(result);
		preferences.setLastLocation(result);

	}

	private synchronized Coordinates getLocation(int maxWaitingTime, int updateTimeout)
	{
		try
		{
			if (LOCAL_LOGV)
				Log.v(App.TAG, "getLocation(" + String.valueOf(maxWaitingTime)
						+ "," + String.valueOf(updateTimeout) + ")");

			final int updateTimeoutPar = updateTimeout;
			synchronized (gotLocationLock)
			{
				new Thread()
				{
					public void run()
					{
						if (LOCAL_LOGV)
						{
							Log.v(TAG, "Creating looper");
							Log.v(TAG, "Thread Name: "
									+ Thread.currentThread().getName()
									+ "Thread ID: "
									+ Thread.currentThread().getId());
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

		sentPI = PendingIntent.getBroadcast(context, 0, new Intent(Consts.SENT), 0);
		deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(Consts.DELIVERED), 0);

		SmsManager smsManager = SmsManager.getDefault();

		String message = String.format(Consts.GOOGLE_MAPS_URL, result.getLatitude(), result.getLongitude());
		Log.v(App.TAG, "Sending sms to" + sender);
		smsManager.sendTextMessage(sender, null, message, sentPI, deliveredPI);

	}

}