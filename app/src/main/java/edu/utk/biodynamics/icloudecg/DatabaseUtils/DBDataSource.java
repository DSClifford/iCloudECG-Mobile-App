package edu.utk.biodynamics.icloudecg.DatabaseUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.utk.biodynamics.icloudecg.R;

/**
 * Created by DSClifford on 9/30/2015.
 */
public class DBDataSource {

    SQLiteOpenHelper dbhelper;
    SQLiteDatabase database;

    private static final String[] allColumns = {
            DBOpenHelper.COLUMN_ID,
            DBOpenHelper.COLUMN_RECID,
            DBOpenHelper.COLUMN_DATE,
            DBOpenHelper.COLUMN_TIME,
            DBOpenHelper.COLUMN_DIAGNOSIS,
            DBOpenHelper.COLUMN_MAXHR,
            DBOpenHelper.COLUMN_FLAGGED
    };
    Context ctx;

    public DBDataSource(Context context){

        dbhelper = new DBOpenHelper(context);
        ctx = context;

    }

    public void open(){
        Log.d("DB","opened");
        database = dbhelper.getWritableDatabase();

    }

    public void close(){
        Log.d("DB","Closed");
        dbhelper.close();

    }

    public boolean create(String recordID, double maxHR, Boolean flagged) {

        int dbRowCount = getCount();
        int dbThreshold = (60*60*24)/30;

        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH));
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String date = year+"/"+month+"/"+day;

        int secInt = calendar.get(Calendar.SECOND);
        if(secInt >= 30){secInt = 30;}else{secInt=0;}
        String sec = String.format("%02d", secInt);
        String hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.format("%02d",calendar.get(Calendar.MINUTE));
        String time = hour+":"+minute+":"+sec;

        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COLUMN_RECID, recordID);
        values.put(DBOpenHelper.COLUMN_DATE, date);
        values.put(DBOpenHelper.COLUMN_TIME, time);
        values.put(DBOpenHelper.COLUMN_DIAGNOSIS, "Normal");
        values.put(DBOpenHelper.COLUMN_MAXHR, maxHR);
        values.put(DBOpenHelper.COLUMN_FLAGGED,String.valueOf(flagged));
        Log.e("DBDataSource Flagged: ",String.valueOf(flagged));

        if(dbRowCount<dbThreshold) {
            database.insert(DBOpenHelper.TABLE_RECORDS, null, values);
          //  String basepath = ctx.getFilesDir().getAbsolutePath();
          //  printRecordList(basepath);
        }else {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
            int dbInsertIndex = sharedPreferences.getInt(ctx.getString(R.string.dbInsertIndex), 1);
            deleteRecord(dbInsertIndex);

            long insertID = database.update(DBOpenHelper.TABLE_RECORDS, values, DBOpenHelper.COLUMN_ID + "=?", new String[] {String.valueOf(dbInsertIndex)});
            dbInsertIndex = dbInsertIndex+1;
            if(dbInsertIndex==dbThreshold+1){
                dbInsertIndex=1;
            }
            sharedPreferences.edit().putInt(ctx.getString(R.string.dbInsertIndex), dbInsertIndex).apply();
        }
            //findAll();

        return true;
    }

    private void deleteRecord(int dbInsertIndex) {

        String basepath = ctx.getFilesDir().getAbsolutePath();
        String where = DBOpenHelper.COLUMN_ID + " = \'" + dbInsertIndex + "\'";

        Cursor cursor = database.query(DBOpenHelper.TABLE_RECORDS, allColumns, where, null, null, null, null);
        cursor.moveToFirst();
        String recIDtoDelete = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COLUMN_RECID));

        File fileToDelete = new File(basepath+"/records/"+recIDtoDelete+".txt");
        fileToDelete.delete();

        //printRecordList(basepath);



    }

    private void printRecordList(String basepath) {
        File f = new File(basepath+"/records");
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++)
        {
            Log.e("DB", "Record Files: " + file[i].getName());
        }
    }

    public List<String> findAll(){
        List<String> times = new ArrayList<String>();

        Cursor cursor = database.query(DBOpenHelper.TABLE_RECORDS,allColumns,
                null,null,null,null,null,null);
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                Log.e("DB","ID _id: "+ cursor.getString(cursor.getColumnIndex(DBOpenHelper.COLUMN_RECID)));
            }
        }

        return null;
    }

    public int getCount() {
        List<String> times = new ArrayList<String>();

        Cursor cursor = database.query(DBOpenHelper.TABLE_RECORDS, allColumns,
                null, null, null, null, null, null);
        int count = cursor.getCount();
        return count;
    }
}
