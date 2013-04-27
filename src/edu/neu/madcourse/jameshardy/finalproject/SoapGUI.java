package edu.neu.madcourse.jameshardy.finalproject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.jameshardy.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;
//import au.com.bytecode.opencsv.CSVWriter;
import edu.neu.madcourse.jameshardy.finalproject.TapListenerService;
import edu.neu.madcourse.jameshardy.finalproject.SoapSettings;
import edu.neu.madcourse.jameshardy.finalproject.SoapSettingsHolder;

public class SoapGUI extends Activity implements OnClickListener {

	public static final String BROADCAST_ACTION = "edu.neu.madcourse.jameshardy.finalproject.send_count";
	public static final String HANDWASH_COUNT = "edu.neu.madcourse.jameshardy.finalproject.wash_count";

	private static final String settingsSharedPrefName = "SoapSettings";
	private static final String settingsPrefDataKey = "settings";
	public static final String SPREF = "soapPreferences";
	public static final String NUMDAYS_PREF = "number_days";
	public static final String DAY_PREF = "current_day";
	public static final String DAYCOUNT_PREF = "dailycount";
	public static final String TOTALCOUNT_PREF = "totalcount";

	private static final String TAG = "SOAP GUI";

	TextView lastWashTime;
	TextView countToday;
	TextView averageCount;
	ActionBarView actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soap_gui);

		Typeface helveticaLight = Typeface.createFromAsset(getAssets(),
				"helvetica_neue_light.ttf");
		TextView soapTitle = (TextView) findViewById(R.id.soap_title);
		soapTitle.setTypeface(helveticaLight);

		lastWashTime = (TextView) findViewById(R.id.soap_last_wash_time);
		lastWashTime.setTypeface(helveticaLight);
		lastWashTime.setText("");

		TextView countTodayText = (TextView) findViewById(R.id.soap_washcount_text);
		countTodayText.setTypeface(helveticaLight);
		countTodayText.setText("WASHES\nTODAY\n");

		countToday = (TextView) findViewById(R.id.soap_washcount_number);
		countToday.setTypeface(helveticaLight);

		TextView averageCountText = (TextView) findViewById(R.id.soap_average_washcount_text);
		averageCountText.setTypeface(helveticaLight);
		averageCountText.setText("DAILY\nAVERAGE\n");

		averageCount = (TextView) findViewById(R.id.soap_average_washcount_number);
		averageCount.setTypeface(helveticaLight);

		View backButton = findViewById(R.id.soap_back_button);
		backButton.setOnClickListener(this);

		View helpButton = findViewById(R.id.soap_help_button);
		helpButton.setOnClickListener(this);

		Button washButton = (Button) findViewById(R.id.soap_manual_wash_button);
		washButton.setOnClickListener(this);
		washButton.setTypeface(helveticaLight);
		
		Button unWashButton = (Button) findViewById(R.id.soap_unwash_button);
		unWashButton.setOnClickListener(this);
		unWashButton.setTypeface(helveticaLight);

	}

	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.soap_help_button:
			Intent ackActivity = new Intent(this, SoapAcknowledgements.class);
			startActivity(ackActivity);
			break;
		case R.id.soap_back_button:
			finish();
			break;
		case R.id.soap_manual_wash_button:
			updateSharedPref();
			updateGUI(false);
			break;

		}

	}

	public void startService() {
		Intent startService = new Intent(
				this,
				edu.neu.madcourse.jameshardy.finalproject.TapListenerService.class);
		startService.putExtra(TapListenerService.MANUAL_START_EXTRA,
				TapListenerService.MANUAL_FLAG);
		startService(startService);
	}

	public void stopService() {
		Intent stopService = new Intent(
				this,
				edu.neu.madcourse.jameshardy.finalproject.TapListenerService.class);
		stopService(stopService);
	}

	public void openSettingsActivity() {
		Intent settingsActivity = new Intent(this,
				edu.neu.madcourse.jameshardy.finalproject.SoapSettings.class);
		startActivity(settingsActivity);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Toast.makeText(getApplicationContext(), "received",
			// Toast.LENGTH_SHORT);
			Log.d(TAG, "Hitting onReceive broadcast");
			int count = 0;
			Bundle b = new Bundle();
			try {
				b = intent.getExtras();
				count = b.getInt(HANDWASH_COUNT);
				// updateTextField(count);
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION: " + e.toString());
			}
		}
	};

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_ACTION);
		registerReceiver(receiver, filter);
		setServiceAlarm();
		updateGUI(true);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	private void updateGUI(boolean updateFromOnResume) {
		int washCountToday = getCurrentWashCountFromSpref();
		int totalWashCount = getTotalWashCountFromSpref();
		int numDaysRecording = getNumDaysFromSpref();
		double avg = 0;
		if (numDaysRecording != 0) {
			avg = totalWashCount / numDaysRecording;
		}
		int secSinceLastWash = getTimeSinceLastWash();
		Log.d(TAG, "min = " + secSinceLastWash);
		updateTextField(washCountToday, totalWashCount, 
				avg, secSinceLastWash, updateFromOnResume);
	}

	private void updateTextField(int count, int tot_cnt, double avg, int sec,
			boolean fromOnResume) {
		// post handwash count
		if (count != 0 && avg != 0) {
			countToday.setText("" + count);
			averageCount.setText("" + avg);
		} else {
			Log.d(TAG, "count is null");
		}
		if (tot_cnt != 0) {
			if (fromOnResume) {
				if (sec > 3600) {
					int hours = sec / 3600;
					int remainder = sec - hours * 3600;
					int minutes = remainder / 60;
					int seconds = remainder - minutes * 60;

					lastWashTime.setText("Last wash was " + hours + " hours, "
							+ minutes + " minutes, and " + seconds
							+ " seconds ago!");
					// TODO: last wash over an hour ago...send notification
				} else if (sec > 60) {
					int minutes = sec / 60;
					int seconds = sec - minutes * 60;
					lastWashTime.setText("Last wash was " + minutes
							+ " minutes and " + seconds + " seconds ago.");
				} else if (sec > 0) {
					lastWashTime.setText("Last wash was " + sec
							+ " seconds ago.");
				} else {
					// min value = -1, means from another day
					lastWashTime.setText("Last wash was over a day ago!");
				}
			} else {
				lastWashTime.setText("Good job washing your hands!\n"
						+ "Try not to touch the screen\n "
						+ "next time, it's dirty!");
			}
		}
	}

	private int getTimeSinceLastWash() {
		Gson g = new Gson();
		Type listTimestamps = new TypeToken<List<String>>() {
		}.getType();
		SharedPreferences sprefs = getSharedPreferences(
				TapListenerService.SPREF, 0);
		Calendar c = Calendar.getInstance();
		int currDay = c.get(Calendar.DAY_OF_YEAR);
		String currDay_str = currDay + "";
		String timestamp_str = sprefs.getString(currDay_str, "");
		// Log.d(TAG, "NULL CHECK " + timestamp_str);
		List<String> timestampList = new ArrayList<String>() {
		};
		// Log.d(TAG, "NULL CHECK " + timestampList.toString());
		if (!timestamp_str.isEmpty()) {
			timestampList = g.fromJson(timestamp_str, listTimestamps);
		}
		if (!timestampList.isEmpty()) {
			Log.d(TAG, "hitting calc min");
			int size = timestampList.size();
			// Timestamp t = new Timestamp();
			long prev_epoch = Timestamp.valueOf(timestampList.get(size - 1))
					.getTime();
			Log.d(TAG, " prev = " + prev_epoch);
			long curr_epoch = c.getTime().getTime();
			Log.d(TAG, " curr = " + curr_epoch);
			long diff = curr_epoch - prev_epoch;
			int sec_since = (int) (diff / 1000.0);
			return sec_since;
		} else {
			return -1;
		}
	}

	private int getTotalWashCountFromSpref() {
		SharedPreferences spref = getSharedPreferences(
				TapListenerService.SPREF, 0);
		return spref.getInt(TOTALCOUNT_PREF, 0);
	}

	private int getNumDaysFromSpref() {
		SharedPreferences spref = getSharedPreferences(
				TapListenerService.SPREF, 0);
		return spref.getInt(NUMDAYS_PREF, 0);
	}

	private int getCurrentWashCountFromSpref() {
		SharedPreferences spref = getSharedPreferences(
				TapListenerService.SPREF, 0);
		// String day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + "";
		// return spref.getInt(day, 0);
		Calendar c = Calendar.getInstance();
		int currDay = c.get(Calendar.DAY_OF_YEAR);
		int storedDay = spref.getInt(DAY_PREF, 0);
		if (currDay == storedDay) {
			return spref.getInt(DAYCOUNT_PREF, 0);
		}
		else return 0; //curr day is different, count = 0
		
	}

	public void updateSharedPref() {
		/*
		 * Shared Preferences <numdays,#> <currentday,#> <daycount, #>
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
		Type listTimestamps = new TypeToken<List<String>>() {
		}.getType();

		int storedDay = sprefs.getInt(DAY_PREF, 0);
		// new day of counting

		if (currDay > storedDay) {
			// reset the daily count to one
			e.putInt(DAYCOUNT_PREF, 1);
			int totalCnt = sprefs.getInt(TOTALCOUNT_PREF, 0);
			e.putInt(TOTALCOUNT_PREF, ++totalCnt);
			e.putInt(DAY_PREF, currDay);
			int numDays = sprefs.getInt(NUMDAYS_PREF, 0);
			e.putInt(NUMDAYS_PREF, ++numDays);
			//
			String timestamp_str = sprefs.getString(currDay_str, "");
			// Log.d(TAG, "NULL CHECK " + timestamp_str);
			List<String> timestampList = new ArrayList<String>() {
			};
			// Log.d(TAG, "NULL CHECK " + timestampList.toString());
			if (!timestamp_str.equals("")) {
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
			// Log.d(TAG, "NULL CHECK " + timestamp_str);
			List<String> timestampList = new ArrayList<String>() {
			};
			// Log.d(TAG, "NULL CHECK " + timestampList.toString());
			if (!timestamp_str.equals("")) {
				timestampList = g.fromJson(timestamp_str, listTimestamps);
			}
			timestampList.add(timestamp);
			timestamp_str = g.toJson(timestampList, listTimestamps);
			e.putString(currDay_str, timestamp_str);
		}

		e.commit();

	}

	public void exportData() {
		// TODO
		SharedPreferences sprefs = getSharedPreferences(settingsSharedPrefName,
				0);
		String previousSettings = sprefs.getString(settingsPrefDataKey, "");
		SoapSettingsHolder settings = new SoapSettingsHolder();
		if (previousSettings != null && previousSettings != "") {
			// previous settings exist, so load them and set them on view
			Gson gson = new Gson();
			settings = gson
					.fromJson(previousSettings, SoapSettingsHolder.class);
		}

		/*
		if (settings.defaultEmail.equals("")) {
			promptForEmailAddressAndSend();
		} else {
			sendEmail(settings.defaultEmail);
		}
		*/
		promptForEmailAddressAndSend(settings.defaultEmail);

	}

	private void promptForEmailAddressAndSend(String emailAddr) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Export Email");
		alert.setMessage("Please enter an email to send today's recorded data to: ");

		// Set an EditText view to get user input
		final EditText emailField = new EditText(this);
		alert.setView(emailField);
		
		emailField.setText(emailAddr);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				Gson gson = new Gson();
				String email_addr = emailField.getText().toString();

				if (email_addr.equals("") || email_addr.length() < 7) {
					Toast.makeText(getBaseContext(), "Invalid Email",
							Toast.LENGTH_SHORT).show();
				} else {
					//store email as default
					SharedPreferences sprefs = getSharedPreferences(settingsSharedPrefName,
							0);
					String previousSettings = sprefs.getString(settingsPrefDataKey, "");
					Editor e = sprefs.edit();
					SoapSettingsHolder settings = new SoapSettingsHolder();
					if (previousSettings != null && previousSettings != "") {
						// previous settings exist, so load them and set them on view
						settings = gson.fromJson(previousSettings, SoapSettingsHolder.class);
					}
					settings.defaultEmail = email_addr;
					String updatedSettings = gson.toJson(settings, SoapSettingsHolder.class);
					e.putString(settingsPrefDataKey, updatedSettings);
					e.commit();
					
					// send email
					sendEmail(email_addr);
				}

			}

		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	private String createCSVFile() {
		Calendar c = Calendar.getInstance();
		int currDay = c.get(Calendar.DAY_OF_YEAR);
		String currDay_str = currDay + "";
		Gson g = new Gson();
		Type listTimestamps = new TypeToken<List<String>>() {
		}.getType();
		String fileName = "";
		String fullPath = "";
		SharedPreferences sprefs = getSharedPreferences(SPREF, 0);

		String timestamp_str = sprefs.getString(currDay_str, "");
		// Log.d(TAG, "NULL CHECK " + timestamp_str);
		List<String> timestampList = new ArrayList<String>() {
		};
		// Log.d(TAG, "NULL CHECK " + timestampList.toString());
		if (!timestamp_str.equals("")) {
			timestampList = g.fromJson(timestamp_str, listTimestamps);
		}

		fileName = "SOAP_" + currDay + ".csv";
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root, fileName);
		fullPath = "" + file;
		if (file.exists()) {
			// file exists delete it then create new one;
			if (file.delete()) {
				CSVWriter writer = null;
				try {
					// writer = new CSVWriter(new FileWriter("/sdcard/myfile.csv"),
					// ',');
					writer = new CSVWriter(new FileWriter(fullPath), ',');
					if (timestampList.isEmpty()) {
						String[] entries = "today#no data".split("#");
						writer.writeNext(entries);
					} else {
						for (int i = 0; i < timestampList.size(); i++) {
							int count = i + 1;
							String entry = count + "#" + timestampList.get(i);
							String[] entries = entry.split("#");
							writer.writeNext(entries);
						}
					}

					writer.close();
				} catch (IOException e) { // error
					Log.d(TAG, "CSVWRITER ERROR: " + e.toString());
					Toast.makeText(this, "CSVWRITER ERROR: " + e.toString(), Toast.LENGTH_LONG).show();
				}
			}
			return fileName;
		} else {
			CSVWriter writer = null;
			try {
				// writer = new CSVWriter(new FileWriter("/sdcard/myfile.csv"),
				// ',');
				writer = new CSVWriter(new FileWriter(fullPath), ',');
				if (timestampList.isEmpty()) {
					String[] entries = "today#no data".split("#");
					writer.writeNext(entries);
				} else {
					for (int i = 0; i < timestampList.size(); i++) {
						int count = i + 1;
						String entry = count + "#" + timestampList.get(i);
						String[] entries = entry.split("#");
						writer.writeNext(entries);
					}
				}

				writer.close();
			} catch (IOException e) { // error
				Log.d(TAG, "CSVWRITER ERROR: " + e.toString());
				Toast.makeText(this, "CSVWRITER ERROR: " + e.toString(), Toast.LENGTH_LONG).show();
			}

			return fileName;
		}

	}

	/**
	 * Get service automatic start time from settings set to this, or if none
	 * set use default (9:00AM)
	 */
	private void setServiceAlarm() {
		Calendar c = Calendar.getInstance();
		SoapSettingsHolder settings;
		SharedPreferences spref = getSharedPreferences(
				SoapSettings.settingsSharedPrefName, 0);
		String json = spref.getString(SoapSettings.settingsPrefDataKey, "");
		if (json.equals("")) {
			settings = new SoapSettingsHolder();
		} else {
			Gson gson = new Gson();
			settings = gson.fromJson(json, SoapSettingsHolder.class);
		}
		int curTimeMinutes = c.get(Calendar.HOUR_OF_DAY) * 60
				+ c.get(Calendar.MINUTE);
		int targetMinutes = settings.startTimeHour * 60
				+ settings.startTimeMinute;
		int timeTillNextOccurance; // in minutes
		if (targetMinutes > curTimeMinutes) {
			timeTillNextOccurance = targetMinutes - curTimeMinutes;
		} else {
			// remainder of current day plus time to start it at next day
			timeTillNextOccurance = 24 * 60 - curTimeMinutes + targetMinutes;
		}
		long triggerServiceInMillis = c.getTimeInMillis()
				+ timeTillNextOccurance * 60 * 1000;

		Intent intent = new Intent(this, TapListenerService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		Log.d(TAG, "scheduling service to start in: " + timeTillNextOccurance
				/ 60 + " hours plus " + timeTillNextOccurance % 60 + " minutes");
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, triggerServiceInMillis,
				24 * 60 * 60 * 1000, pintent);
	}

	private void sendEmail(String addr) {
		// create attachment
		String fileName = createCSVFile();
		// send email
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_EMAIL, new String[]
		// {"hardy.ja@husky.neu.edu"});
		Calendar c = Calendar.getInstance();
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);;
        String phone_num = tm.getLine1Number();
		String emailaddress[] = { addr };
		intent.putExtra(Intent.EXTRA_EMAIL, emailaddress);
		intent.putExtra(Intent.EXTRA_SUBJECT, "SOAP_" + phone_num + " for " 
						+ c.getTime().toString());
		//intent.putExtra(Intent.EXTRA_TEXT, "body text");

		File root = Environment.getExternalStorageDirectory();
		File file = new File(root, fileName);
		//File file = getBaseContext().getFileStreamPath(fileName);
		if (!file.exists() || !file.canRead()) {
			Toast.makeText(this, "Attachment Error: No File Created", Toast.LENGTH_SHORT).show();
			// finish();
			// return;
		} else {
			Uri uri = Uri.parse("file://" + file);
			intent.putExtra(Intent.EXTRA_STREAM, uri);

			startActivity(Intent.createChooser(intent, "Send email..."));
		}

	}
}