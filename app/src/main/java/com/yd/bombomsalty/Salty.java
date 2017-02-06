package com.yd.bombomsalty;

/**
 * Created on 31.01.2017
 @author Yury.
 */

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.Window;


public class Salty extends Activity {

    private static final String LOG_TAG = "MainAct";
    private GameView mGameView;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "create");
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //new View with main game background
        mGameView = new GameView(this);
        setContentView(mGameView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_salt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();

        Log.d(LOG_TAG, "resume");
        //mGameView.resume();
    }

    @Override
    public void onPause(){
        super.onPause();

        Log.d(LOG_TAG, "pause");
        //mGameView.pause();
    }

    @Override
    public void onStop(){
        super.onStop();

        Log.d(LOG_TAG, "stop");
    }
}
