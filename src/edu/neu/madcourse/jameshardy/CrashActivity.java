package edu.neu.madcourse.jameshardy;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class CrashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        
        //CRASH APP -- No one likes null pointers ;)
        TextView tv = null;
        tv.getContext();
        //CRASH
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_crash, menu);
        return true;
    }
}
