package net.sakuragasaki46.coriplus;

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

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MyProfileMessageRecyclerViewAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileMessagesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ProfileMessagesFragment newInstance(int columnCount) {
        ProfileMessagesFragment fragment = new ProfileMessagesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        Log.v("MessageFragment", "got here");
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
        View view = inflater.inflate(R.layout.fragment_profile_message_list, container, false);

        RecyclerView rView = view.findViewById(R.id.list);
        Log.d("onCreateView", "view " + (view instanceof RecyclerView? "is": "is not") + " instance of RecyclerView");

        // Set the adapter
        if (rView != null) {
            final Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) rView;
            LinearLayoutManager llm = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(llm);

            mAdapter = new MyProfileMessageRecyclerViewAdapter(new ArrayList<MessageItem>(), mListener);
            recyclerView.setAdapter(mAdapter);

            String userId;
            try {
                userId = getArguments().getString("userid");
            } catch (NullPointerException e) {
                userId = "self";
            }

            NetworkSingleton network = NetworkSingleton.getInstance();
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    network.createApiUrl("profile_info/" + userId),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            JSONObject userInfo;
                            try{
                                userInfo = response.getJSONObject("user");
                            } catch(JSONException ex){
                                Toast.makeText(getActivity(), R.string.profile_missing, Toast.LENGTH_LONG).show();
                                getActivity().finish();
                                return;
                            }
                            try{
                                String username = userInfo.getString("username");
                                // getActivity().username = username;
                                ((ProfileActivity) getActivity()).getSupportActionBar().setTitle(username);

                                mAdapter.setProfileInfo((ProfileActivity) getActivity(), userInfo);
                            }catch (JSONException ex){
                                Toast.makeText(getActivity(), R.string.network_request_failed, Toast.LENGTH_LONG).show();
                            }catch(NullPointerException ex){
                                Log.e("ProfileActivity", "NullPointerException caught");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getActivity(), R.string.network_request_failed, Toast.LENGTH_LONG).show();
                        }
                    }
            );
            network.setContext(getActivity());
            network.createQueue();
            network.addToQueue(request);

            request = new JsonObjectRequest(
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
                                Log.e("MessageFragment", "Response does not have a timeline_media array.");
                                Toast.makeText(getActivity(), R.string.feed_error, Toast.LENGTH_LONG).show();
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
                                            jsonItem.getInt("privacy")
                                    ));
                                } catch(JSONException ex){
                                    Log.w("MessageFragment", "missing some data on message");
                                    continue;
                                }
                            }
                            mAdapter.setMessages(items);
                            //context.loadingProgressBar.setVisibility(View.GONE);
                        }

                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error){
                            Log.e("MessageFragment", "Couldn't refresh feed");
                            Toast.makeText(getActivity(), R.string.feed_error, Toast.LENGTH_LONG).show();
                        }
                    }
            );
            network.addToQueue(request);
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
}
