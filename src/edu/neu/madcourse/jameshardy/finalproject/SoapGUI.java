package edu.neu.madcourse.jameshardy.finalproject;

import java.io.File;
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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.jameshardy.finalproject.TapListenerService;
import edu.neu.madcourse.jameshardy.finalproject.SoapSettings;

public class SoapGUI extends Activity implements OnClickListener{

	public static final String BROADCAST_ACTION = "edu.neu.madcourse.jameshardy.finalproject.send_count";
	public static final String HANDWASH_COUNT = "edu.neu.madcourse.jameshardy.finalproject.wash_count";
	
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
        updateTextField(washCountToday);
        
        setServiceAlarm();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }
	
	private void updateTextField(int count) {
		//post handwash count
		if (count != 0) {
			countToday.setText("" + count);
		} else {
			Log.d(TAG, "count is null");
		}
	}
	
	private int getCurrentWashCountFromSpref(){
		SharedPreferences spref = getSharedPreferences(TapListenerService.SPREF, 0);
		String day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + "";
		return spref.getInt(day, 0);
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
			/*
			String timestamp_str = sprefs.getString(currDay_str, "");
			List<String> timestampList = new ArrayList<String>();
			timestampList = g.fromJson(timestamp_str, listTimestamps);
			timestampList.add(timestamp);
			timestamp_str = g.toJson(timestampList, listTimestamps);
			e.putString(currDay_str, timestamp_str);
			*/
		}
		
		// same day
		else {
			int dailyCnt = sprefs.getInt(DAYCOUNT_PREF, 0);
			e.putInt(DAYCOUNT_PREF, ++dailyCnt);
			int totalCnt = sprefs.getInt(TOTALCOUNT_PREF, 0);
			e.putInt(TOTALCOUNT_PREF, ++totalCnt);
			e.putInt(DAY_PREF, currDay);
			//
			/*
			String timestamp_str = sprefs.getString(currDay_str, "");
			List<String> timestampList = new ArrayList<String>();
			timestampList = g.fromJson(timestamp_str, listTimestamps);
			timestampList.add(timestamp);
			timestamp_str = g.toJson(timestampList, listTimestamps);
			e.putString(currDay_str, timestamp_str);
			*/
		}
		
		e.commit();
		
		
	}
	
	public void exportData() {
		//TODO
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
		
			if (email_addr == "" || email_addr.length() < 7) {
				Toast.makeText(getBaseContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
			}
			else {
				//create attachment
				createCSVFile();
				//send email
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
				intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
				intent.putExtra(Intent.EXTRA_TEXT, "body text");
				/*
				File root = Environment.getExternalStorageDirectory();
				File file = new File(root, xmlFilename);
				if (!file.exists() || !file.canRead()) {
				    Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
				    finish();
				    return;
				}
				Uri uri = Uri.parse("file://" + file);
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent, "Send email..."));
				*/
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
	private void createCSVFile() {
		//TODO
	}
	
	private void setServiceAlarm(){
		
	}
	
}