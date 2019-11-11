package net.sakuragasaki46.coriplus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

public class ProfileAboutFragment extends Fragment {

    private JSONObject userInfo;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileAboutFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ProfileAboutFragment newInstance() {
        ProfileAboutFragment fragment = new ProfileAboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        Log.v("AboutFragment", "got here");
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_about, container, false);

        return view;
    }

    public void refreshFragment(){
        JSONObject userInfo = ((ProfileActivity) getActivity()).getUserInfo();

        if (userInfo != null){
            TableRow bioRow = getActivity().findViewById(R.id.row_biography);
            String bio = userInfo.optString("biography");
            if (bio != null){
                bioRow.setVisibility(View.VISIBLE);
                ((TextView) bioRow.findViewById(R.id.field_biography)).setText(bio);
            }

            TableRow websiteRow = getActivity().findViewById(R.id.row_website);
            String website = userInfo.optString("website");
            if (website != null){
                websiteRow.setVisibility(View.VISIBLE);
                ((TextView) websiteRow.findViewById(R.id.field_website)).setText(bio);
            }
        } else {
            Log.w("ProfileAboutFragment", "userInfo is null");
        }
    }
}
