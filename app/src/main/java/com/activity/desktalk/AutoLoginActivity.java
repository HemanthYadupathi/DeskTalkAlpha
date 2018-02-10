package com.activity.desktalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import java.util.concurrent.CopyOnWriteArraySet;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AutoLoginActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private String TAG = AutoLoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_login);

        sharedpreferences = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE); //1
        editor = sharedpreferences.edit();

        String username = sharedpreferences.getString(Constants.PREFERENCE_KEY_USER_NAME, "");
        String pwd = sharedpreferences.getString(Constants.PREFERENCE_KEY_USER_PWD, "");

        if (Connectivity.isConnected(getApplicationContext())) {
            login(username, pwd);
        } else {
            Toast.makeText(AutoLoginActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            clearActivity();
        }

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
        final String android_id = Settings.Secure.getString(AutoLoginActivity.this.getContentResolver(),
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
                                    editor.putString(Constants.PREFERENCE_KEY_DEVICE_TOKEN, android_id);
                                    editor.commit();
                                    editor.apply();

                                    if (String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("4")) {
                                        Constants.USER_ID = Constants.USER_TEACHER;
                                    } else if (String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("6")) {
                                        Constants.USER_ID = Constants.USER_PARENT;
                                    } else if (String.valueOf(jsonObject.getJSONObject("response").get("role")).contentEquals("7")) {
                                        Constants.USER_ID = Constants.USER_ATTENDER;
                                    }
                                    Toast.makeText(AutoLoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                                    finish();
                                    if (Constants.USER_ID == Constants.USER_ATTENDER || Constants.USER_ID == Constants.USER_TEACHER) {
                                        Intent intent = new Intent(AutoLoginActivity.this, AttendanceMainActivity.class);
                                        startActivity(intent);
                                    } else if (Constants.USER_ID == Constants.USER_PARENT) {
                                        Intent intent = new Intent(AutoLoginActivity.this, BusTrackMapActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(AutoLoginActivity.this, "Sorry, App doesn't support for this User", Toast.LENGTH_SHORT).show();
                                    clearActivity();
                                }
                            } else {
                                Toast.makeText(AutoLoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                clearActivity();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            clearActivity();
                        }
                    } else {
                        Toast.makeText(AutoLoginActivity.this, "Error logging, please try again", Toast.LENGTH_SHORT).show();
                        clearActivity();
                    }
                } else if (response1.code() == 400) {
                    Toast.makeText(AutoLoginActivity.this, "Invalid username/password, please try again", Toast.LENGTH_SHORT).show();
                    clearActivity();
                } else if (response1.code() == 402) {
                    Toast.makeText(AutoLoginActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                    clearActivity();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(AutoLoginActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                clearActivity();
            }

        });
    }

    private void clearActivity() {
        finish();
        Intent intent = new Intent(AutoLoginActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
