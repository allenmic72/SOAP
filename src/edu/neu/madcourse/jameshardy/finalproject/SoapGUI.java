package edu.neu.madcourse.jameshardy.finalproject;

import java.util.Calendar;

import edu.neu.madcourse.jameshardy.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import edu.neu.madcourse.jameshardy.finalproject.TapListenerService;

public class SoapGUI extends Activity implements OnClickListener{

	public static final String BROADCAST_ACTION = "edu.neu.madcourse.jameshardy.finalproject.send_count";
	public static final String HANDWASH_COUNT = "edu.neu.madcourse.jameshardy.finalproject.wash_count";
	
	private static final String TAG = "SOAP GUI";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soap_gui);
		
		Typeface helveticaLight = Typeface.createFromAsset(getAssets(), "helvetica_neue_light.ttf");
		TextView soapTitle = (TextView) findViewById(R.id.soap_title);
		soapTitle.setTypeface(helveticaLight);
		
		View backButton = findViewById(R.id.soap_back_button);
		backButton.setOnClickListener(this);
		
		View helpButton = findViewById(R.id.soap_help_button);
		helpButton.setOnClickListener(this);
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
			TextView tv = (TextView) findViewById(R.id.soap_washcount_text);
			CharSequence cs = "Washed Hands " + count + " times";
			tv.setText(cs);
		} else {
			Log.d(TAG, "count is null");
		}
	}
	
	private int getCurrentWashCountFromSpref(){
		SharedPreferences spref = getSharedPreferences(TapListenerService.SPREF, 0);
		String day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + "";
		return spref.getInt(day, 0);
	}
}