package edu.neu.madcourse.jameshardy.Boggle;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.R.array;
import edu.neu.madcourse.jameshardy.R.id;
import edu.neu.madcourse.jameshardy.R.layout;
import edu.neu.madcourse.jameshardy.R.menu;
import edu.neu.madcourse.jameshardy.R.raw;
import edu.neu.madcourse.jameshardy.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class Boggle extends Activity implements OnClickListener {
	private static final String TAG = "Boggle";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.boggle_main);

		View newGameButton = findViewById(R.id.boggle_new_game_button);
		newGameButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.boggle_exit_button);
		exitButton.setOnClickListener(this);
		View ackButton = findViewById(R.id.boggle_acknowledgements_button);
		ackButton.setOnClickListener(this);
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.boggle_new_game_button:
			openNewGameDialog();
			break;
		case R.id.boggle_acknowledgements_button:
			Intent i = new Intent(this, BoggleAcknowledgements.class);
			startActivity(i);
			break;
		case R.id.boggle_exit_button:
			finish();
			break;

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/** Ask the user what grid size they want */
	private void openNewGameDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.boggle_new_game_title)
				.setItems(R.array.grid_size,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								startGame(i);
							}
						}).show();
	}

	/** Start a new game with the given grid size */
	private void startGame(int i) {
		// Log.d(TAG, "clicked on " + i);
		Intent intent = new Intent(this, BoggleGame.class);
		intent.putExtra(BoggleGame.GRID_SIZE, i);
		startActivity(intent);
	}
}
