package edu.utk.biodynamics.icloudecg;

/**
 * Created by DSClifford on 8/8/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.utk.biodynamics.icloudecg.DatabaseUtils.DBOpenHelper;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewRecordsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewRecordsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewRecordsFragment extends Fragment {

    ArrayList<Record> arrayOfRecords = new ArrayList<Record>();

    SQLiteOpenHelper dbhelper;
    SQLiteDatabase database;
    Cursor cursor;
    ListView recListView;
    RecordCursorAdapter cursorAdapter = null;
    int count = 0;

    private static final String[] allColumns = {
            DBOpenHelper.COLUMN_ID,
            DBOpenHelper.COLUMN_RECID,
            DBOpenHelper.COLUMN_DATE,
            DBOpenHelper.COLUMN_TIME,
            DBOpenHelper.COLUMN_DIAGNOSIS,
            DBOpenHelper.COLUMN_MAXHR,
            DBOpenHelper.COLUMN_FLAGGED
    };

    private OnFragmentInteractionListener mListener;
    public static View thisView;

    public static ViewRecordsFragment newInstance() {
        ViewRecordsFragment fragment = new ViewRecordsFragment();
        return fragment;
    }

    public ViewRecordsFragment() {
        // Required empty public constructor
    }

    class Record {
        public String upldDate;
        public String recID;
        public String diagnosis;
    }

    public interface Callback {

        public void onItemSelected(String recordID, String upldDate, String diagnosis);
    }

    private class PopulateTable extends AsyncTask<RequestPackage, String, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(RequestPackage... params) {

            dbhelper = new DBOpenHelper(getActivity());
            database = dbhelper.getWritableDatabase();

            cursor = database.query(DBOpenHelper.TABLE_RECORDS, allColumns,
                    null, null, null, null, null, null);




            return null;
        }

        @Override
        protected void onPostExecute(final Void success) {

            cursorAdapter = new RecordCursorAdapter(getActivity(),cursor,0);

            if(cursor.getCount()==0){
                TextView noRec_tv = (TextView) thisView.findViewById(R.id.no_records_label);
                noRec_tv.setText(R.string.no_rec_text);
            }

            recListView = (ListView) thisView.findViewById(R.id.listView);

            String[] from = new String[]{
                    DBOpenHelper.COLUMN_DATE,
                    DBOpenHelper.COLUMN_TIME,
                    DBOpenHelper.COLUMN_DIAGNOSIS,
                    DBOpenHelper.COLUMN_MAXHR,
                    DBOpenHelper.COLUMN_RECID,
                    DBOpenHelper.COLUMN_FLAGGED
            };
            int[] to = new int[]{R.id.upldDate,R.id.upldTime,R.id.diagnosis,R.id.maxHR,R.id.recID,R.id.recFlagged};
            //SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,cursor,from,to);

            recListView.setAdapter(cursorAdapter);
            recListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    TextView recTV = (TextView)view.findViewById(R.id.recID);
                    TextView dateTV = (TextView)view.findViewById(R.id.upldDate);
                    TextView timeTV = (TextView)view.findViewById(R.id.upldTime);
                    TextView diagnosisTV = (TextView)view.findViewById(R.id.diagnosis);
                    String recordID = recTV.getText().toString();
                    String upldDate = dateTV.getText().toString()+" "+timeTV.getText().toString();
                    String diagnosis = diagnosisTV.getText().toString();

                    ((Callback) getActivity())
                            .onItemSelected(recordID, upldDate, diagnosis);
                    Log.d("recSelected", "AVID: " + Long.toString(parent.getSelectedItemId()));
                    Log.d("recSelected", "View: " + view.toString());
                    Log.d("recSelected", "UpldDate: " + upldDate);
                    Log.d("recSelected", "FID: " + recordID);

                }
            });

            recListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    return false;
                }
            });

            dbhelper.close();
        }
    }

    public class RecordCursorAdapter extends CursorAdapter {
        public RecordCursorAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, 0);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.row, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView recTV = (TextView)view.findViewById(R.id.recID);
            TextView dateTV = (TextView)view.findViewById(R.id.upldDate);
            TextView timeTV = (TextView)view.findViewById(R.id.upldTime);
            TextView maxHRTV = (TextView)view.findViewById(R.id.maxHR);
            TextView diagnosisTV = (TextView)view.findViewById(R.id.diagnosis);
            // Extract properties from cursor
            Boolean flagged = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.COLUMN_FLAGGED)));
            String recordID = cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.COLUMN_RECID));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.COLUMN_DATE));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.COLUMN_TIME));
            int maxHR = cursor.getInt(cursor.getColumnIndexOrThrow(DBOpenHelper.COLUMN_MAXHR));
            String diagnosis = cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.COLUMN_DIAGNOSIS));
            // Populate fields with extracted properties
            recTV.setText(recordID);
            dateTV.setText(date);
            timeTV.setText(time);

            if(cursor.getPosition()==0){
                diagnosis = "Atrial Fibrillation";
                count=1;
            }else if(cursor.getPosition()==1){
                flagged=true;
                diagnosis = "Normal";
            }else{
                diagnosis = "Normal";
            }
            diagnosisTV.setText(diagnosis);
            maxHRTV.setText(String.valueOf(maxHR));
            if(flagged){
                dateTV.setBackgroundColor(getResources().getColor(R.color.yellow));
                timeTV.setBackgroundColor(getResources().getColor(R.color.yellow));
                diagnosisTV.setBackgroundColor(getResources().getColor(R.color.yellow));
                maxHRTV.setBackgroundColor(getResources().getColor(R.color.yellow));
            }else if (diagnosis.contains("Normal")){
                dateTV.setBackgroundColor(getResources().getColor(R.color.green));
                timeTV.setBackgroundColor(getResources().getColor(R.color.green));
                diagnosisTV.setBackgroundColor(getResources().getColor(R.color.green));
                maxHRTV.setBackgroundColor(getResources().getColor(R.color.green));
            }else{
                dateTV.setBackgroundColor(getResources().getColor(R.color.recordviewred));
                timeTV.setBackgroundColor(getResources().getColor(R.color.recordviewred));
                diagnosisTV.setBackgroundColor(getResources().getColor(R.color.recordviewred));
                maxHRTV.setBackgroundColor(getResources().getColor(R.color.recordviewred));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_view_records, container, false);
        registerForContextMenu(thisView.findViewById(R.id.listView));
        return thisView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        if(view.getId()==R.id.listView){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i <menuItems.length ; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);


            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
       AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        int listItemIndex = info.position;
        Record selectedRecord = arrayOfRecords.get(listItemIndex);
        Log.d("Debuggy","DeleteRecord: "+selectedRecord.recID);

        return true;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("ViewECGFragment", "OnResume");
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.title_my_records);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String email = prefs.getString("email", "asd");

        RequestPackage p = new RequestPackage();
        p.setMethod("GET");
        p.setUri("http://biodynamics.engr.utk.edu/iCloudECGAppBridge/serverside.php");
        p.setParam("email", email);
        PopulateTable populateTable = new PopulateTable();
        populateTable.execute(p);
    }

}
