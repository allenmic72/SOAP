package edu.neu.madcourse.jameshardy.MultiplayerBoggle;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.Boggle.BoggleGame;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;


public class MP_BoggleCreateUser extends Activity implements OnClickListener {
	
	private static final String TAG = "MP Boggle";
	
	private String userName = "";
	private String phoneNum = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiplayer_boggle_create_user);

		View enterButton = findViewById(R.id.mp_boggle_create_user_button);
		enterButton.setOnClickListener(this);
		
		TelephonyManager tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		  phoneNum = tMgr.getLine1Number();
		
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.mp_boggle_create_user_button:
			//TODO
			EditText et = (EditText)findViewById(R.id.mp_boggle_create_user_field);
			userName = et.getText().toString();
			
			if (userName.length() > 0) {
				Intent i = new Intent(this, MultiplayerBoggle.class);
				Bundle b = new Bundle();
				b.putString(MultiplayerBoggle.USER_NAME, userName);
				b.putString(MultiplayerBoggle.PHONE_NUM, phoneNum);
				i.putExtras(b);
				startActivity(i);
			}
				
			break;

		}
	}

	@Override
	protected void onDestroy() {
		// close database
		// dbDictionary.closeDB();
		super.onDestroy();
	}

	
}
