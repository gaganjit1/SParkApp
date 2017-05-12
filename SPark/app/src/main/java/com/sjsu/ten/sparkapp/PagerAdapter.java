package com.sjsu.ten.sparkapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Ben on 9/24/2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter{
    int mNumberOfTabs;

    public PagerAdapter(FragmentManager fm, int NumberOfTabs){
        super(fm);
        this.mNumberOfTabs = NumberOfTabs;
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                NavigationFragment navigationFragment = new NavigationFragment();
                return navigationFragment;
            case 1:
                GaragePickerFragment Fragment1 = new GaragePickerFragment();
                return Fragment1;
            //case 2:
            //    SchedulerFragment schedulerFragment= new SchedulerFragment();
            //    return schedulerFragment;
            case 2:
                PayForSpotFragment payfor = new PayForSpotFragment();
                return payfor;
            default:
                return null;
        }
    }

    @Override
    public int getCount(){
        return mNumberOfTabs;
    }
}
