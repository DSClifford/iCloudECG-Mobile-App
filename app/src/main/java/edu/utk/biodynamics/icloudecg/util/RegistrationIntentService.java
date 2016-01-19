package edu.utk.biodynamics.icloudecg.util;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import edu.utk.biodynamics.icloudecg.R;

/**
 * Created by DSClifford on 9/14/2015.
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG="RegistrationIntentService.class.getSimpleName()";
    //private static final String["RegistrationIntentService"] TOPICS = {"global"}

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            synchronized (TAG) {
                // Initially a network call, to retrieve the token, subsequent calls are local.
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                sharedPreferences.edit().putString(getString(R.string.gcm_token_keyname),token).apply();

                // TODO: send any registration to my app's servers, if applicable.
                // e.g. sendRegistrationToServer(token);

                // TODO: Subscribe to topic channels, if applicable.
                // e.g. for (String topic : TOPICS) {
                //          GcmPubSub pubSub = GcmPubSub.getInstance(this);
                //          pubSub.subscribe(token, "/topics/" + topic, null);
                //       }

                sharedPreferences.edit().putBoolean(getString(R.string.pref_key_SENT_TOKEN_TO_SERVER), true).apply();
            }
        } catch (Exception e) {
            Log.d("TAG", "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(getString(R.string.pref_key_SENT_TOKEN_TO_SERVER), false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(getString(R.string.intent_name_REGISTRATION_COMPLETE)));
    }
}
