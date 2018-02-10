package com.adapter.desktalk;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.CardView;
import android.view.ViewGroup;

import com.model.desktalk.AttendanceDetailsModel;
import com.utils.desktalk.StudentInfoCardFragment;

import java.util.ArrayList;
import java.util.List;

public class MarkAttendanceStudentAdapter extends FragmentStatePagerAdapter implements CardAdapter {

    private List<StudentInfoCardFragment> fragments;
    public static ArrayList<AttendanceDetailsModel> modelArrayList;
    private float baseElevation;

    public MarkAttendanceStudentAdapter(FragmentManager fm, float baseElevation, ArrayList<AttendanceDetailsModel> modelArrayList) {
        super(fm);
        fragments = new ArrayList<>();
        this.baseElevation = baseElevation;
        this.modelArrayList = modelArrayList;

        for (int i = 0; i < modelArrayList.size(); i++) {
            addCardFragment(new StudentInfoCardFragment());
        }
    }

    @Override
    public float getBaseElevation() {
        return baseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return fragments.get(position).getCardView();
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return StudentInfoCardFragment.getInstance(position, modelArrayList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        fragments.set(position, (StudentInfoCardFragment) fragment);
        return fragment;
    }

    public void addCardFragment(StudentInfoCardFragment fragment) {
        fragments.add(fragment);
    }

}
