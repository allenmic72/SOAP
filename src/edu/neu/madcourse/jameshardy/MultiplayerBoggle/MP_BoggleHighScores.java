package edu.neu.madcourse.jameshardy.MultiplayerBoggle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.mobileclass.apis.KeyValueAPI;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class MP_BoggleHighScores extends Activity {
	private static final String TAG = "MP Boggle";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mp_boggle_high_scores);
		
		Gson g = new Gson();
		Type listOfUsers = new TypeToken<List<MP_BoggleUser>>(){}.getType();
		String users = "";
		List<MP_BoggleUser> mp_users = new ArrayList<MP_BoggleUser>();
		//List<MP_BoggleUser> mp_users;
		
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
		
		List<String> usernames = new ArrayList<String>();
		int[] scores = new int[mp_users.size()];
		for (int i = 0; i <mp_users.size(); i++) {
			MP_BoggleUser bu = new MP_BoggleUser();
			bu = mp_users.get(i);
			scores[i] = bu.getScore();
			usernames.add(bu.name);
		}
		
		//assemble high score strings
		List<String> highscores = new ArrayList<String>();
		for (int i = 0; i <mp_users.size(); i++) {
			String temp = new String(usernames.get(i) + "    Score: " + scores[i]);
			highscores.add(temp);
		}
		/*
		Spinner oppSpin = (Spinner) findViewById(R.id.mp_boggle_opponent_spinner);
		
		ArrayAdapter<String> adapter = 
				new ArrayAdapter<String>(this, R.id.mp_boggle_opponent_spinner, usernames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		oppSpin.setAdapter(adapter);
		*/
		ListView challengers = (ListView) findViewById(R.id.mp_boggle_highscores_list);
		
		ArrayAdapter<String> adapter = 
				new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, highscores);
		challengers.setAdapter(adapter);
		
	
		
	}
	



}
