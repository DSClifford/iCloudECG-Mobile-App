package edu.utk.biodynamics.icloudecg.util;

/**
 * Created by DSClifford on 9/14/2015.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import edu.utk.biodynamics.icloudecg.AnalysisResults;
import edu.utk.biodynamics.icloudecg.R;

public class GCMListenerService extends GcmListenerService {


    public GCMListenerService() {

    }
Context ctx;
    @Override
    public void onMessageReceived(String from, Bundle extras) {
        sendNotification("Received GCM Message: " + extras.toString());
        ctx = this;
        String hrMessage = extras.getString("message");
        if (extras != null) {

            try
            {

                JSONObject json;
                json = new JSONObject().put("event", "message");

                // My application on my host server sends back to "EXTRAS" variables message and msgcnt
                // Depending on how you build your server app you can specify what variables you want to send

                json.put("message", extras.getString("message"));
                json.put("msgcnt", extras.getString("msgcnt"));
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.alert_icon);
                NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.alert_icon)
                        .setLargeIcon(bm)
                        .setContentTitle("ECG Abnormality Detected")
                        .setContentText("Touch for additional details")
                        .setAutoCancel(true)
                        .setVibrate(new long[] { 300, 500, 300, 500, 300, 500, 300, 500, 300, 500, 300, 500 });

                Intent resultIntent = new Intent(this, AnalysisResults.class)
                        .putExtra("message",hrMessage);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(AnalysisResults.class);
// Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                // Send the MESSAGE to the Javascript application
                final int notificationId = 1;
                NotificationManager nm = (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(notificationId, mBuilder.build());
            }
            catch( JSONException e)
            {

            }

        }

    }

    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId) {
        sendNotification("Upstream message sent. Id=" + msgId);
    }

    @Override
    public void onSendError(String msgId, String error) {
        sendNotification("Upstream message send error. Id=" + msgId + ", error" + error);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        Log.i("GCMLog",msg);
    }
}
