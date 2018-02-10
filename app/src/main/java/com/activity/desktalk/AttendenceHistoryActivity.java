package com.activity.desktalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adapter.desktalk.AttendanceHistoryEditAdapter;
import com.adapter.desktalk.AttendanceHistoryViewAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.desktalk.AttendanceDetailsModel;
import com.model.desktalk.DatesModel;
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

public class AttendenceHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SharedPreferences mSharedPreferences;
    private String TAG = AttendenceHistoryActivity.class.getSimpleName();
    private ArrayList<String> dateList = new ArrayList<String>();
    private ArrayList<AttendanceDetailsModel> mArrayListAttendanceDetailsModels;
    private TextView mTextViewSelectedClass, mTextViewTeacher, mTextViewDate;
    private ProgressBar mProgressBar;
    private boolean isShowDetailsList = false;
    private boolean isFromView = false;
    private String selectedDate;

    public static String loginToken, selectedClassID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendence_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        mTextViewSelectedClass = (TextView) findViewById(R.id.textSelectedClass);
        mTextViewTeacher = (TextView) findViewById(R.id.textTeacherName);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE);

        String userData = mSharedPreferences.getString(Constants.PREFERENCE_KEY_USERDATA, "");
        if (!userData.contentEquals("") || userData != null) {
            try {
                JsonObject userDataObject = new JsonParser().parse(userData).getAsJsonObject();
                mTextViewTeacher.setText(userDataObject.get("fname").getAsString() + " " + userDataObject.get("lname").getAsString());

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "userData is null");
        }
        selectedClassID = getIntent().getStringExtra("classID");
        loginToken = mSharedPreferences.getString(Constants.PREFERENCE_KEY_TOKEN, "");
        mTextViewDate = (TextView) findViewById(R.id.date);
        mTextViewSelectedClass.setText(getIntent().getStringExtra("className"));
        mTextViewDate.setText(Constants.getDate());
        if (Connectivity.isConnected(getApplicationContext())) {
            showProgress(true);
            getHistoryDetails(loginToken, selectedClassID);
        } else {
            Toast.makeText(this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        if (isShowDetailsList) {
            isShowDetailsList = false;
            mTextViewDate.setText(Constants.getDate());
            AttendanceHistoryRecyclerViewAdapter adapter = new AttendanceHistoryRecyclerViewAdapter(dateList, AttendenceHistoryActivity.this);
            recyclerView.setAdapter(adapter);
        } else {
            //super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (isShowDetailsList) {
                isShowDetailsList = false;
                mTextViewDate.setText(Constants.getDate());
                AttendanceHistoryRecyclerViewAdapter adapter = new AttendanceHistoryRecyclerViewAdapter(dateList, AttendenceHistoryActivity.this);
                recyclerView.setAdapter(adapter);
            } else {
                //super.onBackPressed();
                finish();
            }
        }
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
                    showProgress(false);
                    if (response.body() != null) {
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
                                if (isFromView) {
                                    AttendanceHistoryViewAdapter adapter = new AttendanceHistoryViewAdapter(mArrayListAttendanceDetailsModels, getApplicationContext());
                                    recyclerView.setAdapter(adapter);
                                } else {
                                    AttendanceHistoryEditAdapter adapter = new AttendanceHistoryEditAdapter(mArrayListAttendanceDetailsModels, getApplicationContext());
                                    recyclerView.setAdapter(adapter);
                                }

                            } else {
                                Toast.makeText(AttendenceHistoryActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else if (response.code() == 404) {
                    showProgress(false);
                    Toast.makeText(AttendenceHistoryActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                    finish();
                    Constants.startLogin(AttendenceHistoryActivity.this);
                } else {
                    showProgress(false);
                    Toast.makeText(AttendenceHistoryActivity.this, "Something went wrong, please login again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                showProgress(false);
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private void showProgress(boolean status) {
        if (status) {
            mProgressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

    }

    private void getHistoryDetails(String token, String classID) {
        isShowDetailsList = false;
        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Apis mInterfaceService = retrofit.create(Apis.class);

        Call<JsonElement> mService = mInterfaceService.getHistoryDates(token, classID);
        final ArrayList<DatesModel> datesModels = new ArrayList<DatesModel>();
        mService.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    showProgress(false);
                    if (response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            if (String.valueOf(jsonObject.get("status").toString()).contentEquals("success")) {
                                JSONArray array = jsonObject.getJSONArray("response");
                                for (int i = 0; i < array.length(); i++) {
                                    Gson gson = new Gson();
                                    DatesModel classModel = gson.fromJson(array.getString(i), DatesModel.class);
                                    datesModels.add(classModel);
                                    dateList.add(datesModels.get(i).getCreated_date());
                                }
                                AttendanceHistoryRecyclerViewAdapter adapter = new AttendanceHistoryRecyclerViewAdapter(dateList, AttendenceHistoryActivity.this);
                                recyclerView.setAdapter(adapter);

                            } else {

                                Toast.makeText(AttendenceHistoryActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else if (response.code() == 404) {
                    showProgress(false);
                    Toast.makeText(AttendenceHistoryActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                } else {
                    showProgress(false);
                    Toast.makeText(AttendenceHistoryActivity.this, "Something went wrong, please login again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                showProgress(false);
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public class AttendanceHistoryRecyclerViewAdapter extends RecyclerView.Adapter<AttendanceHistoryRecyclerViewAdapter.MyViewHolder> {

        private ArrayList<String> mDataset;
        private Context mContext;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mLayout;
            private TextView mTextView;
            private ImageView mImageView;

            public MyViewHolder(View v) {
                super(v);

                mLayout = (LinearLayout) v.findViewById(R.id.layout_click);
                mTextView = (TextView) v.findViewById(R.id.list_item_text);
                mImageView = (ImageView) v.findViewById(R.id.list_item_imageview);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public AttendanceHistoryRecyclerViewAdapter(ArrayList<String> myDataset, Context myContextt) {
            mDataset = myDataset;
            mContext = myContextt;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public AttendanceHistoryRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                                    int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.mTextView.setText(mDataset.get(position));
            holder.mImageView.setBackground(mContext.getDrawable(R.mipmap.ic_edit_blue));
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Connectivity.isConnected(getApplicationContext())) {
                        selectedDate = mDataset.get(position);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        Log.d(TAG, selectedDate);
                        isShowDetailsList = true;
                        isFromView = true;
                        mTextViewDate.setText(selectedDate);
                        getAttendanceDetailsByDate(loginToken, selectedClassID, selectedDate);
                        showProgress(true);
                    } else {
                        Toast.makeText(AttendenceHistoryActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Connectivity.isConnected(getApplicationContext())) {
                        selectedDate = mDataset.get(position);
                        Log.d("edit", mDataset.get(position) + " Clicked_Download");
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        isShowDetailsList = true;
                        isFromView = false;
                        mTextViewDate.setText(selectedDate);
                        getAttendanceDetailsByDate(loginToken, selectedClassID, selectedDate);
                        showProgress(true);
                    } else {
                        Toast.makeText(AttendenceHistoryActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

}