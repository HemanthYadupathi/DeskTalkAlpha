package com.activity.desktalk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActivityChooserView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.desktalk.ProfileResponseModel;
import com.utils.desktalk.Apis;
import com.utils.desktalk.Connectivity;
import com.utils.desktalk.Constants;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences mSharedPreferences;
    private final String TAG = ProfileActivity.class.getSimpleName();
    private TextView mTextViewFname, mTextViewLname, mTextViewDescription, mTextViewFather, mTextViewMother,
            mTextViewDOB, mTextViewBGroup, mTextViewMobile, mTextViewAddress, mTextViewHobbies, mTextViewSkill;
    private Toolbar toolbar;
    private View mProfileForm;
    private View mProgressView;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        mSharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE);
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

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String token = mSharedPreferences.getString(Constants.PREFERENCE_KEY_TOKEN, "");
        if (!token.contentEquals("") || token != null) {

            if (Connectivity.isConnected(getApplicationContext())) {
                showProgress(true);
                getUserProfile(token);
            } else {
                Toast.makeText(ProfileActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "token null");
            Toast.makeText(ProfileActivity.this, "Something went wrong, please login again", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        mTextViewDescription = (TextView) findViewById(R.id.textDesc);
        mTextViewFather = (TextView) findViewById(R.id.prof_Fname);
        mTextViewMother = (TextView) findViewById(R.id.prof_Mname);
        mTextViewDOB = (TextView) findViewById(R.id.prof_DOB);
        mTextViewBGroup = (TextView) findViewById(R.id.prof_BG);
        mTextViewMobile = (TextView) findViewById(R.id.prof_Mnumber);
        mTextViewAddress = (TextView) findViewById(R.id.prof_Address);
        mTextViewHobbies = (TextView) findViewById(R.id.prof_Hobby);
        mTextViewSkill = (TextView) findViewById(R.id.prof_Skill);
        mProfileForm = findViewById(R.id.nestedscrollView);
        mProgressView = findViewById(R.id.login_progress);
        Menu menu = navigationView.getMenu();

        if (Constants.USER_ID == Constants.USER_ATTENDER || Constants.USER_ID == Constants.USER_TEACHER) {
            menu.add(R.id.main, Constants.NAV_MENU_ITEM_ATTENDENCE, 0, "Attendance").setIcon(getDrawable(R.mipmap.ic_attend)).setCheckable(true);
        } else if (Constants.USER_ID == Constants.USER_PARENT) {
            menu.add(R.id.main, Constants.NAV_MENU_ITEM_BUSTRACK, 0, "Bus Tracking").setIcon(getDrawable(R.mipmap.ic_bus)).setCheckable(true);
        }
        menu.add(R.id.main, Constants.NAV_MENU_ITEM_PROFILE, 0, "Profile Information").setIcon(getDrawable(R.mipmap.ic_person)).setCheckable(true);
        navigationView.invalidate();
        int id = Constants.NAV_MENU_ITEM_PROFILE;
        navigationView.setCheckedItem(id);
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
                    Constants.logout(ProfileActivity.this, token);
                } else {
                    Log.d(TAG, getString(R.string.check_connection));
                }
            } else {
                Log.d(TAG, "Token null");
            }
            Constants.clearSharedPreferenceData(mSharedPreferences, TAG);
            finish();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
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

        if (id == Constants.NAV_MENU_ITEM_ATTENDENCE || id == Constants.NAV_MENU_ITEM_BUSTRACK) {
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getUserProfile(String token) {
        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Apis mInterfaceService = retrofit.create(Apis.class);
        Call<JsonElement> mService = mInterfaceService.getUserProfile(token);
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {

                    if (response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            if (String.valueOf(jsonObject.get("status").toString()).contentEquals("success")) {
                                setProfileData(jsonObject.getJSONObject("response").toString());

                            } else {
                                showProgress(false);
                                Toast.makeText(ProfileActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else if (response.code() == 404) {
                    showProgress(false);
                    finish();
                    Constants.startLogin(ProfileActivity.this);
                    Toast.makeText(ProfileActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                } else {
                    showProgress(false);
                    Toast.makeText(ProfileActivity.this, "Something went wrong, please login again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private void setProfileData(String data) {
        Gson gson = new Gson();
        ProfileResponseModel userData = gson.fromJson(data, ProfileResponseModel.class);

        if (!userData.getFname().toString().contentEquals("") && !userData.getLname().toString().contentEquals("")) {
            toolbar.setTitle(userData.getFname().toString() + " " + userData.getLname().toString());
            setSupportActionBar(toolbar);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        if (!userData.getFather_name().toString().contentEquals("")) {
            mTextViewFather.setText(userData.getFather_name().toString());
        } else {
            mTextViewFather.setText(" -- ");
        }
        if (!userData.getMother_name().toString().contentEquals("")) {
            mTextViewMother.setText(userData.getMother_name().toString());
        } else {
            mTextViewMother.setText(" -- ");
        }
        if (!userData.getDate_of_birth().toString().contentEquals("")) {
            mTextViewDOB.setText(userData.getDate_of_birth().toString());
        } else {
            mTextViewDOB.setText(" -- ");
        }
        if (!userData.getBlood_group().toString().contentEquals("")) {
            mTextViewBGroup.setText(userData.getBlood_group().toString());
        } else {
            mTextViewBGroup.setText(" -- ");
        }
        if (!userData.getMobile().toString().contentEquals("")) {
            mTextViewMobile.setText(userData.getMobile().toString());
        } else {
            mTextViewMobile.setText(" -- ");
        }
        if (!userData.getAddress().toString().contentEquals("")) {
            mTextViewAddress.setText(userData.getAddress().toString());
        } else {
            mTextViewAddress.setText(" -- ");
        }
        if (!userData.getHobbies().toString().contentEquals("")) {
            mTextViewHobbies.setText(userData.getHobbies().toString().replace(",", "\n"));
        } else {
            mTextViewHobbies.setText(" -- ");
        }
        if (!userData.getSkills().toString().contentEquals("")) {
            mTextViewSkill.setText(userData.getSkills().toString().replace(",", "\n"));
        } else {
            mTextViewSkill.setText(" -- ");
        }
        if (!userData.getDescription().toString().contentEquals("")) {
            mTextViewDescription.setText(userData.getDescription().toString());
        } else {
            findViewById(R.id.card_view_description).setVisibility(View.GONE);
        }

        showProgress(false);
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

            mProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mProfileForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
