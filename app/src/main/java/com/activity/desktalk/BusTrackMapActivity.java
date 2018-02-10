package com.activity.desktalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.adapter.desktalk.CustomInfoWindowAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class BusTrackMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private SharedPreferences mSharedPreferences;
    private Button mButtonReload;
    private TextView mTextViewLastUpdated;
    private static final String TAG = BusTrackMapActivity.class.getSimpleName();
    private Handler mHandler;
    private final long updateLocationInterval = 5000;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_track_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mButtonReload = (Button) findViewById(R.id.buttonReload);
        mTextViewLastUpdated = (TextView) findViewById(R.id.textLastUpdated);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
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
        final String token = mSharedPreferences.getString(Constants.PREFERENCE_KEY_TOKEN, "");

        if (Connectivity.isConnected(getApplicationContext())) {
            getUpdatedLocation(token);
        } else {
            Toast.makeText(BusTrackMapActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
        }

        mButtonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connectivity.isConnected(getApplicationContext())) {
                    getUpdatedLocation(token);
                } else {
                    Toast.makeText(BusTrackMapActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mHandler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Calling run");
                if (Connectivity.isConnected(getApplicationContext())) {
                    getUpdatedLocation(token);
                } else {
                    Toast.makeText(BusTrackMapActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
                if (mHandler != null)
                    mHandler.postDelayed(this, updateLocationInterval);
            }
        };
        mHandler.postDelayed(runnable, updateLocationInterval);
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
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
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
                    Constants.logout(BusTrackMapActivity.this, token);
                } else {
                    Log.d(TAG, getString(R.string.check_connection));
                }
            } else {
                Log.d(TAG, "Token null");
            }
            Constants.clearSharedPreferenceData(mSharedPreferences, TAG);
            finish();
            Intent intent = new Intent(BusTrackMapActivity.this, LoginActivity.class);
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
            startActivity(new Intent(BusTrackMapActivity.this, ProfileActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*LatLng defaultLoc = new LatLng(12.9319396, 77.613859);
        mMap.addMarker(new MarkerOptions().position(defaultLoc).title("Current bus location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));*/
    }

    private void getUpdatedLocation(String token) {
        Log.d(TAG, "getUpdatedLocation");
        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Apis mInterfaceService = retrofit.create(Apis.class);

        Call<JsonElement> mService = mInterfaceService.getStudentLocation(token);
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    Log.d(TAG, "getUpdatedLocation onResponse " + response.toString());
                    if (response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            if (String.valueOf(jsonObject.get("status").toString()).contentEquals("success")) {
                                JSONObject jsonObjectResponse = jsonObject.getJSONObject("response");
                                Log.e(TAG, response.toString());
                                mTextViewLastUpdated.setText("Last Updated : " + jsonObjectResponse.get("last_location_time").toString());
                                mMap.clear();

                                LatLng defaultLoc = new LatLng(Double.valueOf(jsonObjectResponse.get("lat").toString()), Double.valueOf(jsonObjectResponse.get("lng").toString()));
                                MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus)).position(defaultLoc).title("Current bus location\n").snippet(jsonObjectResponse.get("bus_no").toString() + "\n"
                                        + jsonObjectResponse.get("fname").toString() + " " + jsonObjectResponse.get("lname").toString() + "\n");

                                //Set Custom InfoWindow Adapter
                                CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(BusTrackMapActivity.this);
                                mMap.setInfoWindowAdapter(adapter);
                                mMap.addMarker(marker).showInfoWindow();
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                            }

                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage().toString());
                            LatLng defaultLoc = new LatLng(12.9319396, 77.613859);
                            mMap.addMarker(new MarkerOptions().position(defaultLoc).title("Current bus location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                        }
                    }

                } else if (response.code() == 404) {
                    Toast.makeText(BusTrackMapActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                    mHandler = null;
                    finish();
                    Constants.startLogin(BusTrackMapActivity.this);
                } else {
                    Toast.makeText(BusTrackMapActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                    LatLng defaultLoc = new LatLng(12.9319396, 77.613859);
                    mMap.addMarker(new MarkerOptions().position(defaultLoc).title("Current bus location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e(TAG, t.getMessage().toString());
                LatLng defaultLoc = new LatLng(12.9319396, 77.613859);
                mMap.addMarker(new MarkerOptions().position(defaultLoc).title("Current bus location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
            }
        });
    }
}
