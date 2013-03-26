package edu.neu.madcourse.jameshardy.MultiplayerBoggle;

import android.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;
import android.util.Log;

public class MP_BoggleReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;

		Log.d("BOGGLE", "RECEIVED SMS");
		// received SMS

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_notification_overlay)
				.setContentTitle("My Notification")
				.setContentText("HELLO RECEIVED SMS DATA MSG");

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Hide the notification after its selected
		// noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, mBuilder.build());

		/*
		 * if (bundle != null) { // received SMS
		 * 
		 * NotificationCompat.Builder mBuilder = new
		 * NotificationCompat.Builder(context)
		 * .setSmallIcon(R.drawable.ic_notification_overlay)
		 * .setContentTitle("My Notification")
		 * .setContentText("HELLO RECEIVED SMS DATA MSG");
		 * 
		 * NotificationManager notificationManager =
		 * (NotificationManager)context
		 * .getSystemService(Context.NOTIFICATION_SERVICE);
		 * 
		 * // Hide the notification after its selected //noti.flags |=
		 * Notification.FLAG_AUTO_CANCEL;
		 * 
		 * notificationManager.notify(0, mBuilder.build()); }
		 */

	}

}
