package edu.neu.madcourse.jameshardy;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.telephony.*;
import android.widget.*;

public class TeamMembersActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_members);
        
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);;
        String devId = tm.getDeviceId();
        
        TextView textOut;
        textOut = (TextView)findViewById(R.id.devIdText);
        textOut.setText("Device ID: " + devId);
    }

}
