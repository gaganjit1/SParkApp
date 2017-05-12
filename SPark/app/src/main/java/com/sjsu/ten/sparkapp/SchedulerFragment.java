package com.sjsu.ten.sparkapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Ben on 11/17/2016.
 */

public class SchedulerFragment extends Fragment {
    private View view;
    public SchedulerFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_scheduler, container, false);
        }
        else {
            ((ViewGroup) view.getParent()).removeView(view);
        }

        return view;

    }

}
