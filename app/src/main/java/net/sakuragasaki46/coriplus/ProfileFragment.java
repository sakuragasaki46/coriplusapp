package net.sakuragasaki46.coriplus;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.sakuragasaki46.coriplus.dummy.DummyContent;
import net.sakuragasaki46.coriplus.dummy.DummyContent.DummyItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ProfileFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ProfileFragment newInstance(int columnCount) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyProfileRecyclerViewAdapter(new ArrayList<ProfileItem>(), mListener));


        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateSearch(String text){
        Log.d("updateSearch", "text is " + text);
        NetworkSingleton network = NetworkSingleton.getInstance();
        JSONObject jsonData = new JSONObject();
        try{
            jsonData.put("q", text);
        }catch(JSONException ex){
            Log.e("ProfileFragment", "could not put JSON data");
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                network.createApiUrl( "profile_search"),
                jsonData,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject obj){
                        ArrayList<ProfileItem> items = new ArrayList<>();
                        JSONArray jsonItems;
                        try{
                            jsonItems = obj.getJSONArray("users");
                        } catch (JSONException ex){
                            Log.e("ProfileFragment", "Response does not have a users array.");
                            Toast.makeText(getActivity(), R.string.network_request_failed, Toast.LENGTH_LONG).show();
                            return;
                        }
                        for (int i = 0; i < jsonItems.length(); i++){
                            try {
                                JSONObject jsonItem = jsonItems.getJSONObject(i);
                                items.add(new ProfileItem(
                                        jsonItem.getInt("id"),
                                        jsonItem.getString("username"),
                                        jsonItem.getString("full_name"),
                                        jsonItem.getInt("followers_count")
                                ));
                            } catch(JSONException ex){
                                Log.w("ProfileFragment", "missing some data on message");
                                continue;
                            }
                        }
                        recyclerView.setAdapter(new MyProfileRecyclerViewAdapter(items, mListener));
                        //context.loadingProgressBar.setVisibility(View.GONE);
                    }

                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e("ProfileFragment", "Couldn't refresh feed");
                        Toast.makeText(getActivity(), R.string.network_request_failed, Toast.LENGTH_LONG).show();
                    }
                }
        );
        network.addToQueue(request);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ProfileItem item);
    }
}
