package edu.utk.biodynamics.icloudecg.LoginRegister;

/**
 * Created by DSClifford on 8/8/2015.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import edu.utk.biodynamics.icloudecg.MainActivity;
import edu.utk.biodynamics.icloudecg.R;
import edu.utk.biodynamics.icloudecg.util.RegistrationIntentService;

//Main login/register activity, containing tabs "LoginTab" and "RegisterTab"

public class LoginRegister extends AppCompatActivity {

    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Login","Register"};
    int Numboftabs =2;
    ProgressBar pb;
    SharedPreferences sharedPreferences;
    FrameLayout gcmPBLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getString("email",null)!= null && sharedPreferences.getString("password",null)!=null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        pb = (ProgressBar) findViewById(R.id.gcmreg_progbar);
        pb.setVisibility(View.VISIBLE);
        gcmPBLayout = (FrameLayout) findViewById(R.id.gcmprog_layout);
        gcmPBLayout.setVisibility(View.VISIBLE);
        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        String gcmToken = sharedPreferences.getString(getString(R.string.gcm_token_keyname),null);
        if(gcmToken == null){
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                    new IntentFilter(getString(R.string.intent_name_REGISTRATION_COMPLETE)));

            startService(new Intent(this, RegistrationIntentService.class));

        }else{
            pb.setVisibility(View.INVISIBLE);
            gcmPBLayout.setVisibility(View.INVISIBLE);
            Log.d("GMCToken", "Already Set: " + gcmToken);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String token = sharedPreferences.getString(getString(R.string.gcm_token_keyname),null);
            pb.setVisibility(View.INVISIBLE);
            gcmPBLayout.setVisibility(View.INVISIBLE);
            Log.d("GCMToken", "Just Set" + token);
            LocalBroadcastManager.getInstance(LoginRegister.this).unregisterReceiver(mMessageReceiver);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_register, menu);
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
}
