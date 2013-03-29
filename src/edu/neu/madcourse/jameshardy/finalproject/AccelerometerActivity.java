package edu.neu.madcourse.jameshardy.finalproject;

import java.util.Calendar;

import edu.neu.madcourse.jameshardy.R;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class AccelerometerActivity extends Activity implements SensorListener {
	private final static String TAG = "SensorListener";
	// For shake motion detection.
	private SensorManager sensorMgr;
	private long lastUpdate = 0;
	private float x, y, z;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 40;

	// 800 is the value for detecting the shake...if you raise the value than
	// you
	// will need to shake harder for detection if you lower the value you
	// will need to shake softer for detection.

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.soap_gui);
		// start motion detection
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		boolean accelSupported = sensorMgr.registerListener(this,
				SensorManager.SENSOR_ACCELEROMETER,
				SensorManager.SENSOR_DELAY_GAME);

		if (!accelSupported) {
			// on accelerometer on this device
			sensorMgr.unregisterListener(this,
					SensorManager.SENSOR_ACCELEROMETER);
		}
	}

	protected void onPause() {
		if (sensorMgr != null) {
			sensorMgr.unregisterListener(this,
					SensorManager.SENSOR_ACCELEROMETER);
			sensorMgr = null;
		}
		super.onPause();
	}

	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(int sensor, float[] values) {
		// Log.d("sensor", "onSensorChanged: " + sensor);
		if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.

			if ((curTime - lastUpdate) < 20) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				x = values[SensorManager.DATA_X];
				y = values[SensorManager.DATA_Y];
				z = values[SensorManager.DATA_Z];
				
				//highPassFilter(x, y, z, diffTime);

				
				  float speed = Math.abs((x + y + z - last_x - last_y - last_z) / diffTime * 1000);
				  
				  // Log.d("sensor", "shake detected w/ speed: " + speed + " diff of time = " + diffTime);
				  
				  // Log.d("sensor", "diff: " + diffTime + " - speed: " + speed); 
				  
				  Log.d(TAG, "Z = " + z);
				  
				  /*
				  if (speed > SHAKE_THRESHOLD && speed < 100) {
				  Log.d("sensor", "shake detected w/ speed: " + speed); 
				  Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show(); 
				  }
				  */
				 
				last_x = x;
				last_y = y;
				last_z = z;
			} else {
				lastUpdate = curTime;
			}
		}
	}

	private static final boolean ADAPTIVE_ACCEL_FILTER = true;
	float lastAccel[] = new float[3];
	float accelFilter[] = new float[3];

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

		if (Math.abs(accelFilter[2]) > .8) {
			Log.d(TAG, diffTime + " at:: " + accelFilter[0] + " "
					+ accelFilter[1] + " " + accelFilter[2]);
		}
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