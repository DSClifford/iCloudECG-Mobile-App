package edu.utk.biodynamics.icloudecg;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Set;
import java.util.Timer;

import edu.utk.biodynamics.icloudecg.DatabaseUtils.DBUpdateService;
import edu.utk.biodynamics.icloudecg.DatabaseUtils.ECGFileWriter;
import edu.utk.biodynamics.icloudecg.DatabaseUtils.ECGRecord;
import edu.utk.biodynamics.icloudecg.DatabaseUtils.HttpManager;
import zephyr.android.BioHarnessBT.BTClient;
import zephyr.android.BioHarnessBT.ZephyrProtocol;

//Service to orchestrate all BT interactions and data handling between NewConnectedListener and MainActivity

public class BHInterfaceService extends Service {

    private final int ECG_MSG_ID = 0x22;
    private final int RtoR_MSG_ID = 0x24;
    private final int HEART_RATE = 0x100;
    //other packet options if included from NewConnectedListener
	private final int RESPIRATION_RATE = 0x101;
	private final int SKIN_TEMPERATURE = 0x102;

    //specifications for graphing ECG data
    int ECG_Time;
    int ECG_Length;
    int ECG_samples;
    short ECG_packet;
    int ECG_NUM;
    short ECG_Data[];
    short ECG_30Sec[];
    int count = 0;
    int count2 = 0;

    Handler handler;
    private int[] HRArrayToAvg;
    private double[] HR30SecArray;
    private double HRAvg;
    private double maxHR;
    private int heartRate;
    private double[] BRArrayToAvg;
    private double[] BR30SecArray;
    private double BRAvg;
    private double respRate;
    private String battStat;
    private Boolean flagged;

    BluetoothAdapter adapter = null;
    BTClient _bt;
    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;
    boolean connected = false;
    private final IBinder mBinder = new LocalBinder();

    private static Timer timer = new Timer();
    private Context ctx;

    public void onCreate() {
        super.onCreate();
        ctx = this;

        flagged = false;

        handler = new Handler();

        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        //Registering a new BTBroadcast receiver from the Main Activity context with pairing request event
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);


        BHConnecter bhConnecter = new BHConnecter();
        bhConnecter.execute();

        ECG_Time = getResources().getInteger(R.integer.ECG_Chart_Time);
        ECG_Length = 252*ECG_Time;
        ECG_samples=10;
        ECG_packet = 63;
        ECG_NUM = ECG_packet*ECG_samples;
        ECG_Data = new short[ECG_Length];
        ECG_30Sec = new short[30*252];
        HRArrayToAvg = new int[ECG_Length/ECG_packet];
        BRArrayToAvg = new double[ECG_Length/ECG_packet];
        HR30SecArray = new double[(30*252)/(ECG_Length)];
        BR30SecArray = new double[(30*252)/(ECG_Length)];

