package edu.neu.madcourse.jameshardy.Boggle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.R.raw;
import edu.neu.madcourse.jameshardy.R.string;
import edu.neu.madcourse.jameshardy.Boggle.DatabaseTable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BoggleGame extends Activity {
	private static final String TAG = "Boggle";

	public static final String GRID_SIZE = "edu.neu.madcourse.jameshardy.boggle.grid_size";
	private static final String PREFS_GAME_DATA = "BogglePrefs";
	private static final String PREFS_TIMER = "timerMilisecs";
	private static final String PREF_BOARD = "board";
	public static final int GRID_FOUR = 4;
	public static final int GRID_FIVE = 5;
	public static final int GRID_SIX = 6;
	protected static final int GRID_CONTINUE = -1;

	// TODO MAKE PUZZLE ARRAY RANDOM CHARS INTO STRING GRIDSIZE X GRIDSIZE LONG

	private List<String[]> letterDice;
	private String[] board;
	private List<String> word;
	public String lastWord = "";
	public String scoreStr = "0";
	private int score;
	public String lastWordValid = "NO";
	private List<String> acceptedWords;
	private int grid_dim;
	public String mTimerString;
	public CountDownTimer timer;
	private long millisecLeft;

	// DatabaseTable dbDictionary;

	private BoardView boardView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		int size = getIntent().getIntExtra(GRID_SIZE, GRID_FOUR);
		// size comes through as 0,1,2
		size += 4;
		grid_dim = size;
		
		millisecLeft = 0;

		//init score to 0
		score = 0; 
		// TODO add case based on size of board chance init
		initDice(grid_dim);

		board = getBoard(size);

		// Log.d(TAG, "GRID SIZE " + diff);
		// puzzle = getPuzzle(diff);
		// calculateUsedTiles();

		boardView = new BoardView(this);
		setContentView(boardView);
		boardView.requestFocus();

		// load dictionary
		// dbDictionary = new DatabaseTable(this);
		// dbDictionary.open();

		mTimerString = "";
		timer = new CountDownTimer(120000, 1000) {
			public void onTick(long millisUntilFinished) {
				mTimerString = "seconds remaining: "
						+ (millisUntilFinished / 1000);
				boardView.invalidate();
			}

			public void onFinish() {
				mTimerString = "Time's Up!";
				boardView.invalidate();

				popUpFinishAlert();
			}
		}.start();

		// TODO
		// helper = new WordHelper(this);
		// dataset_cursor = helper.getAll();

		Arrays.fill(usedLetters, false);
		word = new ArrayList();
		acceptedWords = new ArrayList();

		// If the activity is restarted, do a continue next time
		getIntent().putExtra(GRID_SIZE, GRID_CONTINUE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Music.play(this, R.raw.sudoku_game);

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		// Music.stop(this);
		// Save the current puzzle
		/*
		 * getPreferences(MODE_PRIVATE).edit().putString(PREF_PUZZLE,
		 * toPuzzleString(puzzle)).commit();
		 */
		
	}

	@Override
	protected void onDestroy() {
		// close database
		// dbDictionary.closeDB();
		super.onDestroy();
	}

	// TODO
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

	/** Return a string for the tile at the given coordinates */
	protected String getTileString(int x, int y) {
		String s = getTile(x, y);
		if (s == null)
			return "";
		else
			return s;
	}

	/** Return the tile at the given coordinates */
	private String getTile(int x, int y) {
		return board[y * grid_dim + x];
	}

	// allow space up to 6 for max grid size
	private final boolean[] usedLetters = new boolean[36];

	/** Return cached used tiles visible from the given coords */
	protected boolean[] getUsedLetters() {
		return usedLetters;
	}

	/** Change the tile only if it's a valid move */
	protected boolean setTileIfValid(int x, int y) {
		int square = y * grid_dim + x;
		Log.d(TAG, "ontouch: square " + square);
		if (usedLetters[square] == true) {
			// letter already used, return false
			return false;
		} else {
			setTile(x, y);
			return true;
		}
	}

	/** Mark square as used */
	private void setTile(int x, int y) {
		usedLetters[y * grid_dim + x] = true;
		// ADD TO WORD
		String s = getTileString(x, y);
		word.add(s);
		Log.d(TAG, "ontouch: success adding to word");

		// HIGHLIGHT TILE
		// ADD TO WORD
		// MARK AS USED
	}

	/** Empty used letters for next word */
	protected void emptyUsedLetters() {
		Arrays.fill(usedLetters, false);
	}

	public void addLetterToWord(int x, int y) {
		String letter = getTileString(x, y);
		word.add(letter);
	}

	public void emptyWord() {
		// save last word
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < word.size(); i++) {
			b.append(word.get(i));
		}
		lastWord = b.toString();

		word.clear();
	}

	public boolean isValidWord() {
		// String[] s = {"word"};
		// Cursor c = dbDictionary.getWordMatches(word.toString(), null);
		/*
		 * Valid words are only 3 letters or larger
		 */
		if (word.size() >= 3) {
			char fTLS[] = new char[3];
			StringBuilder fullWord = new StringBuilder();
			for (int i = 0; i < word.size(); i++) {
				fullWord.append(word.get(i));
			}

			StringBuilder fTLsb = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				fTLsb.append(word.get(i));
			}

			//build filename from first three letters
			fTLsb.getChars(0, 3, fTLS, 0);
			
			String fileName = new String(fTLS);
			fileName = fileName + ".txt";
			fileName = fileName.toLowerCase();
			
			
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.getAssets().open(fileName)));
				
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.equals(fullWord.toString().toLowerCase()))
						{
							if (score()) //calc and print new score
								return true; 
							else  //word was already used
								return false;
						}
					}
				} finally {
					reader.close();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d(TAG, "Dictionary FILE did not exist");
				return false;
			}
			
			
			return false;
		} else
			return false;
	}
	
	protected boolean score() 
	{
		/*
		 * Scoring Scheme:
		 * 3 letters = 1
		 * 4 letters = 2
		 * 5 letters = 3
		 * 6 letters = 4
		 * etc...
		 */
		StringBuilder fullWord = new StringBuilder();
		for (int i = 0; i < word.size(); i++) {
			fullWord.append(word.get(i));
		}
		String currWord = new String(fullWord.toString().toLowerCase());
		if (acceptedWords.contains(currWord))
		{
			//don't score because word's already been used.
			return false;
		}
		else {
			acceptedWords.add(currWord);
			score += (word.size() - 2); 
			scoreStr = Integer.toString(score);
		}
		return true;
	}

	/*
	 * TODO put this into a new activity. Start new activity with simple screen 
	 * showing resume or quit buttons. This puts game activity on pause where can
	 * save bundle with letters, time, words, and score such that when resume, 
	 * resume with saved data.
	 */
	/*
	public void popUpPauseAlert() {
		new AlertDialog.Builder(this).setTitle(R.string.boggle_pause)
				.setItems(R.array.pause, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						switch (i) {
						case 0: //RESUME
							// timer.notify();
							break;
						case 1: //QUIT
							finish();
							break;
						default:
							break;
						}
					}
				}).show();
	}
	*/
	public void popUpPauseAlert() {
		//timer.onTick(millisecLeft);
		//timer.cancel();
		
		Intent i = new Intent(this, BogglePaused.class);
		startActivity(i);
	}

	public void popUpFinishAlert() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.boggle_time_up)
				.setItems(R.array.time_up,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								switch (i) {
								case 0:
								case 1:
									finish();
									break;
								}
							}
						}).show();
	}
}
