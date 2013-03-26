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

public class MP_BoggleChallengeUser extends Activity {
	private static final String TAG = "MP Boggle";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mp_boggle_challengers);
		
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
		for (int i = 0; i <mp_users.size(); i++) {
			MP_BoggleUser bu = new MP_BoggleUser();
			bu = mp_users.get(i);
			usernames.add(bu.name);
		}
		/*
		Spinner oppSpin = (Spinner) findViewById(R.id.mp_boggle_opponent_spinner);
		
		ArrayAdapter<String> adapter = 
				new ArrayAdapter<String>(this, R.id.mp_boggle_opponent_spinner, usernames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		oppSpin.setAdapter(adapter);
		*/
		ListView challengers = (ListView) findViewById(R.id.mp_boggle_challenge_list);
		
		ArrayAdapter<String> adapter = 
				new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, usernames);
		challengers.setAdapter(adapter);
		
		/*
		challengers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onListItemClick() {
				//TODO
			}
		});
		*/
		
		//View enterButton = findViewById(R.id.mp_boggle_challenge_user_button);
		//enterButton.setOnClickListener(this);
		
	}
	
	/*
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.mp_boggle_challenge_user_button:
			//TODO
			break;

		}
	}
	*/
	/*
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		
	}
	*/


}
