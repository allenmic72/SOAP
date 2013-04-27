package edu.neu.madcourse.jameshardy.finalproject;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.MultiplayerBoggle.MP_BoggleUser;
import edu.neu.mobileclass.apis.KeyValueAPI;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import edu.neu.madcourse.jameshardy.finalproject.SoapSettings;

public class TapListenerService extends Service implements
		SensorEventListener {
	private static final String TAG = "TapListener";

	public static final String SPREF = "soapPreferences";
	public static final String NUMDAYS_PREF = "number_days";
	public static final String DAY_PREF = "current_day";
	public static final String DAYCOUNT_PREF = "dailycount";
	public static final String TOTALCOUNT_PREF = "totalcount";
	public static final String MANUAL_START_EXTRA = "MANUAL";
	public static final int MANUAL_FLAG = 1;
	private long timeToRun;
	
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
	private static final int TAP_THRESHOLD = 2; //was 3
	
	SoapSettingsHolder settings;
	CountDownTimer timer;

	//TODO MAKE SURE TO ONLY REGISTER SERVICE ON SCREEN OFF
	
	// BroadcastReceiver for handling ACTION_SCREEN_OFF.
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//start listener when screens off
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				mSensorManager.registerListener(
						TapListenerService.this, mAccelerometer,
						SensorManager.SENSOR_DELAY_GAME);
			}
			//stop listener when screens on
			else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				mSensorManager.unregisterListener(TapListenerService.this);
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getIntExtra(MANUAL_START_EXTRA, 0) != MANUAL_FLAG){
			//automatically started, set timer to kill service at correct time
			//but first check if the service should run this day
			SharedPreferences spref = getSharedPreferences(SoapSettings.settingsSharedPrefName, 0);
			String json = spref.getString(SoapSettings.settingsPrefDataKey, "");
			if (!json.equals("")){
				Gson gson = new Gson();
				settings = gson.fromJson(json, SoapSettingsHolder.class);
			}
			else{
				settings = new SoapSettingsHolder();
			}
			Calendar c = Calendar.getInstance();
			int today = c.get(Calendar.DAY_OF_WEEK);
			int startDay = settings.startDay + 1; //add one because Calendar.DAY_OF_WEEK starts at 1
			int endDay = settings.endDay + 1;
			if (endDay - today >= 0 && today - startDay >= 0){
				int currMins = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
				int endMins = settings.endTimeHour * 60 + settings.endTimeMinute;
				long runLength;
				if (endMins > currMins){ //end today
					runLength = (endMins - currMins) * 60 * 1000;
				}
				else{ //end some time next day
					runLength = 24 * 60 - currMins + endMins;
					runLength = runLength * 60 * 1000;
				}
				
				if (runLength <= 0){
					stopSelf();
					Log.d(TAG, "Error: bad run length: " + runLength);
				}
				Log.d(TAG, "service runtime in mins: " + runLength/60000);
				timer = new CountDownTimer(runLength, runLength){

					@Override
					public void onFinish() {
						Log.d(TAG, "service run time ended");
						if (settings.autoExport){
							sendExportNotification();
						}
						stopSelf();
					}

					@Override
					public void onTick(long arg0) {
					}
					
				}.start();
			}
			else{ //service doesn't run today
				Log.d(TAG, "stopping service, not scheduled to run today");
				stopSelf();
			}
		}
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
		//DONT start listener until screen off
		//mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		
		Log.d(TAG, "SERVICE STARTED");
		v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

		mgr = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

		// Register our receiver for the ACTION_SCREEN_OFF action. This will
		// make our receiver
		// code be called whenever the phone enters standby mode.
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mReceiver, filter);
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Unregister our receiver.
        unregisterReceiver(mReceiver);
		
        //unregister receiver incase
		mSensorManager.unregisterListener(this);
		Log.d(TAG, "SERVICE onDestroy() called");
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
			
			int state = highPassFilter(x, y, z, diffTime);
			
			//Log.d(TAG, "STATE: " + state);
			
			switch (heldState) {
			case PulseState.NonActive:
				heldState = state;
				break;
			case PulseState.NegativePulse:
				if (state == PulseState.PositivePulse
						&& (curTime - last_detect_time) > 500) {
					if (curTime-last_detect_time > 3000) {
						//reset tap count
						tap_count = 0;
					}
					Log.d(TAG, "TAP DETECTED");
					heldState = 0;
					last_detect_time = curTime;
					tap_count++;
				} else
					heldState = state;
				break;
			case PulseState.PositivePulse:
				if (state == PulseState.NegativePulse
						&& (curTime - last_detect_time) > 500) {
					if (curTime-last_detect_time > 3000) {
						//reset tap count
						tap_count = 0;
					}
					Log.d(TAG, "TAP DETECTED");
					heldState = 0;
					last_detect_time = curTime;
					tap_count++;
				} else
					heldState = state;
				break;
			}
			
			if (tap_count == 1) {
				if (LISTEN_FOR_HANDSHAKE == true) {
					long pattern[] = {0,300,200,300};
					v.vibrate(pattern, -1);
					//v.vibrate(2000);
					tap_count = 0;
					LISTEN_FOR_HANDSHAKE = false;
					Log.d(TAG, "Hitting handshake");
					updateSharedPref();
				} 
			}
			if (tap_count == 3) {
				v.vibrate(300);
				LISTEN_FOR_HANDSHAKE = true;
				tap_count = 0;
				//IS_FIRST_TAP = true;
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
	private static boolean LISTEN_FOR_HANDSHAKE = false;
	private static boolean POTENTIAL_TAP = false;
	private static boolean IS_FIRST_TAP = true;
	private static boolean REGISTERED_TAP = false;
	private static boolean FALSE_TAP = false;
	float lastAccel[] = new float[3];
	float accelFilter[] = new float[3];
	float prevAccelFilter[] = new float[3];
	int heldState = 0;
	long state_timestamp = 0;
	long last_detect_time = 0;
	/*
	 * float z_stack[] = new float[5]; private int stack_count = 0; private int
	 * tap_count = 0; private int registered_taps = 0;
	 */
	//private int count_down = 6;
	private int count_down = 6;
	private int tap_count = 0;
	private long last_tap_time = 0;
	private long start_time = 0;
	private int handwash_count = 0;
	
	// Accelerometer data state
	class PulseState {
		public static final int NonActive = 0;
		public static final int PositivePulse = 1;
		public static final int NegativePulse = 2;
		public static final int UnknownPulse = -1;
	};

	/**
	 * Adaptation of Apple's high pass filter Gotta be good since it's an apple
	 * product
	 * 
	 * @param accelX
	 * @param accelY
	 * @param accelZ
	 */
	public int highPassFilter(float accelX, float accelY, float accelZ,
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
		
		//Log.d(TAG, "accel x: " + accelFilter[0] + " accel y: " + accelFilter[1] + " accel z: " + accelFilter[2]);		

		lastAccel[0] = accelX;
		lastAccel[1] = accelY;
		lastAccel[2] = accelZ;
		
		if (state_timestamp == 0) {
			state_timestamp = curTime;
		}
		int prevState = heldState;
		int currState = 0;
		
		//Log.d(TAG, "z   " + accelZ);
		if ((Math.abs(Math.abs(accelZ) - 9.81)) > TAP_THRESHOLD) {
			double diffZ = Math.abs(accelZ) - 9.81;
			//Log.d(TAG, "diff   " + diffZ);
			if (diffZ > 0) {
				currState = PulseState.PositivePulse;
			}
			else {
				currState = PulseState.NegativePulse;
			}
		}
		else {
			currState = PulseState.NonActive;
		}
		
		//Log.d(TAG, "CURR STATE  " + currState);
		
		switch (prevState) {
		case PulseState.NonActive:
			//if (currState != PulseState.NonActive && (curTime - state_timestamp) < 150) {
			if (currState != PulseState.NonActive) {
				prevState = currState;
				state_timestamp = curTime;
				//Log.d(TAG, "HIT NONACTIVE");
			}
			break;
		case PulseState.NegativePulse:
			if (currState == PulseState.PositivePulse && (curTime - state_timestamp) < 150) {
				prevState = currState;
				state_timestamp = curTime;
				//Log.d(TAG, "HIT NEG");
			}
			break;
		case PulseState.PositivePulse:
			if (currState == PulseState.NegativePulse && (curTime - state_timestamp) < 150) {
				prevState = currState;
				state_timestamp = curTime;
				//Log.d(TAG, "HIT POS");
			}
			break;
		}
		
		//prevState = currState;
				
		prevAccelFilter[0] = accelFilter[0];
		prevAccelFilter[1] = accelFilter[1];
		prevAccelFilter[2] = accelFilter[2];
		
		//return prevState;
		return currState;
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
	
	/**
	 * updates the count for this day in the shared pref
	 * the key value is the current day in the current year (1 - 365)
	 */
	public void updateSharedPref(){
		/*
		 * Shared Preferences
		 * <numdays,#>
		 * <currentday,#>
		 * <daycount, #>
		 * <totalcount, #>
		 */
	
		Calendar c = Calendar.getInstance();
		int currDay = c.get(Calendar.DAY_OF_YEAR);
		String currDay_str = currDay + "";
		Timestamp t = new Timestamp(c.getTime().getTime());
		String timestamp = t.toString();
		
		SharedPreferences sprefs = getSharedPreferences(SPREF, 0);
		Editor e = sprefs.edit();
		
		Gson g = new Gson();
		Type listTimestamps = new TypeToken<List<String>>(){}.getType();
		
		int storedDay = sprefs.getInt(DAY_PREF, 0);
		// new day of counting
		
		if (currDay > storedDay) {
			//reset the daily count to one
			e.putInt(DAYCOUNT_PREF, 1);
			int totalCnt = sprefs.getInt(TOTALCOUNT_PREF, 0);
			e.putInt(TOTALCOUNT_PREF, ++totalCnt);
			e.putInt(DAY_PREF, currDay);
			int numDays = sprefs.getInt(NUMDAYS_PREF, 0);
			e.putInt(NUMDAYS_PREF, ++numDays);
			//
			
			String timestamp_str = sprefs.getString(currDay_str, "");
			//Log.d(TAG, "NULL CHECK " + timestamp_str);
			List<String> timestampList = new ArrayList<String>(){};
			//Log.d(TAG, "NULL CHECK " + timestampList.toString());
			if (!timestamp_str.isEmpty()) {
				timestampList = g.fromJson(timestamp_str, listTimestamps);
			}
			timestampList.add(timestamp);
			timestamp_str = g.toJson(timestampList, listTimestamps);
			e.putString(currDay_str, timestamp_str);
			
		}
		
		// same day
		else {
			int dailyCnt = sprefs.getInt(DAYCOUNT_PREF, 0);
			e.putInt(DAYCOUNT_PREF, ++dailyCnt);
			int totalCnt = sprefs.getInt(TOTALCOUNT_PREF, 0);
			e.putInt(TOTALCOUNT_PREF, ++totalCnt);
			e.putInt(DAY_PREF, currDay);
			//
			String timestamp_str = sprefs.getString(currDay_str, "");
			//Log.d(TAG, "NULL CHECK " + timestamp_str);
			List<String> timestampList = new ArrayList<String>(){};
			//Log.d(TAG, "NULL CHECK " + timestampList.toString());
			if (!timestamp_str.isEmpty()) {
				timestampList = g.fromJson(timestamp_str, listTimestamps);
			}
			timestampList.add(timestamp);
			timestamp_str = g.toJson(timestampList, listTimestamps);
			e.putString(currDay_str, timestamp_str);
			
		}
		
		e.commit();
		
	}
	private void sendExportNotification(){
		
		NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder notification = 
        		new NotificationCompat.Builder(this)
       			.setSmallIcon(R.drawable.soap_launcher)
       			.setContentTitle("SOAP")
       			.setContentText("Export your data from today!")
       			.setAutoCancel(true);
        Intent notificationIntent = new Intent(this, SoapGUI.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(notificationIntent);
        
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        notification.setContentIntent(pendingIntent);
        notificationManager.notify(this.hashCode(), notification.build());

		
	}

}