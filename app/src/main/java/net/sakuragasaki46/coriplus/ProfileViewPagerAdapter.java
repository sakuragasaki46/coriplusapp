package net.sakuragasaki46.coriplus;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import org.json.JSONObject;

public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {
    private String title[] = {"Messages", "About"};
    private String userid;
    private JSONObject userInfo;

    public ProfileViewPagerAdapter(FragmentManager manager, String userid) {
        super(manager);
        this.userid = userid;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if (position == 0){
            fragment = ProfileMessagesFragment.newInstance();
        } else {
            fragment = ProfileAboutFragment.newInstance();
        }

        Bundle bundle = new Bundle();
        bundle.putString("userid", userid);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setUserInfo(JSONObject userInfo) {
        this.userInfo = userInfo;
    }
}
