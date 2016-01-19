package edu.utk.biodynamics.icloudecg.LoginRegister;

/**
 * Created by DSClifford on 9/18/2015.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import edu.utk.biodynamics.icloudecg.DatabaseUtils.HttpManager;
import edu.utk.biodynamics.icloudecg.MainActivity;
import edu.utk.biodynamics.icloudecg.R;
import edu.utk.biodynamics.icloudecg.RandomID;
import edu.utk.biodynamics.icloudecg.RequestPackage;

public class RegisterTab extends Fragment {
    private MyTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mFNameView;
    private AutoCompleteTextView mLNameView;
    private DatePicker mBDateView;
    private RadioGroup mGenderView;
    private RadioButton selectedGender;
    private EditText mPasswordView;
    private EditText mPasswordVerView;
    private View mProgressView;
    private View mLoginFormView;
    private String uri = "http://biodynamics.engr.utk.edu/iCloudECGAppBridge/registerUser.php";
    String fname;
    String lname;
    int bday;
    int bmonth;
    int byear;
    String gender;
    String email;
    String password;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v =inflater.inflate(R.layout.register_tab,container,false);

        // Set up the registration form.
        mFNameView = (AutoCompleteTextView) v.findViewById(R.id.fname);
        mLNameView = (AutoCompleteTextView) v.findViewById(R.id.lname);
        mBDateView = (DatePicker) v.findViewById(R.id.datePicker);
        mGenderView = (RadioGroup) v.findViewById(R.id.genderchoice);
        mEmailView = (AutoCompleteTextView) v.findViewById(R.id.email);
        mPasswordView = (EditText) v.findViewById(R.id.password);
        mPasswordVerView = (EditText) v.findViewById(R.id.password_confirm);

        Button registerButton = (Button) v.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedgenderid = mGenderView.getCheckedRadioButtonId();
                selectedGender = (RadioButton) v.findViewById(selectedgenderid);
                attemptRegister();
            }
        });

        mLoginFormView = v.findViewById(R.id.login_form);
        mProgressView = v.findViewById(R.id.login_progress);
        return v;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mFNameView.setError(null);
        mLNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordVerView.setError(null);


        // Store values at the time of the login attempt.
        fname = mFNameView.getText().toString();
        lname = mLNameView.getText().toString();
        bday = mBDateView.getDayOfMonth();
        bmonth = mBDateView.getMonth()+1;
        byear = mBDateView.getYear();
        gender = selectedGender.getText().toString();
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!mPasswordView.getText().toString().equals(mPasswordVerView.getText().toString())){
            mPasswordVerView.setError(getString(R.string.password_no_match_error));
            focusView = mPasswordVerView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            updateUserPrefs();
            RequestPackage p = requestData(uri);
            mAuthTask = new MyTask();
            mAuthTask.execute(p);
        }
    }

    private void updateUserPrefs() {

        String bdate = Integer.toString(bmonth)+"/"+Integer.toString(bday)+"/"+Integer.toString(byear);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.edit().putString("first_name",fname).apply();
        sharedPreferences.edit().putString("last_name",lname).apply();
        sharedPreferences.edit().putString("gender",gender).apply();
        sharedPreferences.edit().putInt("bday", bday).apply();
        sharedPreferences.edit().putInt("bmonth",bmonth).apply();
        sharedPreferences.edit().putInt("byear",byear).apply();
        sharedPreferences.edit().putString("bdate",bdate).apply();
        sharedPreferences.edit().putString("email",email).apply();
        sharedPreferences.edit().putString("password",password).apply();
        sharedPreferences.edit().putBoolean("wifi_only",true);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;//email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;//password.length() > 4;
    }

    private RequestPackage requestData(String uri) {

        String bdate = byear+"-"+bmonth+"-"+bday;
        String userid = RandomID.generateString();
        RequestPackage p = new RequestPackage();
        p.setMethod("GET");
        p.setUri(uri);
        p.setParam("email", email);
        p.setParam("pass", password);
        p.setParam("fname", fname);
        p.setParam("lname", lname);
        p.setParam("bdate", bdate);
        p.setParam("gender", gender);
        p.setParam("userid", userid);

        return p;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */

    private class MyTask extends AsyncTask<RequestPackage, String, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(RequestPackage... params) {
            Boolean success = false;
            String content = HttpManager.getData(params[0]);
            Log.d("HttpOutput", content.toString());
            if (content != null) {
                if(content.equals("1\n")){
                    success = true;
                }else{
                    success = false;
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Log.d("Login", "Success");
                goToMain();
                getActivity().finish();
            } else {
                Log.d("Login","Fail");
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
    public void goToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }
}
