package edu.neu.madcourse.jameshardy.finalproject;

import edu.neu.madcourse.jameshardy.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class SoapGUI extends Activity implements OnClickListener{

	public static final String BROADCAST_ACTION = "edu.neu.madcourse.jameshardy.finalproject.send_count";
	public static final String HANDWASH_COUNT = "edu.neu.madcourse.jameshardy.finalproject.wash_count";
	
	private static final String TAG = "SOAP GUI";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soap_gui);
		
		View startServiceButton = findViewById(R.id.soap_gui_start_service);
		startServiceButton.setOnClickListener(this);
		
		View killServiceButton = findViewById(R.id.soap_gui_kill_service);
		killServiceButton.setOnClickListener(this);
		
		View ackButton = findViewById(R.id.soap_acknowledgements_button);
		ackButton.setOnClickListener(this);
		
	}

	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.soap_gui_start_service:
			//Log.d("ACTIVITY", "START CLICKED");
			Intent startService = new Intent(this, edu.neu.madcourse.jameshardy.finalproject.TapListenerService.class);
			startService(startService); 
			break;
		case R.id.soap_gui_kill_service:
			//Log.d("ACTIVITY", "STOP CLICKED");
			Intent stopService = new Intent(this, edu.neu.madcourse.jameshardy.finalproject.TapListenerService.class);
			stopService(stopService); 
			break;
		case R.id.soap_acknowledgements_button:
			Intent ackActivity = new Intent(this, SoapAcknowledgements.class);
			startActivity(ackActivity);
			break;
		}
		
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
				updateTextField(count);
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
}