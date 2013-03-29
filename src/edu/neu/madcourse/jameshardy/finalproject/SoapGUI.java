package edu.neu.madcourse.jameshardy.finalproject;

import edu.neu.madcourse.jameshardy.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class SoapGUI extends Activity implements OnClickListener{
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soap_gui);
		
		View startServiceButton = findViewById(R.id.soap_gui_start_service);
		startServiceButton.setOnClickListener(this);
		
		View killServiceButton = findViewById(R.id.soap_gui_kill_service);
		killServiceButton.setOnClickListener(this);
	}

	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.soap_gui_start_service:
			//Log.d("ACTIVITY", "START CLICKED");
			Intent startService = new Intent(this, edu.neu.madcourse.jameshardy.finalproject.AccelerometerListenerService.class);
			startService(startService); 
			break;
		case R.id.soap_gui_kill_service:
			//Log.d("ACTIVITY", "STOP CLICKED");
			Intent stopService = new Intent(this, edu.neu.madcourse.jameshardy.finalproject.AccelerometerListenerService.class);
			stopService(stopService); 
			break;
		}
		
	}
	
	
}