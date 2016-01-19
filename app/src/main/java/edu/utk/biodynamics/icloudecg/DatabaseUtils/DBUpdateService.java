package edu.utk.biodynamics.icloudecg.DatabaseUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by DSClifford on 10/1/2015.
 */
public class DBUpdateService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    Context ctx = this;
    public DBUpdateService() {
        super("DBUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String recordID = intent.getStringExtra("recordID");
        double maxHR = intent.getDoubleExtra("maxHR", 0.0);
        Boolean flagged = intent.getBooleanExtra("flagged",false);
        Log.e("DBUpdate Flagged: ",String.valueOf(flagged));

        DBDataSource dataSource;
        dataSource = new DBDataSource(ctx);
        dataSource.open();
        dataSource.create(recordID, maxHR, flagged);
        dataSource.close();

    }
}
