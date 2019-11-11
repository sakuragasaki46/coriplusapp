package net.sakuragasaki46.coriplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileTabFragment extends Fragment {
    int position;
    private TextView textView;

    public static Fragment getInstance(String userid, int position) {
        // Bundle bundle = new Bundle();
        // bundle.putInt("pos", position);
        // ProfileTabFragment tabFragment = new ProfileTabFragment();
        // tabFragment.setArguments(bundle);
        // return tabFragment;

        Fragment fragment;
        if (position == 0){
            fragment = new ProfileMessagesFragment();
        } else {
            fragment = new ProfileAboutFragment();
        }

        Bundle bundle = new Bundle();
        bundle.putString("userid", userid);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (position == 0) {
            return inflater.inflate(R.layout.fragment_profile_message_list, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_profile_about, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
