package edu.utk.biodynamics.icloudecg.util;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by DSClifford on 9/14/2015.
 */
public class InstanceIDListener extends InstanceIDListenerService {
    /**
     * Called if InstanceID token is updated.
     * May occur if the security of the previous token had been compromised
     * and is initiated by the InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        startService(new Intent(this, RegistrationIntentService.class));
    }
}
