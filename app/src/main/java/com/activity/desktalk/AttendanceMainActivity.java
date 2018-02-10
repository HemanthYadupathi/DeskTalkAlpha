package com.activity.desktalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.desktalk.ClassDetailsModel;
import com.utils.desktalk.Apis;
import com.utils.desktalk.Connectivity;
import com.utils.desktalk.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AttendanceMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private Button mButtonMark, mButtonHistory;
    private SharedPreferences mSharedPreferences;
    private static final String TAG = AttendanceMainActivity.class.getSimpleName();
    private Spinner mSpinnerClass;
    private ArrayList<ClassDetailsModel> modelArrayList = new ArrayList<ClassDetailsModel>();
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mButtonMark = (Button) findViewById(R.id.button_markAtt);
        mButtonHistory = (Button) findViewById(R.id.button_attendanceHistory);
        mSpinnerClass = (Spinner) findViewById(R.id.spinner_class);

        mButtonHistory.setOnClickListener(this);
        mButtonMark.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);

        TextView nav_user = (TextView) hView.findViewById(R.id.text_name);
        mSharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE);
        String userData = mSharedPreferences.getString(Constants.PREFERENCE_KEY_USERDATA, "");
        if (!userData.contentEquals("") || userData != null) {
            try {
                JsonObject userDataObject = new JsonParser().parse(userData).getAsJsonObject();
                nav_user.setText(userDataObject.get("fname").getAsString() + " " + userDataObject.get("lname").getAsString());

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "userData is null");
        }

        String token = mSharedPreferences.getString(Constants.PREFERENCE_KEY_TOKEN, "");
        String UID = null;
        if (!userData.contentEquals("") || userData != null) {
            try {
                JsonObject userDataObject = new JsonParser().parse(userData).getAsJsonObject();
                UID = userDataObject.get("user_id").getAsString();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        if ((!token.contentEquals("") || token != null) && (!UID.contentEquals("") || UID != null)) {

            if (Connectivity.isConnected(getApplicationContext())) {
                getClassDetails(token, UID);
            } else {
                Toast.makeText(AttendanceMainActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "token null");
            Toast.makeText(AttendanceMainActivity.this, "Something went wrong, please login again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.attendance_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            String token = mSharedPreferences.getString(Constants.PREFERENCE_KEY_TOKEN, "");
            if (!token.contentEquals("") || token != null) {
                if (Connectivity.isConnected(getApplicationContext())) {
                    Constants.logout(AttendanceMainActivity.this, token);
                } else {
                    Log.d(TAG, getString(R.string.check_connection));
                }
            } else {
                Log.d(TAG, "Token null");
            }

            Constants.clearSharedPreferenceData(mSharedPreferences, TAG);
            finish();
            Intent intent = new Intent(AttendanceMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            startActivity(new Intent(AttendanceMainActivity.this, ProfileActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        String classID = null, classname = null;
        if (modelArrayList.size() != 0) {
            classID = modelArrayList.get(mSpinnerClass.getSelectedItemPosition()).getClass_id();
            classname = modelArrayList.get(mSpinnerClass.getSelectedItemPosition()).getClass_name() + " " + modelArrayList.get(mSpinnerClass.getSelectedItemPosition()).getSection();
        }
        switch (view.getId()) {
            case R.id.button_attendanceHistory:
                if (classID != null) {
                    Log.d(TAG, "Passing Class ID: " + classID);
                    Intent intent = new Intent(AttendanceMainActivity.this, AttendenceHistoryActivity.class);
                    intent.putExtra("classID", classID);
                    intent.putExtra("className", classname);
                    startActivity(intent);
                }
                break;
            case R.id.button_markAtt:
                if (classID != null) {
                    Log.d(TAG, "Passing Class ID: " + classID);
                    Intent intent = new Intent(AttendanceMainActivity.this, MarkAttendenceActivity.class);
                    intent.putExtra("classID", classID);
                    intent.putExtra("className", classname);
                    startActivity(intent);
                }
                break;
        }
    }

    private void getClassDetails(String token, String UID) {
        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Apis mInterfaceService = retrofit.create(Apis.class);

        Call<JsonElement> mService = mInterfaceService.getClassDetails(token, UID);

        final ArrayList<String> classes = new ArrayList<String>();
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {

                    if (response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            if (String.valueOf(jsonObject.get("status").toString()).contentEquals("success")) {
                                JSONArray array = jsonObject.getJSONArray("response");
                                for (int i = 0; i < array.length(); i++) {
                                    Gson gson = new Gson();
                                    ClassDetailsModel classModel = gson.fromJson(array.getString(i), ClassDetailsModel.class);
                                    modelArrayList.add(classModel);
                                    classes.add(modelArrayList.get(i).getClass_name() + " " + modelArrayList.get(i).getSection());
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AttendanceMainActivity.this, android.R.layout.simple_spinner_dropdown_item, classes);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                                mSpinnerClass.setAdapter(spinnerArrayAdapter);
                                Log.d(TAG, "Class : " + classes.toString());
                            } else {
                                //showProgress(false);
                                Toast.makeText(AttendanceMainActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else if (response.code() == 404) {
                    Toast.makeText(AttendanceMainActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                    finish();
                    Constants.startLogin(AttendanceMainActivity.this);
                } else {
                    //showProgress(false);
                    Toast.makeText(AttendanceMainActivity.this, "Something went wrong, please login again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }
}
