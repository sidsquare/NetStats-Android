package com.siddharth.netstats;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Siddharth on 23-Sep-15.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs=4;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                mFrag frag1 = new mFrag();
                return frag1;
            case 1:
                wFrag frag2 = new wFrag();
                return frag2;
            case 2:
                gFrag frag3=new gFrag();
                return frag3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
