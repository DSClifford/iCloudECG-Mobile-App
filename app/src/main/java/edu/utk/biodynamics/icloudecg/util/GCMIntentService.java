package edu.utk.biodynamics.icloudecg.util;

/**
 * Created by DSClifford on 9/14/2015.
 */

import android.app.IntentService;
import android.content.Intent;

public class GCMIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GCMIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

/*    private static final String PROJECT_ID = "xxx";

    private static final String TAG = "GCMIntentService";

    public GCMIntentService()
    {
        super(PROJECT_ID);
        Log.d(TAG, "GCMIntentService init");
    }


    @Override
    protected void onError(Context ctx, String sError) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Error: " + sError);

    }

    @Override
    protected void onMessage(Context ctx, Intent intent) {

        Log.d(TAG, "Message Received");

        String message = intent.getStringExtra("message");

        sendGCMIntent(ctx, message);

    }


    private void sendGCMIntent(Context ctx, String message) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("GCM_RECEIVED_ACTION");

        broadcastIntent.putExtra("gcm", message);

        ctx.sendBroadcast(broadcastIntent);

    }


    @Override
    protected void onRegistered(Context ctx, String regId) {
        // TODO Auto-generated method stub
        // send regId to your server
        Log.d(TAG, regId);

    }

    @Override
    protected void onUnregistered(Context ctx, String regId) {
        // TODO Auto-generated method stub
        // send notification to your server to remove that regId

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    */
}
