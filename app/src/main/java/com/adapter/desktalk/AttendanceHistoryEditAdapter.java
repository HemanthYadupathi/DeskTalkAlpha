package com.adapter.desktalk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.desktalk.AttendenceHistoryActivity;
import com.activity.desktalk.R;
import com.model.desktalk.AttendanceDetailsModel;
import com.utils.desktalk.Connectivity;
import com.utils.desktalk.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AttendanceHistoryEditAdapter extends RecyclerView.Adapter<AttendanceHistoryEditAdapter.MyViewHolder> {

    private ArrayList<AttendanceDetailsModel> mDataset;
    private Map<String, String> mMapStatus = new HashMap<String, String>();
    private ArrayList<String> status = new ArrayList<String>();
    private final String TAG = AttendanceHistoryEditAdapter.class.getSimpleName();
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextViewStatus, mTextViewName, mTextViewRegNo, mTextViewPrecent;
        private Spinner mSpinnerStatus;

        public MyViewHolder(View v) {
            super(v);
            mTextViewStatus = (TextView) v.findViewById(R.id.listitem_textstatus);
            mTextViewName = (TextView) v.findViewById(R.id.listitem_textname);
            mTextViewRegNo = (TextView) v.findViewById(R.id.listitem_textregno);
            mTextViewPrecent = (TextView) v.findViewById(R.id.listitem_textpercen);
            mSpinnerStatus = (Spinner) v.findViewById(R.id.spinnerStatus);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AttendanceHistoryEditAdapter(ArrayList<AttendanceDetailsModel> myDataset, Context myContextt) {
        mDataset = myDataset;
        mContext = myContextt;
        for (int i = 0; i < mDataset.size(); i++) {
            mMapStatus.put(mDataset.get(i).getAttendance_take_id(), mDataset.get(i).getStatus());
        }
        status.add(Constants.ATTENDANCE_UNKNOWN);
        status.add(Constants.PRESENT);
        status.add(Constants.ABSENT);

    }

    // Create new views (invoked by the layout manager)
    @Override
    public AttendanceHistoryEditAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_layout_attendencehistory_edit, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.mSpinnerStatus.setSelection(status.indexOf(mMapStatus.get(mDataset.get(position).getAttendance_take_id())));
        if (mDataset.get(position).getStatus() != null && !mDataset.get(position).getStatus().contentEquals(""))
            if (mDataset.get(position).getStatus().contentEquals(Constants.ATTENDANCE_STATUS_ABSENT))
                holder.mTextViewStatus.setText("Attendance Status : " + Constants.ABSENT);
            else if (mDataset.get(position).getStatus().contentEquals(Constants.ATTENDANCE_STATUS_PRESENT))
                holder.mTextViewStatus.setText("Attendance Status : " + Constants.PRESENT);
            else
                holder.mTextViewStatus.setText("Attendance Status : " + Constants.ATTENDANCE_UNKNOWN);
        else
            holder.mTextViewStatus.setText("Attendance Status : " + Constants.ATTENDANCE_UNKNOWN);
        if (mDataset.get(position).getFname() != null && !mDataset.get(position).getFname().contentEquals("") && mDataset.get(position).getLname() != null && !mDataset.get(position).getLname().contentEquals(""))
            holder.mTextViewName.setText("Name : " + mDataset.get(position).getFname() + " " + mDataset.get(position).getLname());
        else
            holder.mTextViewName.setText("Name : " + " -");
        if (mDataset.get(position).getReference_id() != null && !mDataset.get(position).getReference_id().contentEquals(""))
            holder.mTextViewRegNo.setText("RegNO : " + mDataset.get(position).getReference_id());
        else
            holder.mTextViewRegNo.setText("RegNO : " + " -");
        holder.mTextViewPrecent.setText("Attendane(100%) : " + " -");

        holder.mSpinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mMapStatus.put(mDataset.get(position).getAttendance_take_id(), status.get(i));
                if (!status.get(i).contentEquals(Constants.ATTENDANCE_UNKNOWN)) {
                    if (Connectivity.isConnected(mContext)) {

                        Map<String, String> map = new HashMap<String, String>();
                        map.put("attendance_take_id", mDataset.get(position).getAttendance_take_id());
                        if (status.get(i).equalsIgnoreCase(Constants.PRESENT)) {
                            map.put("status", Constants.ATTENDANCE_STATUS_PRESENT);
                        } else if (status.get(i).equalsIgnoreCase(Constants.ABSENT)) {
                            map.put("status", Constants.ATTENDANCE_STATUS_ABSENT);
                        }

                        Log.d(TAG, "Mark attendance" + map.toString());
                        Constants.markStudentAttendance(TAG, AttendenceHistoryActivity.loginToken, map);
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Attendance selected Unknown");
                }
                notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
