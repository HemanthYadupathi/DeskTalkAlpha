package com.utils.desktalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.activity.desktalk.LoginActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by User on 10/10/2017.
 */

public class Constants {


    public static final int USER_TEACHER = 0;
    public static final int USER_STUDENT = 1;
    public static final int USER_PARENT = 2;
    public static final int USER_ATTENDER = 3;


    public static int USER_ID;

    public static final String BASE_URL = "https://desktalk.in/api/index.php/";

    public static final String PREFERENCE_LOGIN_DETAILS = "LOGIN_DETAILS";
    public static final String PREFERENCE_KEY_USERDATA = "USER_DATA";
    public static final String PREFERENCE_KEY_USER_ROLE = "USER_ROLE";

    public static final String PREFERENCE_KEY_USER_NAME = "USER_NAME";
    public static final String PREFERENCE_KEY_USER_PWD = "USER_PWD";
    public static final String PREFERENCE_KEY_DEVICE_TOKEN = "DEVICE_TOKEN";

    public static final String PREFERENCE_KEY_TOKEN = "TOKEN";

    public static final String PRESENT = "Present";
    public static final String ABSENT = "Absent";
    public static final String ATTENDANCE_UNKNOWN = "Unknown";
    public static final String ATTENDANCE_STATUS_UNKNOWN = "0";
    public static final String ATTENDANCE_STATUS_PRESENT = "1";
    public static final String ATTENDANCE_STATUS_ABSENT = "2";

    public static final int NAV_MENU_ITEM_ATTENDENCE = 0;
    public static final int NAV_MENU_ITEM_PROFILE = 1;
    public static final int NAV_MENU_ITEM_BUSTRACK = 2;

    public static void logout(final Context classname, String token) {
        final String TAG = "Logout constants";

        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Apis mInterfaceService = retrofit.create(Apis.class);
        Call<JsonElement> mService = mInterfaceService.Logout(token);
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {

                    if (response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            if (String.valueOf(jsonObject.get("status").toString()).contentEquals("success")) {
                                Log.d(TAG, "Logout success");

                            } else {
                                Log.d(TAG, "Logout failed");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else if (response.code() == 400) {
                    Log.e(TAG, "Logout failed error code 400");
                } else {
                    Log.e(TAG, "Logout failed error " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public static String getDate() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static void markStudentAttendance(final String TAG, String token, Map<String, String> mapData) {

        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Apis mInterfaceService = retrofit.create(Apis.class);
        Call<JsonElement> mService = mInterfaceService.markAttedance(token, mapData);
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        try {
                            Log.d(TAG, response.body().toString());
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            Log.d(TAG, String.valueOf(jsonObject.get("message")));
                            /*if (String.valueOf(jsonObject.get("status")).contentEquals("success")) {
                                Log.d(TAG, "Attendance marked success");
                            } else {
                                Log.d(TAG, "Attendance marked failed");
                            }*/
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                } else {
                    Log.d(TAG, String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d(TAG, t.getMessage().toString());
            }
        });
    }

    public static void clearSharedPreferenceData(SharedPreferences preferences, String TAG) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        editor.apply();
        Log.i(TAG, "Preference cleared");
    }

    public static void startLogin(Context mContext){
        Intent intent = new Intent(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }
}
