package edu.utk.biodynamics.icloudecg;

/**
 * Created by DSClifford on 8/8/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ToggleButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewECGFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewECGFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewECGFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static ProgressBar pb;
    Button eventbtn;

    private OnFragmentInteractionListener mListener;
    public static View thisView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewECGFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewECGFragment newInstance(String param1, String param2) {
        ViewECGFragment fragment = new ViewECGFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewECGFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_view_ecg, container, false);
        final MainActivity mainActivity = (MainActivity) getActivity();
        thisView.findViewById(R.id.no_bh_popup).setVisibility(View.INVISIBLE);
        if(!mainActivity.connected) {
            thisView.findViewById(R.id.no_bh_popup).setVisibility(View.VISIBLE);
        }
        pb = (ProgressBar) thisView.findViewById(R.id.ecg_prog_bar);
        pb.setVisibility(View.INVISIBLE);

        final ToggleButton toggle = (ToggleButton) thisView.findViewById(R.id.pauseToggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    toggle.setTextColor(getResources().getColor(R.color.red));
                }else{
                    toggle.setTextColor(getResources().getColor(R.color.breaks));
                }
            }
        });
        eventbtn = (Button) thisView.findViewById(R.id.eventbtn);

        eventbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent flagIntent = new Intent(getActivity(), BHInterfaceService.class)
                        .setAction(((MainActivity) getActivity()).ACTION_RECORD_FLAGGED);
                getActivity().startService(flagIntent);


            }
        });

        return thisView;
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

    public static void setPBVis(int i) {
        if(i == 0){
            pb.setVisibility(View.VISIBLE);
        }else if (i==1){
            pb.setVisibility(View.INVISIBLE);
        }
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
        actionBar.setTitle(R.string.title_viewecg);
    }

    @Override
    public void onPause(){
        super.onResume();
        Log.d("View ECG", "OnPause");
    }

}
