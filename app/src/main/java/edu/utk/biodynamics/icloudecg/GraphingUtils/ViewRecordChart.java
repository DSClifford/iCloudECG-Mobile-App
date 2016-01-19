package edu.utk.biodynamics.icloudecg.GraphingUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.utk.biodynamics.icloudecg.R;

/**
 * Created by DSClifford on 8/8/2015.
 */

//Popup display for stored record graph viewing

public class ViewRecordChart extends Activity {

    private static boolean apiTooLowForImmersive = false;
    private LineChart mChart;
    private ArrayList<Short> ECG_Data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {

            apiTooLowForImmersive = true;

            //requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        setContentView(R.layout.activity_view_record_chart);
        setupActionBar();

        String basepath = getFilesDir().getAbsolutePath();
        String recordID = getIntent().getStringExtra("recordID");

        TextView tv_upldDate = (TextView) findViewById(R.id.record_date_time);
        tv_upldDate.setText(getIntent().getStringExtra("upldDate"));
        TextView tv_diagnosis = (TextView) findViewById(R.id.record_diagnosis);
        tv_diagnosis.setText("Diagnosis: "+getIntent().getStringExtra("diagnosis"));

        try {
            InputStream dataIn = new FileInputStream(basepath + "/records/" + recordID + "retrieved" + ".txt");
                    //new FileInputStream(basepath + "/records/" + recordID + "retrieved" + ".txt");
            InputStreamReader dataReader = new InputStreamReader(dataIn);
            BufferedReader buffReader = new BufferedReader(dataReader);


            ECG_Data = new ArrayList<Short>();
            String dataLine; //declare a variable
            while((dataLine=buffReader.readLine())!=null)
                ECG_Data.add(Short.parseShort(dataLine));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("Debuggy", "windowFocusChange");
        if (hasFocus) {
            Log.d("Debuggy", "hasFocus");

            if (!apiTooLowForImmersive) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setUpChart();
        getChartData();

    }

    private void getChartData() {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        Short[] dataList = ECG_Data.toArray(new Short[ECG_Data.size()]);
        for (int i = 0; i < dataList.length; i++) {

            yVals1.add(new Entry(dataList[i], i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals1, "DataSet 1");
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setDrawCircles(false);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < dataList.length; i++) {
            xVals.add((i) + "");
        }

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        float avgY = data.getYValueSum()/data.getYValCount();
        float maxY = data.getYMax();
        float minY = data.getYMin();
        float maxMinMargin = (float)((maxY - minY) * 0.1);
        Log.d("Debug","MaxY: "+maxY);
        Log.d("Debug", "MinY: " + minY);

        mChart.setData(data);
        mChart.zoom(5,1,0,avgY);
        mChart.invalidate();


        YAxis leftAxis = mChart.getAxisLeft();
        if(maxY<1.2*avgY && minY>0.8*avgY) {
            leftAxis.setAxisMaxValue(maxY + maxMinMargin);
            leftAxis.setAxisMinValue(minY - maxMinMargin);
        }
        leftAxis.setStartAtZero(false);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);






    }

    public void backBtnAction(View view) {
        finish();
    }

    public void setUpChart() {
        mChart = (LineChart) findViewById(R.id.record_chart_view);
        //mChart.setOnChartGestureListener(this);
        //mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        //mChart.setScaleEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(false);


        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        XAxis xAxis = mChart.getXAxis();
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);

    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            if (getActionBar() != null) {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}