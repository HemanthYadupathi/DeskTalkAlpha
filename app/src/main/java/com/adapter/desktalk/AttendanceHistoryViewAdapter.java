package com.adapter.desktalk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activity.desktalk.R;
import com.model.desktalk.AttendanceDetailsModel;
import com.utils.desktalk.Constants;

import java.util.ArrayList;


public class AttendanceHistoryViewAdapter extends RecyclerView.Adapter<AttendanceHistoryViewAdapter.MyViewHolder> {

    private ArrayList<AttendanceDetailsModel> mDataset;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextViewStatus, mTextViewName, mTextViewRegNo, mTextViewPrecent;

        public MyViewHolder(View v) {
            super(v);
            mTextViewStatus = (TextView) v.findViewById(R.id.textAttendenceStatus);
            mTextViewName = (TextView) v.findViewById(R.id.listitem_textname);
            mTextViewRegNo = (TextView) v.findViewById(R.id.listitem_textregno);
            mTextViewPrecent = (TextView) v.findViewById(R.id.listitem_textpercen);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AttendanceHistoryViewAdapter(ArrayList<AttendanceDetailsModel> myDataset, Context myContextt) {
        mDataset = myDataset;
        mContext = myContextt;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AttendanceHistoryViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_layout_attendencehistory, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (mDataset.get(position).getStatus() != null && !mDataset.get(position).getStatus().contentEquals(""))
            if (mDataset.get(position).getStatus().contentEquals(Constants.ATTENDANCE_STATUS_ABSENT))
                holder.mTextViewStatus.setText(Constants.ABSENT);
            else if (mDataset.get(position).getStatus().contentEquals(Constants.ATTENDANCE_STATUS_PRESENT))
                holder.mTextViewStatus.setText(Constants.PRESENT);
            else
                holder.mTextViewStatus.setText(Constants.ATTENDANCE_UNKNOWN);
        else
            holder.mTextViewStatus.setText(Constants.ATTENDANCE_UNKNOWN);
        if (mDataset.get(position).getFname() != null && !mDataset.get(position).getFname().contentEquals("") && mDataset.get(position).getLname() != null && !mDataset.get(position).getLname().contentEquals(""))
            holder.mTextViewName.setText("Name : " + mDataset.get(position).getFname() + " " + mDataset.get(position).getLname());
        else
            holder.mTextViewName.setText("Name : " + " -");
        if (mDataset.get(position).getReference_id() != null && !mDataset.get(position).getReference_id().contentEquals(""))
            holder.mTextViewRegNo.setText("RegNO : " + mDataset.get(position).getReference_id());
        else
            holder.mTextViewRegNo.setText("RegNO : " + " -");
        holder.mTextViewPrecent.setText("Attendane(100%) : " + " -");
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
