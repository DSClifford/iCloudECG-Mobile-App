package edu.utk.biodynamics.icloudecg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DSClifford on 8/8/2015.
 */

//Activity to display received analysis results

public class AnalysisResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_results);
        Intent messageIntent = getIntent();
        String message = messageIntent.getStringExtra("message");
        TextView analysisText = (TextView) findViewById(R.id.alert_message);
        analysisText.setText(message);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        TextView dateTimeTV = (TextView) findViewById(R.id.analysisTimeTV);
        dateTimeTV.setText(strDate);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_analysis_results, menu);
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
