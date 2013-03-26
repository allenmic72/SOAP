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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MP_BoggleChallengeUser extends ListActivity {
	private static final String TAG = "MP Boggle";

	public static final String USER_NAME = "edu.neu.madcourse.jameshardy.multiplayerboggle.user_name";
	public static final String PHONE_NUM = "edu.neu.madcourse.jameshardy.multiplayerboggle.phone_num";
	
	private String userName;
	private String phoneNum;
	private Gson g = new Gson();
	private List<MP_BoggleUser> mp_users = new ArrayList<MP_BoggleUser>();
	private Type listOfUsers = new TypeToken<List<MP_BoggleUser>>(){}.getType();
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.mp_boggle_challengers)
		Bundle b = getIntent().getExtras();
		userName = b.getString(USER_NAME);
		phoneNum = b.getString(PHONE_NUM);
		
		String users = "";
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
		ListView challengers = (ListView) findViewById(R.id.mp_boggle_challenge_list);
		
		ArrayAdapter<String> adapter = 
				new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, usernames);
		challengers.setAdapter(adapter);
		*/
		setListAdapter(new ArrayAdapter<String>(this, R.layout.mp_boggle_challengers, R.id.mp_boggle_challengers_textview, usernames));
		
		
		//View enterButton = findViewById(R.id.mp_boggle_challenge_user_button);
		//enterButton.setOnClickListener(this);
		
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		//super.onListItemClick(l, v, position, id);
		String selected_user = l.getItemAtPosition(position).toString();
		Toast.makeText(this, "Sending Invite to " + selected_user, Toast.LENGTH_LONG).show();
		
		MP_BoggleUser user = new MP_BoggleUser();
		for (int i = 0; i <mp_users.size(); i++) {
			user = mp_users.get(i);
			if (user.getName().equals(selected_user)) break;
		}
		String DELIVERED = "SMS_DELIVERED";
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
	            new Intent(DELIVERED), 0);
		//---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED)); 
        
        //TODO
        String newGameID = userName+user.getName();
        
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(user.getNumber(), null, "Invitation to game: " + newGameID, null, deliveredPI);
		//sms.sendDataMessage(user.getNumber(), null, 50009, user, null, deliveredPI);
	
	}
	


}
