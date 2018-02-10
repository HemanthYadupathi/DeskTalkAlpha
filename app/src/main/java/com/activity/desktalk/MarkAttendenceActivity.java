package com.activity.desktalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.adapter.desktalk.MarkAttendanceStudentAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.model.desktalk.AttendanceDetailsModel;
import com.utils.desktalk.Apis;
import com.utils.desktalk.Connectivity;
import com.utils.desktalk.Constants;
import com.utils.desktalk.ShadowTransformer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MarkAttendenceActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private final String TAG = MarkAttendenceActivity.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private String loginToken, selectedClassID, date;
    private TextView mTextViewDate;
    private ArrayList<AttendanceDetailsModel> mArrayListAttendanceDetailsModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendence);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextViewDate = (TextView) findViewById(R.id.date);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        mSharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE);
        loginToken = mSharedPreferences.getString(Constants.PREFERENCE_KEY_TOKEN, "");
        selectedClassID = getIntent().getStringExtra("classID");
        date = Constants.getDate();
        mTextViewDate.setText(date);
        if (Connectivity.isConnected(getApplicationContext())) {
            getAttendanceDetailsByDate(loginToken, selectedClassID, date);
        } else {
            Toast.makeText(MarkAttendenceActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
        }
    }

    public static float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void getAttendanceDetailsByDate(String token, String classID, String date) {
        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Apis mInterfaceService = retrofit.create(Apis.class);

        Call<JsonElement> mService = mInterfaceService.getAttendanceDetailsbyDate(token, classID, date);
        mArrayListAttendanceDetailsModels = new ArrayList<AttendanceDetailsModel>();
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    //showProgress(false);
                    if (response.body() != null) {
                        Log.d(TAG, String.valueOf(response));
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            if (String.valueOf(jsonObject.get("status").toString()).contentEquals("success")) {
                                JSONObject responseObject = jsonObject.getJSONObject("response");
                                JSONArray array = responseObject.getJSONArray("attendance");
                                for (int i = 0; i < array.length(); i++) {
                                    Gson gson = new Gson();
                                    AttendanceDetailsModel detailsModel = gson.fromJson(array.getString(i), AttendanceDetailsModel.class);
                                    mArrayListAttendanceDetailsModels.add(detailsModel);
                                }

                                FragmentManager fragmentManager = getSupportFragmentManager();
                                MarkAttendanceStudentAdapter pagerAdapter = new MarkAttendanceStudentAdapter(fragmentManager, dpToPixels(2, MarkAttendenceActivity.this), mArrayListAttendanceDetailsModels);
                                ShadowTransformer fragmentCardShadowTransformer = new ShadowTransformer(viewPager,pagerAdapter);
                                fragmentCardShadowTransformer.enableScaling(true);

                                viewPager.setAdapter(pagerAdapter);
                                viewPager.setPageTransformer(false, fragmentCardShadowTransformer);
                                viewPager.setOffscreenPageLimit(3);

                            } else {
                                Toast.makeText(MarkAttendenceActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else if (response.code() == 404) {
                    //showProgress(false);
                    Toast.makeText(MarkAttendenceActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                    finish();
                    Constants.startLogin(MarkAttendenceActivity.this);
                } else {
                    //showProgress(false);
                    Toast.makeText(MarkAttendenceActivity.this, "Something went wrong, please login again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                //showProgress(false);
                Log.e(TAG, t.getMessage());
            }
        });
    }
}
