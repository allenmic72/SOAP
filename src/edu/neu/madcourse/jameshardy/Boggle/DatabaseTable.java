package edu.neu.madcourse.jameshardy.Boggle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.R.raw;
import edu.neu.madcourse.jameshardy.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseTable {

	private static final String TAG = "DictionaryDatabase";

	// The columns we'll include in the dictionary table
	public static final String COLUMN_WORD = "word";
	// public static final String COL_DEFINITION = "DEFINITION";

	private static final String DATABASE_NAME = "dictionary.db";
	private static final String TABLE_WORDS = "words";
	private static final String FTS_VIRTUAL_TABLE = "FTS";
	private static final int DATABASE_VERSION = 1;

	private DatabaseOpenHelper mDatabaseOpenHelper;
	private SQLiteDatabase mDatabase;

	public DatabaseTable(Context context) {
		mDatabaseOpenHelper = new DatabaseOpenHelper(context);
	}

	private static class DatabaseOpenHelper extends SQLiteOpenHelper {

		private final Context mHelperContext;
		private SQLiteDatabase mDB;

		//private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
			//	+ FTS_VIRTUAL_TABLE + " USING fts3 (" + COL_WORD + ")";
		private static final String DATABASE_CREATE = "create table "
			      + TABLE_WORDS + "(" + COLUMN_WORD
			      + " text not null);";

		// COL_WORD + ", " +
		// COL_DEFINITION + ")";

		DatabaseOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mHelperContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			mDatabase = db;
			mDatabase.execSQL(DATABASE_CREATE);
			loadDictionary();
			*/
			db.execSQL(DATABASE_CREATE);
			mDB = db;
			loadDictionary();
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
			onCreate(db);
		}

		private void loadDictionary() {
			new Thread(new Runnable() {
				public void run() {
					try {
						loadWords();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}).start();
		}

		private void loadWords() throws IOException {
			final Resources resources = mHelperContext.getResources();
			/*
			InputStream inputStream = resources.openRawResource(R.raw.wordlist);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			try {
				String line;
				while ((line = reader.readLine()) != null) {
					//String[] strings = TextUtils.split(line, "-");
					//if (strings.length < 2)
						//continue;
					//long id = addWord(strings[0].trim(), strings[1].trim());
					long id = addWord(line);
					int i = 0;
					if (id < 0) {
						Log.e(TAG, "unable to add word: " + line);
					}
				}
			} finally {
				reader.close();
			}
			*/
		}

		public long addWord(String word) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(COLUMN_WORD, word);
			//initialValues.put(COL_DEFINITION, definition);

			return mDB.insert(TABLE_WORDS, null, initialValues);
		}
	}
	
	public void open() throws SQLException {
		mDatabase = mDatabaseOpenHelper.getWritableDatabase();
	}
	
	
	
	public Cursor getWordMatches(String query, String[] columns) {
	    //String selection = COL_WORD + " MATCH ?";
	    String selection = COLUMN_WORD + " = ?";
		String[] selectionArgs = new String[] {query};
		//
		//Log.d(TAG, selection + query);
		//
	    return query(selection, selectionArgs, columns);
	}

	private Cursor query(String selection, String[] selectionArgs, String[] columns) {
	    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	    builder.setTables(TABLE_WORDS);

	    Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
	            columns, selection, selectionArgs, null, null, null);
	    //
	    
	    if (cursor == null) {
	        return null;
	    } else if (!cursor.moveToFirst()) {
	        cursor.close();
	        return null;
	    }
	    return cursor;
	}
	
	public void closeDB() {
		mDatabaseOpenHelper.close();
	}
	/*
	public boolean wordQuery(String word) {
		Cursor cursor = mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
	}
	*/
	public Cursor wordQuery(String word) {
		//return mDatabase.query(TABLE_WORDS, new String[] {COLUMN_WORD}, COLUMN_WORD + "= ?", new String[] {word}, null, null, null);
		Cursor cursor = null;
		if (mDatabase.isOpen()) {
			cursor = mDatabase.query(TABLE_WORDS, null, COLUMN_WORD + "='"+word+"'", null, null, null, null);
			//cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_WORDS + " WHERE " + COLUMN_WORD + "='" + word + "';", null);
		}
		return cursor; 
	}
	/*
	public String wordQuery(String word) {
		SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_WORDS, new String[] {COLUMN_WORD}, COLUMN_WORD + "= ?", new String[] {word}, null, null, null);
		if (cursor != null)
		{
			cursor.moveToFirst();
		}
		return cursor.getString(0);
	}
	*/
}
