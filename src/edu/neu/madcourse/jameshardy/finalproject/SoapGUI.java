package edu.neu.madcourse.jameshardy.finalproject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.jameshardy.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;
import edu.neu.madcourse.jameshardy.finalproject.TapListenerService;
import edu.neu.madcourse.jameshardy.finalproject.SoapSettings;
import edu.neu.madcourse.jameshardy.finalproject.SoapSettingsHolder;

public class SoapGUI extends Activity implements OnClickListener{

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
		
		Typeface helveticaLight = Typeface.createFromAsset(getAssets(), "helvetica_neue_light.ttf");
		TextView soapTitle = (TextView) findViewById(R.id.soap_title);
		soapTitle.setTypeface(helveticaLight);
		
		lastWashTime = (TextView) findViewById(R.id.soap_last_wash_time);
		lastWashTime.setTypeface(helveticaLight);
		lastWashTime.setText("Last wash 22 minutes ago");
		
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
			break;
		
		}
		
		
	}
	
	public void startService(){
		Intent startService = new Intent(this, edu.neu.madcourse.jameshardy.finalproject.TapListenerService.class);
		startService(startService); 
	}
	
	public void stopService(){
		Intent stopService = new Intent(this, edu.neu.madcourse.jameshardy.finalproject.TapListenerService.class);
		stopService(stopService); 
	}
	
	public void openSettingsActivity(){
		Intent settingsActivity = new Intent(this, edu.neu.madcourse.jameshardy.finalproject.SoapSettings.class);
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
				//updateTextField(count);
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
        int washCountToday = getCurrentWashCountFromSpref();
        
        setServiceAlarm();
        int totalWashCount = getTotalWashCountFromSpref();
        int numDaysRecording = getNumDaysFromSpref();
        double avg = 0;
        if (numDaysRecording != 0) {
        	avg = totalWashCount/numDaysRecording;
        }
        updateTextField(washCountToday, avg);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }
	
	private void updateTextField(int count, double avg) {
		//post handwash count
		if (count != 0 && avg != 0) {
			countToday.setText("" + count);
			averageCount.setText("" + avg);
		} else {
			Log.d(TAG, "count is null");
		}
	}
	
	private int getTotalWashCountFromSpref() {
		SharedPreferences spref = getSharedPreferences(TapListenerService.SPREF, 0);
		return spref.getInt(TOTALCOUNT_PREF, 0);
	}
	
	private int getNumDaysFromSpref() {
		SharedPreferences spref = getSharedPreferences(TapListenerService.SPREF, 0);
		return spref.getInt(NUMDAYS_PREF, 0);
	}
	
	private int getCurrentWashCountFromSpref(){
		SharedPreferences spref = getSharedPreferences(TapListenerService.SPREF, 0);
		//String day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + "";
		//return spref.getInt(day, 0);
		return spref.getInt(DAYCOUNT_PREF, 0);
	}
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
			//Log.d(TAG, "NULL CHECK " + timestamp_str);
			List<String> timestampList = new ArrayList<String>(){};
			//Log.d(TAG, "NULL CHECK " + timestampList.toString());
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
		//TODO
		SharedPreferences sprefs = getSharedPreferences(settingsSharedPrefName, 0);
		String previousSettings = sprefs.getString(settingsPrefDataKey, "");
		SoapSettingsHolder settings = new SoapSettingsHolder();
		if (previousSettings != null && previousSettings != ""){
			//previous settings exist, so load them and set them on view
			Gson gson = new Gson();
			settings = gson.fromJson(previousSettings, SoapSettingsHolder.class);
		}
		
		if (settings.defaultEmail.equals("")) {
			promptForEmailAddressAndSend();
		}
		else {
			sendEmail(settings.defaultEmail);
		}
		
	}
	
	private void promptForEmailAddressAndSend() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Export Email");
		alert.setMessage("Please enter an email to send data to: ");

		// Set an EditText view to get user input 
		final EditText emailField = new EditText(this);
		alert.setView(emailField);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {

		  // Do something with value!

			
			String email_addr = emailField.getText().toString();
		
			if (email_addr.equals("") || email_addr.length() < 7) {
				Toast.makeText(getBaseContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
			}
			else {
				//send email
				sendEmail(email_addr);	
			}
			
		  }
		  
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
		Type listTimestamps = new TypeToken<List<String>>(){}.getType();
		String fileName = "";
		String fullPath = "";
		SharedPreferences sprefs = getSharedPreferences(SPREF, 0);
		
		String timestamp_str = sprefs.getString(currDay_str, "");
		//Log.d(TAG, "NULL CHECK " + timestamp_str);
		List<String> timestampList = new ArrayList<String>(){};
		//Log.d(TAG, "NULL CHECK " + timestampList.toString());
		if (!timestamp_str.isEmpty()) {
			timestampList = g.fromJson(timestamp_str, listTimestamps);
		}
		
		fileName = "SOAP_" + currDay + ".csv";
		fullPath = "/sdcard/"+fileName;
		
		CSVWriter writer = null;
		try 
		{
		    //writer = new CSVWriter(new FileWriter("/sdcard/myfile.csv"), ',');
			writer = new CSVWriter(new FileWriter(fullPath), ',');
			if (timestampList.isEmpty()) {
				String[] entries = "today#no data".split("#");
				writer.writeNext(entries);
			}
			else {
				for (int i = 0; i < timestampList.size(); i++) {
					int count = i+1;
					String entry = count+"#"+timestampList.get(i);
					String[] entries = entry.split("#");
					writer.writeNext(entries);
				}
			}
			 
		    writer.close();
		} 
		catch (IOException e)
		{
		    //error
		}
		
		return fileName;
	}
	private void setServiceAlarm(){
		
	}
	
	private void sendEmail(String addr) {
		//create attachment
		String fileName = createCSVFile();
		//send email
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		//intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"hardy.ja@husky.neu.edu"});
		intent.putExtra(Intent.EXTRA_EMAIL, addr);
		intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
		intent.putExtra(Intent.EXTRA_TEXT, "body text");
		
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root, fileName);
		if (!file.exists() || !file.canRead()) {
		    Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
		    //finish();
		    //return;
		}
		Uri uri = Uri.parse("file://" + file);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		
		startActivity(Intent.createChooser(intent, "Send email..."));
	}
}