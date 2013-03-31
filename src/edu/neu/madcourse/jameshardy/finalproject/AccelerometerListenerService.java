package edu.neu.madcourse.jameshardy.finalproject;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.MultiplayerBoggle.MP_BoggleUser;
import edu.neu.mobileclass.apis.KeyValueAPI;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import edu.neu.madcourse.jameshardy.finalproject.SoapGUI;

public class AccelerometerListenerService extends Service implements
		SensorEventListener {
	private static final String TAG = "AccelerometerListener";

	//public static final String BROADCAST_ACTION = "edu.neu.madcourse.jameshardy.finalproject.send_count";
	//public static final String HANDWASH_COUNT = "edu.neu.madcourse.jameshardy.finalproject.wash_count";

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	float x;
	float y;
	float z;
	private float last_x, last_y, last_z;
	private Vibrator v;

	long lastEventTime = 0;
	long curTime = 0;

	private PowerManager mgr;
	private WakeLock wakelock;

	private static final int SHAKE_THRESHOLD = 9;
	private static final int Z_THRESHOLD = 20;

	// BroadcastReceiver for handling ACTION_SCREEN_OFF.
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Check action just to be on the safe side.
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.v("shake mediator screen off", "trying re-registration");
				// Unregisters the listener and registers it again.
				mSensorManager
						.unregisterListener(AccelerometerListenerService.this);
				mSensorManager.registerListener(
						AccelerometerListenerService.this, mAccelerometer,
						SensorManager.SENSOR_DELAY_GAME);
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Log.d(TAG, "START COMMAND");
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
		// Log.d(TAG, "SERVICE STARTED");
		v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

		mgr = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakelock = mgr
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakelock.acquire();

		// Register our receiver for the ACTION_SCREEN_OFF action. This will
		// make our receiver
		// code be called whenever the phone enters standby mode.
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Unregister our receiver.
        unregisterReceiver(mReceiver);
		
		mSensorManager.unregisterListener(this);
		Log.d(TAG, "SERVICE onDestroy() called");
		wakelock.release();
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

		curTime = System.currentTimeMillis();
		// only allow one update every 100ms.
		if ((curTime - lastEventTime) < 50) {
			long diffTime = (curTime - lastEventTime);
			lastEventTime = curTime;
			float speed = Math.abs(x + y + z - last_x - last_y - last_z)
					/ diffTime * 1000;

			if (tap_count == 3) {
				handwash_count++;
				Log.d(TAG, "REGISTERED WASHED HANDS");
				v.vibrate(300);

				// broadcast update
				Intent broadcast = new Intent();
				Bundle b = new Bundle();
				b.putInt(SoapGUI.HANDWASH_COUNT, handwash_count);
				broadcast.putExtras(b);
				broadcast.setAction(SoapGUI.BROADCAST_ACTION);
				sendBroadcast(broadcast);

				tap_count = 0;
				IS_FIRST_TAP = true;
			}
			highPassFilter(x, y, z, diffTime);

			// Log.d("sensor", "diff: " + diffTime + " - speed: " + speed);
			if (speed > SHAKE_THRESHOLD) {
				// Log.d(TAG, "Z = " + z);
				// Log.d("sensor", "shake detected w/ speed: " + speed);
				/*
				 * Toast.makeText(this, "shake detected w/ speed: " + speed,
				 * Toast.LENGTH_SHORT).show();
				 */
			}
			last_x = x;
			last_y = y;
			last_z = z;
		} else {
			lastEventTime = curTime;
		}

		/*
		 * Log.d(TAG, Calendar.getInstance().getTime().getTime() - lastEventTime
		 * + " at:: " + x + " " + y + " " + z);
		 */

	}

	private static final boolean ADAPTIVE_ACCEL_FILTER = true;
	private static boolean POTENTIAL_TAP = false;
	private static boolean IS_FIRST_TAP = true;
	float lastAccel[] = new float[3];
	float accelFilter[] = new float[3];
	float prevAccelFilter[] = new float[3];
	/*
	 * float z_stack[] = new float[5]; private int stack_count = 0; private int
	 * tap_count = 0; private int registered_taps = 0;
	 */
	private int count_down = 6;
	private int tap_count = 0;
	private long start_time = 0;
	private int handwash_count = 0;

	/**
	 * Adaptation of Apple's high pass filter Gotta be good since it's an apple
	 * product
	 * 
	 * @param accelX
	 * @param accelY
	 * @param accelZ
	 */
	public void highPassFilter(float accelX, float accelY, float accelZ,
			long diffTime) {
		// high pass filter
		float updateFreq = 30; // match this to your update speed
		float cutOffFreq = 1.0f;
		float RC = 1.0f / cutOffFreq;
		float dt = 1.0f / updateFreq;
		float filterConstant = RC / (dt + RC);
		float alpha = filterConstant;
		float kAccelerometerMinStep = 0.033f;
		float kAccelerometerNoiseAttenuation = 3.0f;

		if (ADAPTIVE_ACCEL_FILTER) {
			float d = clamp(
					Math.abs(norm(accelFilter[0], accelFilter[1],
							accelFilter[2]) - norm(accelX, accelY, accelZ))
							/ kAccelerometerMinStep - 1.0f, 0.0f, 1.0f);
			alpha = d * filterConstant / kAccelerometerNoiseAttenuation
					+ (1.0f - d) * filterConstant;
		}

		accelFilter[0] = (float) (alpha * (accelFilter[0] + accelX - lastAccel[0]));
		accelFilter[1] = (float) (alpha * (accelFilter[1] + accelY - lastAccel[1]));
		accelFilter[2] = (float) (alpha * (accelFilter[2] + accelZ - lastAccel[2]));

		lastAccel[0] = accelX;
		lastAccel[1] = accelY;
		lastAccel[2] = accelZ;

		/*
		 * Log.d(TAG, Calendar.getInstance().getTime().getTime() - lastUpdate +
		 * " at:: " + accelFilter[0] + " " + accelFilter[1] + " " +
		 * accelFilter[2]);
		 */
		// MONITOR Z's
		// Log.d(TAG, diffTime + " at:: " + accelFilter[2]);

		// processing middle to end of tap
		if (POTENTIAL_TAP == true) {
			if (count_down > 0) {
				count_down--;
				// end of Tap
				if (Math.abs(accelFilter[2]) < .1) {
					tap_count++;
					// reset
					count_down = 6;
					POTENTIAL_TAP = false;
					// start timing between taps
					start_time = curTime;
					// IS_FIRST_TAP = false;
				}
			}
			// not tap
			else {
				// reset
				count_down = 5;
				POTENTIAL_TAP = false;
			}
		}

		// handling beginning of tap
		if (Math.abs(accelFilter[2]) > .4 && Math.abs(accelFilter[2]) < 1.5) {
			if (POTENTIAL_TAP == false) {
				// start of Tap
				if (Math.abs(prevAccelFilter[2]) < .1) {
					// mark as potential tap
					POTENTIAL_TAP = true;
					if (!IS_FIRST_TAP) {
						if ((curTime - start_time) > 1000) {
							// taps too far apart, treat as first tap
							tap_count = 0;
							// IS_FIRST_TAP = true;
						}
					} else {
						// first tap, set to false after
						IS_FIRST_TAP = false;
					}
				}
			}

			/*
			 * if (stack_count < 5) { z_stack[stack_count] =
			 * Math.abs(accelFilter[2]); stack_count++; } else stack_count = 0;
			 */

			// Log.d(TAG, diffTime + " at:: " + accelFilter[0] + " " +
			// accelFilter[1] + " " + accelFilter[2]);
		}
		prevAccelFilter[0] = accelFilter[0];
		prevAccelFilter[1] = accelFilter[1];
		prevAccelFilter[2] = accelFilter[2];
	}

	private float norm(float x, float y, float z) {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	private float clamp(float v, float min, float max) {
		if (v > max)
			return max;
		else if (v < min)
			return min;
		else
			return v;
	}

}