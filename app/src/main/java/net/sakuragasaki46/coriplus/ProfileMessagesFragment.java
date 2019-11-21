package net.sakuragasaki46.coriplus;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ProfileMessagesFragment extends Fragment {

    private RecyclerView recyclerView;

    private OnListFragmentInteractionListener mListener;
    private MyMessageRecyclerViewAdapter mAdapter;
    private int debugCounter = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileMessagesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ProfileMessagesFragment newInstance() {
        ProfileMessagesFragment fragment = new ProfileMessagesFragment();
        Bundle args = new Bundle();
        // args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        Log.v("FeedMessagesFragment", "got here");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // TODO
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile_message_list, container, false);

        // TODO

        Log.d("ProfileMessagesFragment", "getActivity() is " + getActivity());

        updateProfile(view);

        return view;
    }

    private void updateProfile(final View view) {
        if (recyclerView == null) {
            RecyclerView rView = view.findViewById(R.id.list);
            Log.d("onCreateView", "(" + (debugCounter++) + ") rView " + (rView != null ? "is not" : "is") + " null");

            // Set the adapter
            if (rView != null) {
                recyclerView = (RecyclerView) rView;

                final Context context = view.getContext();
                LinearLayoutManager llm = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(llm);
                Log.v("ProfileMessagesFragment", "after setLayoutManager");

                mAdapter = new MyMessageRecyclerViewAdapter(new ArrayList<MessageItem>(), mListener);
                recyclerView.setAdapter(mAdapter);
                Log.v("ProfileMessagesFragment", "after setAdapter");
            }
        }

        String userId;
        try {
            userId = getArguments().getString("userid");
        } catch (NullPointerException e) {
            userId = "self";
        }

        NetworkSingleton network = NetworkSingleton.getInstance();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                network.createApiUrl( "profile_info/feed/" + userId),
                null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject obj){
                        ArrayList<MessageItem> items = new ArrayList<>();
                        JSONArray jsonItems;
                        try{
                            jsonItems = obj.getJSONArray("timeline_media");
                        } catch (JSONException ex){
                            Log.e("FeedMessagesFragment", "Response does not have a timeline_media array.");
                            Toast.makeText(getActivity(), R.string.feed_error, Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (jsonItems.length() == 0){
                            recyclerView.setVisibility(View.GONE);
                            view.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                            return;
                        }
                        for (int i = 0; i < jsonItems.length(); i++){
                            try {
                                JSONObject jsonItem = jsonItems.getJSONObject(i);
                                items.add(new MessageItem(
                                        jsonItem.getInt("id"),
                                        jsonItem.getJSONObject("user"),
                                        jsonItem.getString("text"),
                                        jsonItem.getDouble("pub_date"),
                                        jsonItem.getInt("privacy"),
                                        jsonItem.optString("media")
                                ));
                            } catch(JSONException ex){
                                Log.w("FeedMessagesFragment", "missing some data on message");
                                continue;
                            }
                        }
                        mAdapter.setMessages(items);
                        Log.d("ProfileMessagesFragment","recyclerView.getHeight() is " + recyclerView.getHeight());
                        /*
                        try {
                            recyclerView.setMinimumHeight(
                                    ((Activity) recyclerView.getContext()).findViewById(R.id.viewpager).getHeight() -
                                            ((Activity) recyclerView.getContext()).findViewById(R.id.tabs).getHeight());
                        } catch(NullPointerException ex){
                            Log.w("ProfileMessagesFragment", "life is sad :'(");
                        }

                         */
                        // mAdapter.setMessages(items);
                        //context.loadingProgressBar.setVisibility(View.GONE);
                    }

                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e("FeedMessagesFragment", "Couldn't refresh feed");
                        Toast.makeText(getActivity(), R.string.feed_error, Toast.LENGTH_LONG).show();
                    }
                }
        );
        network.addToQueue(request);
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
}
