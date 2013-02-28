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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;

public class WordHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "words.db";
	private static final int SCHEMA_VERSION=1;
	
	public WordHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Words (_id INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT);");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public void insert(String word) {
		
		ContentValues cv = new ContentValues();
		cv.put("word", word);
		
		//must pass at least one name of column
		getWritableDatabase().insert("Words", "word", cv);
	}
	
	public Cursor getAll() {
		return(getReadableDatabase().rawQuery("SELECT _id, word FROM Words", null));
	}
	
	public String getNote(Cursor c) {
		return(c.getString(1));
	}
}
