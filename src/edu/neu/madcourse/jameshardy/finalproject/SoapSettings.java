package edu.neu.madcourse.jameshardy.finalproject;

import edu.neu.madcourse.jameshardy.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class SoapSettings extends Activity implements OnClickListener, OnItemSelectedListener{

	private static final String TAG = "SOAP SETTINGS";
	private static final int TO_TIME_DIALOG_ID = 34;
	private static final int FROM_TIME_DIALOG_ID = 76;
	
	//auto monitor specific time settings
	Spinner fromDaySpinner;
	Spinner toDaySpinner;
	TextView toDayText;
	TextView fromTimeText;
	Button fromTimeButton;
	TextView toTimeText;
	Button toTimeButton;
	
	Typeface helveticaLight;
	String[] items;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soap_settings);
		
		View backButton = findViewById(R.id.soap_settings_back_button);
		backButton.setOnClickListener(this);
		
		View helpButton = findViewById(R.id.soap_settings_help_button);
		helpButton.setOnClickListener(this);
		
		helveticaLight = Typeface.createFromAsset(getAssets(), "helvetica_neue_light.ttf");
		TextView settingsTitle = (TextView) findViewById(R.id.soap_settings_title);
		settingsTitle.setTypeface(helveticaLight);
		
		TextView autoMonitorTopText = (TextView) findViewById(R.id.soap_auto_monitor_text_top);
		autoMonitorTopText.setTypeface(helveticaLight);
		
		TextView autoMonitorBottomText = (TextView) findViewById(R.id.soap_auto_monitor_text_bottom);
		autoMonitorBottomText.setTypeface(helveticaLight);
		
		toDayText = (TextView) findViewById(R.id.soap_to_day_text);
		toDayText.setTypeface(helveticaLight);
		
		fromTimeText = (TextView) findViewById(R.id.soap_from_time_text);
		fromTimeText.setTypeface(helveticaLight);
		
		toTimeText = (TextView) findViewById(R.id.soap_to_time_text);
		toTimeText.setTypeface(helveticaLight);
		
		CheckBox autoMonitor = (CheckBox) findViewById(R.id.soap_auto_monitor_checkbox);
		autoMonitor.setChecked(true);
		
		items = getResources().getStringArray(R.array.days_in_week);
		/*
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.days_in_week, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		
		*/
		ArrayAdapter<String> adapter = createCustomAdapater();
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				
		fromDaySpinner = (Spinner) findViewById(R.id.soap_from_day_spinner);
		fromDaySpinner.setOnItemSelectedListener(this);
		fromDaySpinner.setAdapter(adapter);
		
		toDaySpinner = (Spinner) findViewById(R.id.soap_to_day_spinner);
		toDaySpinner.setOnItemSelectedListener(this);
		toDaySpinner.setAdapter(adapter);
		toDaySpinner.setSelection(4); //Friday as default
		
		fromTimeButton = (Button) findViewById(R.id.soap_from_time_picker_button);
		fromTimeButton.setOnClickListener(this);
		fromTimeButton.setTypeface(helveticaLight);
		fromTimeButton.setText("9:00 AM");
		
		toTimeButton = (Button) findViewById(R.id.soap_to_time_picker_button);
		toTimeButton.setOnClickListener(this);
		toTimeButton.setTypeface(helveticaLight);
		toTimeButton.setText("5:00 PM");
		
		
	}
	
	@SuppressWarnings("deprecation")
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.soap_settings_back_button:
			finish();
			break;
		case R.id.soap_settings_help_button:
			//TODO
			break;
		case R.id.soap_from_time_picker_button:
			showDialog(FROM_TIME_DIALOG_ID);
			break;
		case R.id.soap_to_time_picker_button:
			showDialog(TO_TIME_DIALOG_ID);
			break;
		}
		
	}

	public void onItemSelected(AdapterView<?> parent, View v, int pos,
			long id) {
		String day = (String) parent.getItemAtPosition(pos);
		Log.d(TAG, "spinner id:" + id + " day ::" + day);
		
	}

	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
	public void onAutoMonitorChecked(View view){
		
		boolean checked = ((CheckBox) view).isChecked();
		    
	    if (checked){
	    	Log.d(TAG, "checkbox checked");
	    	toggleAutoMonitorSettingsVisiblity(View.VISIBLE);
	    }
	    else{
	    	Log.d(TAG, "checkbox unchecked");
	    	toggleAutoMonitorSettingsVisiblity(View.INVISIBLE);
	    }
	}
	
	/**
	 * Switch the Auto Monitor time settings to visible or invisible
	 * @param visibility should be either VIEW.INVISIBLE or VIEW.VISIBLE
	 */
	private void toggleAutoMonitorSettingsVisiblity(int visibility){
		fromDaySpinner.setVisibility(visibility);
		toDaySpinner.setVisibility(visibility);
		toDayText.setVisibility(visibility);
		fromTimeText.setVisibility(visibility);
		fromTimeButton.setVisibility(visibility);
		toTimeText.setVisibility(visibility);
		toTimeButton.setVisibility(visibility);
	}
	
	/**
	 * creates a custom adapter for the spinners, giving them special font
	 */
	private ArrayAdapter<String> createCustomAdapater(){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				R.layout.spinner_textiew, items) {

		     public View getView(int position, View convertView, ViewGroup parent) {
		             TextView v = (TextView) super.getView(position, convertView, parent);
		
		             v.setTypeface(helveticaLight);
		             v.setText(items[position]);
		             ((TextView) v).setTextColor(Color.BLACK);
		             return v;
		     }
		
		
		     public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
		             TextView v = (TextView) super.getDropDownView(position, convertView, parent);
		
		             v.setTypeface(helveticaLight);
		             v.setText(items[position]);
		
		             return v;
		     }
		};
		return adapter;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FROM_TIME_DIALOG_ID:
			return new TimePickerDialog(this, 
                                        fromTimePickerListener, 9, 00,false);
		case TO_TIME_DIALOG_ID:
			return new TimePickerDialog(this, 
                    toTimePickerListener, 17, 00,false);
		}
		return null;
	}
 
	private TimePickerDialog.OnTimeSetListener fromTimePickerListener = 
            new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int selectedHour,
				int selectedMinute) {
			setProperTimeInButton(selectedHour, selectedMinute, fromTimeButton);
		}
	};
	
	private TimePickerDialog.OnTimeSetListener toTimePickerListener = 
            new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int selectedHour,
				int selectedMinute) {
			setProperTimeInButton(selectedHour, selectedMinute, toTimeButton);
		}
	};
	
	private void setProperTimeInButton(int selectedHour, int selectedMinute, Button b){
		String amPM = "AM";
		if (selectedHour > 12){
			selectedHour = selectedHour - 12;
			amPM = "PM";
		}
		
		String min = "" + selectedMinute;
		if (selectedMinute < 10){
			min = "0" + selectedMinute;
		}
			
		b.setText(selectedHour + ":" + selectedMinute + " " + amPM);
	}
	
	
}