package com.utils.desktalk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.desktalk.R;
import com.adapter.desktalk.CardAdapter;
import com.adapter.desktalk.MarkAttendanceStudentAdapter;
import com.model.desktalk.AttendanceDetailsModel;

import java.util.HashMap;
import java.util.Map;


public class StudentInfoCardFragment extends Fragment {

    private CardView cardView, mCardViewImage;
    private ImageView mButtonRight, mButtonWrong;
    private TextView mTextViewValue, mTextViewName, mTextViewRegno, mTextViewAttend;
    private LinearLayout mLayoutButtons;
    private AttendanceDetailsModel modelData;
    private final String TAG = StudentInfoCardFragment.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private String loginToken;
    //private Map<String, String> mMapStatus = new HashMap<String, String>();
    int pos;


    public static Fragment getInstance(int position, AttendanceDetailsModel model) {
        StudentInfoCardFragment f = new StudentInfoCardFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("name", model.getFname() + " " + model.getLname());
        args.putString("regno", model.getReference_id());
        args.putString("attend", model.getAttendance_take_id());
        args.putString("status", model.getStatus());
        f.setArguments(args);

        return f;
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_student_attendance, container, false);

        cardView = (CardView) view.findViewById(R.id.cardView);
        mCardViewImage = (CardView) view.findViewById(R.id.view2);
        mTextViewValue = (TextView) view.findViewById(R.id.text_attendance_value);
        mTextViewName = (TextView) view.findViewById(R.id.name);
        mTextViewRegno = (TextView) view.findViewById(R.id.regno);
        mTextViewAttend = (TextView) view.findViewById(R.id.atten);
        mLayoutButtons = (LinearLayout) view.findViewById(R.id.layout_btn);

        mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE);
        loginToken = mSharedPreferences.getString(Constants.PREFERENCE_KEY_TOKEN, "");

        cardView.setMaxCardElevation(cardView.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);
        mCardViewImage.setMaxCardElevation(mCardViewImage.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);
        pos = getArguments().getInt("position");
        mTextViewName.setText(getArguments().getString("name"));
        mTextViewRegno.setText(getArguments().getString("regno"));
        mTextViewAttend.setText(getArguments().getString("attend"));
        if (getArguments().getString("status").contentEquals(Constants.ATTENDANCE_STATUS_PRESENT)) {
            setLayout(mTextViewValue, Constants.PRESENT, getResources().getColor(R.color.colorGreenPresent));
            mLayoutButtons.setVisibility(View.GONE);
        } else if (getArguments().getString("status").contentEquals(Constants.ATTENDANCE_STATUS_ABSENT)) {
            setLayout(mTextViewValue, Constants.ABSENT, getResources().getColor(R.color.colorRedAbsent));
            mLayoutButtons.setVisibility(View.GONE);
        }

        mButtonRight = (ImageView) view.findViewById(R.id.btn_present);
        mButtonWrong = (ImageView) view.findViewById(R.id.btn_absent);

        mButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connectivity.isConnected(getActivity())) {
                    doCircularAnimationSetText(mTextViewValue, Constants.PRESENT, getResources().getColor(R.color.colorGreenPresent));
                    mLayoutButtons.setVisibility(View.GONE);
                    //mMapStatus.put(getArguments().getString("attend"), Constants.ATTENDANCE_STATUS_PRESENT);
                    MarkAttendanceStudentAdapter.modelArrayList.get(pos).setStatus(Constants.ATTENDANCE_STATUS_PRESENT);
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("attendance_take_id", getArguments().getString("attend"));
                    map.put("status", Constants.ATTENDANCE_STATUS_PRESENT);
                    map.put("remarks", "");
                    Constants.markStudentAttendance(TAG, loginToken, map);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mButtonWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connectivity.isConnected(getActivity())) {
                    doCircularAnimationSetText(mTextViewValue, Constants.ABSENT, getResources().getColor(R.color.colorRedAbsent));
                    MarkAttendanceStudentAdapter.modelArrayList.get(pos).setStatus(Constants.ATTENDANCE_STATUS_ABSENT);
                    mLayoutButtons.setVisibility(View.GONE);
                    //mMapStatus.put(getArguments().getString("attend"), Constants.ATTENDANCE_STATUS_ABSENT);
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("attendance_take_id", getArguments().getString("attend"));
                    map.put("status", Constants.ATTENDANCE_STATUS_ABSENT);
                    map.put("remarks", "");
                    Constants.markStudentAttendance(TAG, loginToken, map);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private void setLayout(TextView view, String status, int color) {
        view.setText(status);
        view.setBackgroundColor(color);
        view.setVisibility(View.VISIBLE);
    }

    private void doCircularAnimationSetText(TextView view, String value, int color) {

        // get the center for the clipping circle
        int centerX = (view.getLeft() + view.getRight()) / 2;
        int centerY = (view.getTop() + view.getBottom()) / 2;

        int startRadius = 0;
        // get the final radius for the clipping circle
        int endRadius = Math.max(view.getWidth(), view.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view,
                        centerX, centerY, startRadius, endRadius);
        anim.setDuration(1000);
        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        view.setText(value);
        view.setBackgroundColor(color);
        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    public CardView getCardView() {
        return cardView;
    }

   /* private void markStudentAttendance(String token, Map<String, String> mapData) {

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
                            if (String.valueOf(jsonObject.get("status")).contentEquals("success")) {

                            }
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
    }*/
}
