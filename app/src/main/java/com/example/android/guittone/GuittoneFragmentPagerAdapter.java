package com.example.android.guittone;

/**
 * Created by Giacomo on 14/02/2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

/**
 * Created by Giacomo on 14/02/2017.
 */



/**
 * Provides the appropriate {@link Fragment} for a view pager.
 */
public class GuittoneFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private String tabTitles[] = new String[] { "Plugs", "Power Consumption"};
    public GuittoneFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0){
            return  new MainFragment();
        }else if(position ==1) {
            return  new PowerFragment();
        }else {
            return  new MainFragment();
        }

    }
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


    @Override
    public int getCount() {
        return 2;
    }
}