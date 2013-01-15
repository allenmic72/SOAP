package edu.neu.madcourse.jameshardy;


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
        setContentView(R.layout.activity_main);
        
        View teamMembersButton = findViewById(R.id.team_member_button);
        teamMembersButton.setOnClickListener(this);
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
    
    public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.team_member_button:
    		Intent i = new Intent(this, TeamMembersActivity.class);
    		startActivity(i);
    		break;
    	}
    }
    
}
