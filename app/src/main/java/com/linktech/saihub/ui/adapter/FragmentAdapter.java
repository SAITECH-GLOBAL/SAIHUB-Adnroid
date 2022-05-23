package com.linktech.saihub.ui.adapter;

import android.util.Pair;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentPagerAdapter {
    private List<Pair<String, Fragment>> mItems;

    public FragmentAdapter(FragmentManager fm, List<Pair<String, Fragment>> items) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mItems = items;
    }

    @Override
    public Fragment getItem(int position) {
        return mItems.get(position).second;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).hashCode();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mItems.get(position).first;
    }

    public List<Pair<String, Fragment>> getData() {
        return mItems;
    }

    public void setNewData(List<Pair<String, Fragment>> items) {
        mItems = items;
        notifyDataSetChanged();
    }


}