        for(int x=0;x<ECG_Data.length;x++) {ECG_Data[x]=500;}

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
                byte[] pin = (byte[]) m.invoke(device, "1234");
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
    public int onStartCommand (Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(MainActivity.ACTION_RECORD_FLAGGED)) {
                flagged = true;
                Log.e("EventFlag", "Pressed");
            }
        }
        return START_NOT_STICKY;
    }


    private class BHConnecter extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            String BhMacID = "00:07:80:9D:8A:E8";
            //String BhMacID = "00:07:80:88:F6:BF";
            adapter = BluetoothAdapter.getDefaultAdapter();

            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().startsWith("BH")) {
                        BluetoothDevice btDevice = device;
                        BhMacID = btDevice.getAddress();
                        break;
                    }
                }
            }

            BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
            String DeviceName = Device.getName();
            _bt = new BTClient(adapter, BhMacID);
            _NConnListener = new NewConnectedListener(Newhandler, Newhandler);
            _bt.addConnectedEventListener(_NConnListener);

            if (_bt.IsConnected()) {
                _bt.start();
                //startTimedService();

                connected = true;

            } else {

                connected = false;
            }

            return null;
        }


        @Override
        protected void onPostExecute(final Void success) {


        }
    }

    /*
    private void startTimedService() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        long intervalHours = sharedPreferences.getInt("lengthMinutes",0);
        long intervalMinutes = sharedPreferences.getInt("lengthSeconds",0);
        long intervalMilliseconds = ((intervalHours*60)+intervalMinutes)*60*1000;
        if(intervalMinutes==1){
            intervalMilliseconds = 20000;
        }

        timer.scheduleAtFixedRate(new mainTask(),intervalMilliseconds, intervalMilliseconds);
    }
    */

    private class mainTask {
        public void run() {
            if (_bt.IsConnected()) {
                //toastHandler.sendEmptyMessage(0);
                final ECGRecord newRecord = makeNewRecord();
                sendRecordToDB(newRecord.getId());

                Runnable r = new Runnable() {
                    public void run() {

                        ECGFileWriter.newFile(ECG_30Sec, newRecord.getId(), newRecord.getBasePath());
                        //Possible issue if ECG_toAnalyze changes while this thread is running?
                        Context ctx = getApplicationContext();
                        HttpManager.sendFile("biodynamics.engr.utk.edu", "dcliffo3", "********", newRecord.getId(), newRecord.getBasePath());
                        HttpManager.updateECGDatabase(ctx, newRecord);
                        HttpManager.initiateAnalysis(newRecord.getId(),newRecord.getEmail());
                        // requestData("http://biodynamics.engr.utk.edu/iCloudECGAppBridge/loginreturn.php");
                        flagged=false;
                    }
                };

                new Thread(r).start();
            }
        }

        private ECGRecord makeNewRecord() {
            final String randomID = RandomID.generateString();
            final String basepath = getFilesDir().getAbsolutePath();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            String email = prefs.getString("email", "asd");
            String gender = prefs.getString("gender", "NP");
            String fname = prefs.getString("first_name", "NP");

            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            int byear = prefs.getInt("byear",today.get(Calendar.YEAR));
            int bmonth = prefs.getInt("bmonth", today.get(Calendar.MONTH));
            int bday = prefs.getInt("bday", today.get(Calendar.DAY_OF_MONTH));
            dob.set(byear, bmonth, bday);
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                age--;
            }

            ECGRecord record = new ECGRecord();
            record.setEmail(email);
            record.setPatientName(fname);
            record.setPatientGender(gender);
            record.setPatientAge(age);
            record.setId(randomID);
            record.setMaxHR(getMaxValue(HR30SecArray));
            record.setMaxBR(getMaxValue(BR30SecArray));
            record.setBasePath(basepath);

            if(flagged) {
                record.setRecord_notes("Flagged");
            }else{
                record.setRecord_notes("None");
            }


            return record;
        }
    }

    private void sendRecordToDB(String id) {

        Intent intent = new Intent(BHInterfaceService.this, DBUpdateService.class);
        intent.putExtra("maxHR",getMaxValue(HR30SecArray));
        intent.putExtra("recordID",id);
        intent.putExtra("flagged",flagged);
        Log.e("SendRecToDB Flagged: ",String.valueOf(flagged));
        startService(intent);

    }

    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
    }

    public void killMyself(){
        _bt.Close();
        stopSelf();
    }

    private void sendNewDataToUI() {
        Intent intent = new Intent("newData");
        sendNewData(intent);
    }

    private void sendNewData(Intent intent){
        intent.putExtra("HeartRate",HRAvg);
        intent.putExtra("RespRate",BRAvg);
        intent.putExtra("ECGData", ECG_Data);
        intent.putExtra("BattStat",battStat);
        intent.putExtra("Count",count);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    final Handler Newhandler = new Handler() {
        public void handleMessage(Message msg) {
            TextView tv;
            switch (msg.what) {
                //get bioharness heartrate

                case HEART_RATE:
                    heartRate = msg.getData().getInt("HeartRate");

                    break;

                case RESPIRATION_RATE:
                    respRate = msg.getData().getDouble("RespirationRate");
                    break;

                case SKIN_TEMPERATURE:
                    battStat = msg.getData().getString("SkinTemperature");
                    break;

                //get ecg signals
                case ECG_MSG_ID:
                    short ECG[] = msg.getData().getShortArray("ECG");

                    if(count==ECG_Length/ECG_packet){
                        count=0;

                        int sum = 0;
                        for (int hr : HRArrayToAvg) sum += hr;
                        HRAvg = 1.0d * sum / HRArrayToAvg.length;

                        sum = 0;
                        for (double br : BRArrayToAvg) sum += br;
                        BRAvg = sum / BRArrayToAvg.length;

                        if (count2==(252*30)/ECG_Length){
                            count2=0;
                            mainTask MainTask = new mainTask();
                            MainTask.run();
                        }
                        int k =0;
                        for (int i = count2*ECG_Length; i < (count2+1)*ECG_Length ; i++) {

                            ECG_30Sec[i] = ECG_Data[k];
                            k++;
                        }
                        HR30SecArray[count2] = HRAvg;
                        BR30SecArray[count2] = BRAvg;
                        count2++;

                    }
                    int j =0;
                    for (int i = count*ECG_packet; i < (count+1)*ECG_packet ; i++) {
                        
                        ECG_Data[i] = ECG[j];
                        j++;

                        HRArrayToAvg[count] = heartRate;
                        BRArrayToAvg[count] = respRate;

                    }

                    count++;

                    sendNewDataToUI();
            //30 Second Record Building

            }
        }
    };

    public static double getMaxValue(double[] array){
        double maxValue = array[0];
        for(int i=1;i < array.length;i++){
            if(array[i] > maxValue){
                maxValue = array[i];

            }
        }
        return maxValue;
    }

    private void newConnListener() {
        _NConnListener = new NewConnectedListener(Newhandler, Newhandler);
        _bt.addConnectedEventListener(_NConnListener);

    }


    public class LocalBinder extends Binder {
        BHInterfaceService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BHInterfaceService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder ;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return true;
    }
}
