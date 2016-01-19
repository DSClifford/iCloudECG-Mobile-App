package edu.utk.biodynamics.icloudecg;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import edu.utk.biodynamics.icloudecg.DatabaseUtils.HttpManager;
import edu.utk.biodynamics.icloudecg.GraphingUtils.ViewRecordChart;
import zephyr.android.BioHarnessBT.BTClient;
import zephyr.android.BioHarnessBT.ZephyrProtocol;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ViewRecordsFragment.Callback,
        ViewECGFragment.OnFragmentInteractionListener, ViewAlerts.OnFragmentInteractionListener,
        ViewRecordsFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    //BT Objects/classes
    BluetoothAdapter adapter = null;
    BTClient _bt;
    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;
    boolean connected = false;

    //BT message packet ids
    private final int ECG_MSG_ID = 0x22;
    private final int RtoR_MSG_ID = 0x24;
    private final int HEART_RATE = 0x100;


    //specifications for graphing ECG data and packaging data arrays
    int ECG_Time;
    int ECG_Length;
    int ECG_samples;
    short ECG_packet;
    int ECG_NUM;
    short ECG_Data[];
    short ECG_toAnalyze[];
    float ECGmin;
    float ECGmax;
    int count;

    //specifications for RtoR data
    int RtoR_samples=10;
    int RtoR_packet=18;
    int RtoR_NUM=RtoR_samples*RtoR_packet;
    int RtoR_Data[] = new int[RtoR_NUM];
    int RtoR_Long = 200;
    double RtoR_Data_Long[] = new double [RtoR_Long];


    //Sensor data variables
    int HRnum = 10;
    short HR_Data[] = new short[HRnum];
    private double heartRate;
    private double respRate;
    private String battStatus;

    //Used to time left-right graphing of ECG
    private int chartSwitch;

    public Intent bindtestIntent;
    BHInterfaceService mService;

    ProgressBar pb;
    //ECG Graph chart
    LineChart mChart;

    MenuItem connectIcon;

    public final static String ACTION_RECORD_FLAGGED =
            "edu.utk.biodynamics.ACTION_RECORD_FLAGGED";

    public static final String PREFS_NAME = "MyPreferencesFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Context context = MainActivity.this;

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //Filtering BT requests/actions
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        //Registering a new BTBroadcast receiver from the Main Activity context with pairing request event
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);

        //initialize the data for the graph (will be overwritten)
        ECG_Time = MainActivity.this.getResources().getInteger(R.integer.ECG_Chart_Time);
        ECG_Length = 252*ECG_Time;
        ECG_samples=10;
        ECG_packet = 63;
        ECG_NUM = ECG_packet*ECG_samples;
        ECG_Data = new short[ECG_Length];
        ECGmax = 511;
        ECGmin = 459;
        chartSwitch = 0;

        for(int x=0;x<ECG_NUM;x++) {ECG_Data[x]=500;}
        for(int x=0;x<HRnum;x++) {HR_Data[x]=60;}
        for(int x=0;x<RtoR_NUM;x++) {RtoR_Data[x]=60;}
        for(int x=0;x<RtoR_Long;x++) {RtoR_Data_Long[x]=0;}

        // Braodcast listener for data from NewConnectedListener
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("newData"));


    }

    //Receives BT data broadcasts, initializes data fields & chart, fills in received data to text fields and graph
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

                ECG_Data = intent.getShortArrayExtra("ECGData");
                heartRate = intent.getDoubleExtra("HeartRate", 0.0);
                respRate = intent.getDoubleExtra("RespRate",0.0);
                battStatus = intent.getStringExtra("BattStat");
                count = intent.getIntExtra("Count",0);

                    if (heartRate==0) {
                        TextView HRTv = (TextView) findViewById(R.id.hr_num);
                        if (HRTv != null) {
                            HRTv.setText("...");
                        }
                    }else {
                        TextView HRTv = (TextView) findViewById(R.id.hr_num);
                        if (HRTv != null) {
                            HRTv.setText(String.valueOf(heartRate));
                        }
                    }

                    if (respRate==0.0) {
                        TextView BRTv = (TextView) findViewById(R.id.br_num);
                        if (BRTv != null) {
                            BRTv.setText("...");
                        }
                    }else{
                    TextView BRTv = (TextView) findViewById(R.id.br_num);
                    if (BRTv != null) {
                        BRTv.setText(String.valueOf(respRate));
                    }
                }
                if (battStatus != null) {
                    TextView BattTv = (TextView) findViewById(R.id.batt_num);
                    if (BattTv != null) {
                        BattTv.setText(" " + battStatus + "%");
                    }
                }else{
                    TextView BattTv = (TextView) findViewById(R.id.batt_num);
                    if (BattTv != null) {
                        BattTv.setText("...%");
                    }
                }
                //  ... react to local broadcast message
                //String toastmessage = "Test1: " + test1 + " & " + "Test2: " + test2 + " & " + "Test3: " + test3;
                //Toast.makeText(getApplicationContext(), toastmessage, Toast.LENGTH_SHORT).show();


            //Checks to confirm ViewECGFragment fragment is active, and fills in ECG data array to be graphed
            //Also configures various graph properties
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragID = fragmentManager.findFragmentById(R.id.container);
                ViewECGFragment.setPBVis(1);
                connected=true;
                if(connectIcon != null) {
                    connectIcon.setIcon(R.drawable.bt_c);
                }
                if (currentFragID != null) {
                    if (currentFragID.toString().contains("ViewECGFragment")) {

                        findViewById(R.id.no_bh_popup).setVisibility(View.GONE);
                        ToggleButton pauseBtn = (ToggleButton) findViewById(R.id.pauseToggle);
                        Boolean pause = pauseBtn.isChecked();
                        if(!pause) {
                            if (findViewById(R.id.ECGgraph) != null && chartSwitch == 0) {
                                setUpChart();
                                findViewById(R.id.ECGgraph).setVisibility(View.VISIBLE);
                                chartSwitch = 1;
                            }

                            ArrayList<Entry> yVals1 = new ArrayList<Entry>();

                            for (int i = 0; i < ECG_Data.length; i++) {

                                yVals1.add(new Entry(ECG_Data[i], i));
                            }

                            // create a dataset and give it a type
                            LineDataSet set1 = new LineDataSet(yVals1, "DataSet 1");
                            set1.setColor(Color.RED);
                            set1.setCircleColor(Color.RED);
                            set1.setLineWidth(1f);
                            set1.setDrawCircles(false);
                            set1.setDrawCircleHole(false);
                            set1.setValueTextSize(9f);
                            set1.setFillAlpha(65);
                            set1.setFillColor(Color.BLACK);

                            ArrayList<String> xVals = new ArrayList<String>();
                            for (int i = 0; i < ECG_Data.length; i++) {
                                xVals.add((i) + "");
                            }

                            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                            dataSets.add(set1); // add the datasets

                            // create a data object with the datasets
                            LineData data = new LineData(xVals, dataSets);

                            // set data
                            float avgY = data.getYValueSum() / data.getYValCount();
                            float maxY = data.getYMax();
                            float minY = data.getYMin();
                            float maxMinMargin = 0;//(float)((maxY - minY) * 0.07);

                            mChart.setData(data);
                            mChart.invalidate();
                            mChart.getLegend().setEnabled(false);

                            float xLimitLine = count * ECG_packet;
                            LimitLine limitLine = new LimitLine(xLimitLine);
                            limitLine.setLineWidth(6f);
                            limitLine.setLineColor(0xfff3f3f3);

                            YAxis leftAxis = mChart.getAxisLeft();
                            if (maxY < 1.2 * avgY && minY > 0.8 * avgY) {
                                leftAxis.setAxisMaxValue(maxY + maxMinMargin);
                                leftAxis.setAxisMinValue(minY);
                            }
                            leftAxis.setStartAtZero(false);
                            leftAxis.setDrawLabels(false);
                            leftAxis.setDrawGridLines(false);
                            mChart.getXAxis().removeAllLimitLines();
                            mChart.getXAxis().addLimitLine(limitLine);

                        }
                    }
                }
            }

    };

    public void setUpChart() {
        mChart = (LineChart) findViewById(R.id.ECGgraph);
        //mChart.setOnChartGestureListener(this);
        //mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("");
        mChart.setNoDataText("");
        // enable touch gestures
        mChart.setTouchEnabled(false);

        // enable scaling and dragging
        mChart.setDragEnabled(false);
        //mChart.setScaleEnabled(true);
        mChart.setScaleXEnabled(false);
        mChart.setScaleYEnabled(false);


        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.getXAxis().setDrawLabels(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getAxisLeft().setEnabled(false);
    }

    //ECG Record table on item selected action
    @Override
    public void onItemSelected(String recordID, String upldDate, String diagnosis) {
        GetRecordData dataGetter = new GetRecordData();
        dataGetter.execute(recordID,upldDate,diagnosis);

    }

    //Reads ECG data from file and launches graph view dialog
    private class GetRecordData extends AsyncTask<String, String, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            String basepath = getFilesDir().getAbsolutePath();

            boolean result = HttpManager.getFile("biodynamics.engr.utk.edu", "dcliffo3", "*******", params[0], basepath);
            if(result) {
                return params;
            }else{
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String[] result) {

        if (result!=null) {
            Intent intent = new Intent(MainActivity.this, ViewRecordChart.class)
                    .putExtra("recordID",result[0])
                    .putExtra("upldDate",result[1])
                    .putExtra("diagnosis",result[2]);
            startActivity(intent);
        }else {

        }

        }
    }


    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
        }
    }
    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            try {
                BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", String.class);
                byte[] pin = (byte[])m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", pin.getClass());
                Object result = m.invoke(device, pin);
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

       @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = new Fragment();
        switch (position) {
            case 0:
                fragment = new ViewECGFragment();
                pb = (ProgressBar) findViewById(R.id.ecg_prog_bar);
                chartSwitch=0;
                break;
            case 1:
                fragment = new ViewRecordsFragment();
                mTitle = getString(R.string.title_my_records);
                chartSwitch=0;
                break;
            case 2:
                fragment = new ViewAlerts();
                mTitle = getString(R.string.title_view_alerts);
                chartSwitch=0;
                break;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
       }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            connectIcon = menu.findItem(R.id.connectIcon);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void connectToBH(View view){
        bindtestIntent = new Intent(MainActivity.this, BHInterfaceService.class);
        startService(bindtestIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int selected = item.getItemId();
        switch(selected) {
            //connect selected
            case R.id.connect:
            case R.id.connectIcon:
                if(connected==false) {
                    //ViewECGFragment.setPBVis(0);
                    bindtestIntent = new Intent(MainActivity.this, BHInterfaceService.class);
                            startService(bindtestIntent);
                }
                break;
            //disconnect selected
            case R.id.disconnect:
            //    if(connected==true) {

                Intent intent = new Intent(this, BHInterfaceService.class);
                bindService(intent, mServerConn, Context.BIND_AUTO_CREATE);
                //mService.testShit();

                    connected=false;
              //  }
                break;

            case R.id.testNotification:

                Runnable r = new Runnable() {
                    public void run() {


                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String gcmToken = sharedPreferences.getString(getString(R.string.gcm_token_keyname), null);
                        HttpManager.testNotification(gcmToken);

                    }

                };

                new Thread(r).start();

                //		MyTask task = new MyTask();
                //		task.execute();

                break;

            case R.id.action_settings:

                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);

                break;
        }
        return false;
    }

    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BHInterfaceService.LocalBinder binder = (BHInterfaceService.LocalBinder) service;
            mService = binder.getService();
            stopService(bindtestIntent);
            mService.killMyself();
            unbindService(mServerConn);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void bind() {
        // mContext is defined upper in code, I think it is not necessary to explain what is it
        bindService(new Intent(MainActivity.this, BHInterfaceService.class), mServerConn, Context.BIND_AUTO_CREATE);

    }

    public void unbind() {
        stopService(new Intent(MainActivity.this, BHInterfaceService.class));
        unbindService(mServerConn);
    }



    private class BHConnect extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            String BhMacID = "00:07:80:9D:8A:E8";
            //String BhMacID = "00:07:80:88:F6:BF";
            adapter = BluetoothAdapter.getDefaultAdapter();

            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

            if (pairedDevices.size() > 0)
            {
                for (BluetoothDevice device : pairedDevices)
                {
                    if (device.getName().startsWith("BH"))
                    {
                        BluetoothDevice btDevice = device;
                        BhMacID = btDevice.getAddress();
                        break;
                    }
                }
            }

            BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
            String DeviceName = Device.getName();
            _bt = new BTClient(adapter, BhMacID);



            if(_bt.IsConnected())
            {
                _bt.start();
                updateConnStatus(0);
                connected=true;

            }
            else
            {
                updateConnStatus(1);
                connected=false;
            }

            return null;
        }


        @Override
        protected void onPostExecute(final Void success) {

        }
    }



    private void updateConnStatus(int flag) {

        switch (flag) {
            case 0:
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.connection_status);
                        String ErrorText;
                        ErrorText = "Connected to BioHarness";
                        tv.setText(ErrorText);
                    }
                });

                break;
            case 1:
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.connection_status);
                        String ErrorText;
                        ErrorText = "Unable to Connect";
                        tv.setText(ErrorText);
                    }
                });

                break;
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
