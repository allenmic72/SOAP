package edu.neu.madcourse.jameshardy.finalproject;

import java.util.Calendar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AccelerometerListenerService extends Service implements
		SensorEventListener {
	private static final String TAG = "AccelerometerListener";

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	float x;
	float y;
	float z;
	private float last_x, last_y, last_z;

	long lastEventTime = 0;

	private static final int SHAKE_THRESHOLD = 9;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//Log.d(TAG, "START COMMAND");
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		//Log.d(TAG, "SERVICE STARTED");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mSensorManager.unregisterListener(this);
	}

	@Override
	public IBinder onBind(Intent i) {
		return null;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO

	}

	public void onSensorChanged(SensorEvent ev) {
		x = ev.values[0];
		y = ev.values[1];
		z = ev.values[2];

		long curTime = System.currentTimeMillis();
		// only allow one update every 100ms.
		if ((curTime - lastEventTime) < 20) {
			long diffTime = (curTime - lastEventTime);
			lastEventTime = curTime;
			float speed = Math.abs(x + y + z - last_x - last_y - last_z)
					/ diffTime * 1000;

			// Log.d("sensor", "diff: " + diffTime + " - speed: " + speed);
			if (speed > SHAKE_THRESHOLD) {
				Log.d("sensor", "shake detected w/ speed: " + speed);
				/*
				Toast.makeText(this, "shake detected w/ speed: " + speed,
						Toast.LENGTH_SHORT).show();
						*/
			}
			last_x = x;
			last_y = y;
			last_z = z;
		}
		else
		{
			lastEventTime = curTime;
		}

		/*
		Log.d(TAG, Calendar.getInstance().getTime().getTime() - lastEventTime
				+ " at:: " + x + " " + y + " " + z);
				*/

	}

}