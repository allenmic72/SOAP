package edu.neu.madcourse.jameshardy.Boggle;

import android.app.Application;
//import android.content.Context;
import edu.neu.madcourse.jameshardy.Boggle.DatabaseTable;

//import edu.neu.madcourse.jameshardy.Boggle.Boggle;

/*
 public class DatabaseSingleton {

 private static final DatabaseSingleton instance = new DatabaseSingleton();

 private static DatabaseTable mDB;

 public static DatabaseSingleton getInstance() {
 return instance;
 }

 private DatabaseSingleton() {
 mDB = new DatabaseTable(Context.getApplicationContext());
 }

 }
 */

public class DatabaseSingleton extends Application {

	private static DatabaseSingleton instance;

	public static DatabaseTable mDB;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mDB = new DatabaseTable(this);
	}

	public static DatabaseSingleton getInstance() {
		return instance;
	}

	private DatabaseSingleton() {
	}

}