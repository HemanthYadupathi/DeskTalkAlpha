package com.activity.desktalk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.utils.desktalk.Apis;
import com.utils.desktalk.Connectivity;
import com.utils.desktalk.Constants;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    private SharedPreferences sharedpreferences;
    private Editor editor;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mTextViewForgotPwd;
    private final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.e(TAG, Constants.getDate());
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mTextViewForgotPwd = (TextView) findViewById(R.id.textForgotPwd);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        sharedpreferences = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE); //1
        editor = sharedpreferences.edit();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (id == EditorInfo.IME_ACTION_DONE)) {
                    if (Connectivity.isConnected(getApplicationContext())) {
                        showProgress(true);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                        login(mEmailView.getText().toString(), mPasswordView.getText().toString());
                    } else {
                        Snackbar.make(textView, getString(R.string.check_connection), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean cancel = false;
                View focusView = null;

                if (!TextUtils.isEmpty(mEmailView.getText().toString()) && !TextUtils.isEmpty(mPasswordView.getText().toString())) {
                    if (Connectivity.isConnected(getApplicationContext())) {
                        showProgress(true);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        login(mEmailView.getText().toString(), mPasswordView.getText().toString());
                    } else {
                        Snackbar.make(view, getString(R.string.check_connection), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    if (TextUtils.isEmpty(mEmailView.getText().toString())) {
                        mEmailView.setError(getString(R.string.error_field_required));
                        focusView = mEmailView;
                        cancel = true;
                    } else if (TextUtils.isEmpty(mPasswordView.getText().toString())) {
                        mPasswordView.setError(getString(R.string.error_field_required));
                        focusView = mPasswordView;
                        cancel = true;
                    }
                    if (cancel) {
                        focusView.requestFocus();
                    }
                }

            }
        });

        mTextViewForgotPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                /*final AlertDialog.Builder pwdDialog = new AlertDialog.Builder(LoginActivity.this);
                View dialodView = getLayoutInflater().inflate(R.layout.dialog_forgotpwd, null);
                pwdDialog.setView(dialodView);
                pwdDialog.setCancelable(false);
                final AlertDialog alertDialog = pwdDialog.create();
                alertDialog.show();
                EditText mEditTextSchoolName = (EditText) dialodView.findViewById(R.id.schoolName);
                EditText mEditTextUserName = (EditText) dialodView.findViewById(R.id.userName);
                Button mButtonContinue = (Button) dialodView.findViewById(R.id.btn_send);
                Button mButtonCancel = (Button) dialodView.findViewById(R.id.btn_cancel);

                mButtonContinue.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(LoginActivity.this, "Button continue", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });

                mButtonCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(LoginActivity.this, "Button cancel", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });
*/
            }
        });

    }

    private void login(final String user, final String password) {

        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        final String android_id = Settings.Secure.getString(LoginActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Map<String, String> requestBodyMap = new HashMap<>();

        requestBodyMap.put("username", String.valueOf(user));
        requestBodyMap.put("password", String.valueOf(password));
        requestBodyMap.put("devicetoken", android_id);

        Apis mInterfaceService = retrofit.create(Apis.class);
        Call<JsonElement> mService = mInterfaceService.Authenticate(requestBodyMap);
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response1) {
                showProgress(false);
                if (response1.code() == 200) {
                    if (response1.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response1.body().toString());
                            if (String.valueOf(jsonObject.get("status")).contentEquals("success")) {
                                if (String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("4")
                                        || String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("6")
                                        || String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("7")) {

                                    editor = sharedpreferences.edit();
                                    editor.putString(Constants.PREFERENCE_KEY_USERDATA, String.valueOf(jsonObject.getJSONObject("response")));
                                    editor.putString(Constants.PREFERENCE_KEY_TOKEN, String.valueOf(jsonObject.getJSONObject("response").get("token")));
                                    editor.putString(Constants.PREFERENCE_KEY_USER_NAME, user);
                                    editor.putString(Constants.PREFERENCE_KEY_USER_PWD, password);
                                    editor.putString(Constants.PREFERENCE_KEY_DEVICE_TOKEN,android_id );
                                    editor.commit();
                                    editor.apply();

                                    if (String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("4")) {
                                        Constants.USER_ID = Constants.USER_TEACHER;
                                    } else if (String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("6")) {
                                        Constants.USER_ID = Constants.USER_PARENT;
                                    } else if (String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("7")) {
                                        Constants.USER_ID = Constants.USER_ATTENDER;
                                    }
                                    Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                                    finish();
                                    if (Constants.USER_ID == Constants.USER_ATTENDER || Constants.USER_ID == Constants.USER_TEACHER) {
                                        Intent intent = new Intent(LoginActivity.this, AttendanceMainActivity.class);
                                        startActivity(intent);
                                    } else if (Constants.USER_ID == Constants.USER_PARENT) {
                                        Intent intent = new Intent(LoginActivity.this, BusTrackMapActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Sorry, App doesn't support for this User", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                showProgress(false);
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            showProgress(false);
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error logging, please try again", Toast.LENGTH_SHORT).show();
                    }
                } else if (response1.code() == 400) {
                    Toast.makeText(LoginActivity.this, "Invalid username/password, please try again", Toast.LENGTH_SHORT).show();
                } else if (response1.code() == 402) {
                    Toast.makeText(LoginActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

