package edu.neu.madcourse.jameshardy.MultiplayerBoggle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.neu.mobileclass.apis.KeyValueAPI;
import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.R.array;
import edu.neu.madcourse.jameshardy.R.id;
import edu.neu.madcourse.jameshardy.R.layout;
import edu.neu.madcourse.jameshardy.R.menu;
import edu.neu.madcourse.jameshardy.R.raw;
import edu.neu.madcourse.jameshardy.R.string;
import edu.neu.madcourse.jameshardy.MultiplayerBoggle.MP_BoggleUser;
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
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;


public class MultiplayerBoggle extends Activity implements OnClickListener {
	private static final String TAG = "MP Boggle";
	
	public static final String USER_NAME = "edu.neu.madcourse.jameshardy.multiplayerboggle.user_name";
	public static final String PHONE_NUM = "edu.neu.madcourse.jameshardy.multiplayerboggle.phone_num";

	public static final int GRID_FOUR = 4;
	public static final int GRID_FIVE = 5;
	public static final int GRID_SIX = 6;
	protected static final int GRID_CONTINUE = -1;
	
	private int grid_size;
	private List<String[]> letterDice;
	private String[] boardStr;
	
	public static String userName = "";
	public static String phoneNum = "";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mp_boggle_main);
		
		Bundle b = getIntent().getExtras();
		userName = b.getString(USER_NAME);
		phoneNum = b.getString(PHONE_NUM);
		
		Gson g = new Gson();
		Type listOfUsers = new TypeToken<List<MP_BoggleUser>>(){}.getType();
		String users = "";
		List<MP_BoggleUser> mp_users = new ArrayList<MP_BoggleUser>();
		
		//TODO REMOVE. JUST FOR TESTING
		//KeyValueAPI.clear("hardyja", "hardyja");
		//TODO
		
		boolean gettingUser = false;
		while (!gettingUser) {
			if (KeyValueAPI.isServerAvailable())
			{
				Log.d(TAG, "adding user");
				users = KeyValueAPI.get("hardyja", "hardyja", "users");
				if (users.length() > 0)
				{
					mp_users = g.fromJson(users, listOfUsers); 
				}
				gettingUser = true;
			}
		}
		
		boolean addedUser = false;
		//checks if user already exists
		for (int i = 0; i<mp_users.size(); i++) {
			MP_BoggleUser user = mp_users.get(i);
			if (user.name.equals(userName) && user.number.equals(phoneNum))
				addedUser = true;
		}
		while (!addedUser) {
			if (KeyValueAPI.isServerAvailable())
			{
				Log.d(TAG, "adding user");
				//add new user with score init to 0
				MP_BoggleUser bu = new MP_BoggleUser(userName, phoneNum, 0);
				mp_users.add(bu);
				String users_str = g.toJson(mp_users, listOfUsers);
				KeyValueAPI.put("hardyja", "hardyja", "users", users_str);
				addedUser = true;
			}
		}
	


		//set default to 4x4 and changed via settings
		grid_size = 4;
		initDice(grid_size);
		boardStr = getBoard(grid_size);
		
		View newGameButton = findViewById(R.id.boggle_new_game_button);
		newGameButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.boggle_exit_button);
		exitButton.setOnClickListener(this);
		View ackButton = findViewById(R.id.boggle_acknowledgements_button);
		ackButton.setOnClickListener(this);
		View rulesButton = findViewById(R.id.mp_boggle_rules_button);
		rulesButton.setOnClickListener(this);
		View highscoresButton = findViewById(R.id.high_score_button);
		highscoresButton.setOnClickListener(this);
		View settingsButton = findViewById(R.id.boggle_settings_button);
		settingsButton.setOnClickListener(this);
		View challengeButton = findViewById(R.id.mp_boggle_challenge_button);
		challengeButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		Intent i;
		switch (v.getId()) {
		case R.id.boggle_new_game_button:
			//openNewGameDialog();
			startGame(grid_size, boardStr, userName, phoneNum);
			break;
		case R.id.boggle_acknowledgements_button:
			i = new Intent(this, BoggleAcknowledgements.class);
			startActivity(i);
			break;
		case R.id.mp_boggle_rules_button:
			i = new Intent(this, MP_BoggleRules.class);
			startActivity(i);
			break;
		case R.id.high_score_button:
			i = new Intent(this, MP_BoggleHighScores.class);
			startActivity(i);
			break;
		case R.id.boggle_settings_button:
			openSettingsDialog();
			break;
		case R.id.mp_boggle_challenge_button:
			i = new Intent(this, MP_BoggleChallengeUser.class);
			Bundle b = new Bundle();
			b.putString(USER_NAME, userName);
			b.putString(PHONE_NUM, phoneNum);
			i.putExtras(b);
			startActivity(i);
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
	
	private void openSettingsDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.boggle_settings_title)
				.setItems(R.array.grid_size,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								//startGame(i);
								grid_size = i + 4;
								initDice(grid_size);
								boardStr = getBoard(grid_size);
							}
						}).show();
	}
	
	/** Start a new game with the given grid size */
	private void startGame(int gridSize, String[] board, String user, String phone) {
		// Log.d(TAG, "clicked on " + i);
		Intent intent = new Intent(this, BoggleGame.class);
		Bundle b = new Bundle();
		b.putInt(BoggleGame.GRID_SIZE, gridSize);
		b.putStringArray(BoggleGame.BOARD_STR, board);
		b.putString(BoggleGame.USERNAME_STR, user);
		b.putString(BoggleGame.PHONE_STR, phone);
		intent.putExtras(b);
		startActivity(intent);
	}
	// init dice for 4x4, 5x5, 6x6 games
		private void initDice(int dim) {
			letterDice = new ArrayList();
			switch (dim) {
			case GRID_CONTINUE:
				break;
			case GRID_SIX:
				letterDice.add(new String[] { "A", "O", "B", "B", "O", "J" });
				letterDice.add(new String[] { "W", "H", "G", "E", "E", "N" });
				letterDice.add(new String[] { "N", "R", "N", "Z", "H", "L" });
				letterDice.add(new String[] { "N", "A", "E", "A", "G", "E" });
				letterDice.add(new String[] { "D", "I", "Y", "S", "T", "T" });
				letterDice.add(new String[] { "I", "E", "S", "T", "S", "O" });
				letterDice.add(new String[] { "A", "O", "T", "T", "W", "O" });
				letterDice.add(new String[] { "H", "Qu", "U", "M", "N", "I" });
				letterDice.add(new String[] { "R", "Y", "T", "L", "T", "E" });
				letterDice.add(new String[] { "P", "O", "H", "C", "S", "A" });
				letterDice.add(new String[] { "L", "R", "E", "V", "Y", "D" });
				letterDice.add(new String[] { "E", "X", "L", "D", "I", "R" });
				letterDice.add(new String[] { "I", "E", "N", "S", "U", "E" });
				letterDice.add(new String[] { "S", "F", "F", "K", "A", "P" });
				letterDice.add(new String[] { "I", "O", "T", "M", "U", "C" });
				letterDice.add(new String[] { "E", "H", "W", "V", "T", "R" });
				letterDice.add(new String[] { "A", "O", "B", "B", "O", "J" });
				letterDice.add(new String[] { "W", "H", "G", "E", "E", "N" });
				letterDice.add(new String[] { "N", "R", "N", "Z", "H", "L" });
				letterDice.add(new String[] { "N", "A", "E", "A", "G", "E" });
				letterDice.add(new String[] { "D", "I", "Y", "S", "T", "T" });
				letterDice.add(new String[] { "I", "E", "S", "T", "S", "O" });
				letterDice.add(new String[] { "A", "O", "T", "T", "W", "O" });
				letterDice.add(new String[] { "H", "Qu", "U", "M", "N", "I" });
				letterDice.add(new String[] { "R", "Y", "T", "L", "T", "E" });
				letterDice.add(new String[] { "P", "O", "H", "C", "S", "A" });
				letterDice.add(new String[] { "L", "R", "E", "V", "Y", "D" });
				letterDice.add(new String[] { "E", "X", "L", "D", "I", "R" });
				letterDice.add(new String[] { "I", "E", "N", "S", "U", "E" });
				letterDice.add(new String[] { "S", "F", "F", "K", "A", "P" });
				letterDice.add(new String[] { "I", "O", "T", "M", "U", "C" });
				letterDice.add(new String[] { "E", "H", "W", "V", "T", "R" });
				letterDice.add(new String[] { "A", "O", "B", "B", "O", "J" });
				letterDice.add(new String[] { "W", "H", "G", "E", "E", "N" });
				letterDice.add(new String[] { "I", "O", "T", "M", "U", "C" });
				letterDice.add(new String[] { "E", "H", "W", "V", "T", "R" });
				break;
			case GRID_FIVE:
				letterDice.add(new String[] { "A", "O", "B", "B", "O", "J" });
				letterDice.add(new String[] { "W", "H", "G", "E", "E", "N" });
				letterDice.add(new String[] { "N", "R", "N", "Z", "H", "L" });
				letterDice.add(new String[] { "N", "A", "E", "A", "G", "E" });
				letterDice.add(new String[] { "D", "I", "Y", "S", "T", "T" });
				letterDice.add(new String[] { "I", "E", "S", "T", "S", "O" });
				letterDice.add(new String[] { "A", "O", "T", "T", "W", "O" });
				letterDice.add(new String[] { "H", "Qu", "U", "M", "N", "I" });
				letterDice.add(new String[] { "R", "Y", "T", "L", "T", "E" });
				letterDice.add(new String[] { "A", "O", "B", "B", "O", "J" });
				letterDice.add(new String[] { "W", "H", "G", "E", "E", "N" });
				letterDice.add(new String[] { "N", "R", "N", "Z", "H", "L" });
				letterDice.add(new String[] { "N", "A", "E", "A", "G", "E" });
				letterDice.add(new String[] { "D", "I", "Y", "S", "T", "T" });
				letterDice.add(new String[] { "I", "E", "S", "T", "S", "O" });
				letterDice.add(new String[] { "A", "O", "T", "T", "W", "O" });
				letterDice.add(new String[] { "H", "Qu", "U", "M", "N", "I" });
				letterDice.add(new String[] { "R", "Y", "T", "L", "T", "E" });
				letterDice.add(new String[] { "P", "O", "H", "C", "S", "A" });
				letterDice.add(new String[] { "L", "R", "E", "V", "Y", "D" });
				letterDice.add(new String[] { "E", "X", "L", "D", "I", "R" });
				letterDice.add(new String[] { "I", "E", "N", "S", "U", "E" });
				letterDice.add(new String[] { "S", "F", "F", "K", "A", "P" });
				letterDice.add(new String[] { "I", "O", "T", "M", "U", "C" });
				letterDice.add(new String[] { "E", "H", "W", "V", "T", "R" });
				break;
			case GRID_FOUR:
			default:
				letterDice.add(new String[] { "A", "O", "B", "B", "O", "J" });
				letterDice.add(new String[] { "W", "H", "G", "E", "E", "N" });
				letterDice.add(new String[] { "N", "R", "N", "Z", "H", "L" });
				letterDice.add(new String[] { "N", "A", "E", "A", "G", "E" });
				letterDice.add(new String[] { "D", "I", "Y", "S", "T", "T" });
				letterDice.add(new String[] { "I", "E", "S", "T", "S", "O" });
				letterDice.add(new String[] { "A", "O", "T", "T", "W", "O" });
				letterDice.add(new String[] { "H", "Qu", "U", "M", "N", "I" });
				letterDice.add(new String[] { "R", "Y", "T", "L", "T", "E" });
				letterDice.add(new String[] { "P", "O", "H", "C", "S", "A" });
				letterDice.add(new String[] { "L", "R", "E", "V", "Y", "D" });
				letterDice.add(new String[] { "E", "X", "L", "D", "I", "R" });
				letterDice.add(new String[] { "I", "E", "N", "S", "U", "E" });
				letterDice.add(new String[] { "S", "F", "F", "K", "A", "P" });
				letterDice.add(new String[] { "I", "O", "T", "M", "U", "C" });
				letterDice.add(new String[] { "E", "H", "W", "V", "T", "R" });
				break;
			}

			/*
			 * TEST STRING ARRAYS String[] temp = letterDice.get(0); Log.d(TAG,
			 * "TEST DICE 1" + temp[2]);
			 */
		}
		
		private String[] getBoard(int size) {
			List<String> letter_list = new ArrayList();
			String[] bd = {};
			Random generator = new Random();

			for (int i = 0; i < (size * size); i++) {
				String[] temp = letterDice.get(i);
				// random 0-5, 6 sides of dice
				int rand = generator.nextInt(6);
				letter_list.add(temp[rand]);
			}

			if (letter_list.size() > 0)
				// letter_list.toArray(bd);
				bd = letter_list.toArray(new String[0]);
			// Log.d(TAG, "LETTER LIST " + bd[0]);
			return bd;
		}

}
