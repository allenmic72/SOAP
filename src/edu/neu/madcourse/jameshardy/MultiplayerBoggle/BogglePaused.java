package edu.neu.madcourse.jameshardy.MultiplayerBoggle;

import edu.neu.madcourse.jameshardy.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class BogglePaused extends Activity implements OnClickListener{
	
	private static final String TAG = "BogglePause";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.boggle_pause);

		View resumeGameButton = findViewById(R.id.boggle_resume_button);
		resumeGameButton.setOnClickListener(this);
		
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.boggle_resume_button:
			this.finish();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		// close database
		// dbDictionary.closeDB();
		Log.d(TAG, " hitting DESTROY");
		super.onDestroy();
	}
}
