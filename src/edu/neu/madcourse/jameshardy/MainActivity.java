package edu.neu.madcourse.jameshardy;

import edu.neu.madcourse.jameshardy.MultiplayerBoggle.*;
import edu.neu.madcourse.jameshardy.Boggle.*;
import edu.neu.madcourse.jameshardy.Sudoku.*;
import edu.neu.madcourse.jameshardy.finalproject.*;
import edu.neu.mobileClass.*;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_launch_window);
        
        //DON'T FORGET TO ADD THIS IN
        //PhoneCheckAPI.doAuthorization(this);
        
        View teamMembersButton = findViewById(R.id.team_members_button);
        teamMembersButton.setOnClickListener(this);
        
        View sudokuButton = findViewById(R.id.sudoku_button);
        sudokuButton.setOnClickListener(this);
        
        View boggleButton = findViewById(R.id.boggle_button);
        boggleButton.setOnClickListener(this);
        
        View multiplayerBoggleButton = findViewById(R.id.multiplayer_boggle_button);
        multiplayerBoggleButton.setOnClickListener(this);
        
        View crashButton = findViewById(R.id.crash_button);
        crashButton.setOnClickListener(this);
        
        View soapButton = findViewById(R.id.soap_gui_button);
        soapButton.setOnClickListener(this);
        
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
        
        View finalProjButton = findViewById(R.id.final_proj_button);
        finalProjButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
       super.onResume();
    }

    @Override
    protected void onPause() {
       super.onPause();
    }
    
    Intent i;
    public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.team_members_button:
    		i = new Intent(this, TeamMembersActivity.class);
    		startActivity(i);
    		break;
    	case R.id.sudoku_button:
    		i = new Intent(this, Sudoku.class);
    		startActivity(i);
    		break;
    	case R.id.boggle_button:
    		i = new Intent(this, Boggle.class);
    		startActivity(i);
    		break;
    	case R.id.multiplayer_boggle_button:
    		/*
    		if (MultiplayerBoggle.userName.length() == 0) {
    			i = new Intent(this, MP_BoggleCreateUser.class);
    			startActivity(i);
    		}
    		else {
    			i = new Intent(this, MultiplayerBoggle.class);
        		startActivity(i);
    		}
    		*/
    		i = new Intent(this, MP_BoggleCreateUser.class);
			startActivity(i);
    		break;
    	case R.id.crash_button:
    		i = new Intent(this, CrashActivity.class);
    		startActivity(i);
    		break;
    	case R.id.soap_gui_button:
    		i = new Intent(this, SoapGUI.class);
    		//i = new Intent(this, AccelerometerActivity.class);
    		startActivity(i);
    		break;
    	case R.id.exit_button:
    		finish();
    		break;
    	case R.id.final_proj_button:
    		i = new Intent(this, FinalProjActivity.class);
    		startActivity(i);
    		break;
    	}
    }
    
}
