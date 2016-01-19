package edu.utk.biodynamics.icloudecg.DatabaseUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by DSClifford on 9/30/2015.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "records.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_RECORDS = "records";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RECID = "recordID";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DIAGNOSIS = "diagnosis";
    public static final String COLUMN_MAXHR = "maxhr";
    public static final String COLUMN_FLAGGED = "flagged";


    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_RECORDS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECID + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TIME + " TEXT, " +
                    COLUMN_DIAGNOSIS + " TEXT, " +
                    COLUMN_MAXHR + " NUMERIC, " +
                    COLUMN_FLAGGED + " TEXT " +
                    ")";



    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        Log.d("DB","Table Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        onCreate(db);
    }
}
