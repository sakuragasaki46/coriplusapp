package net.sakuragasaki46.coriplus;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
public class FeedMessagesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FeedMessagesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FeedMessagesFragment newInstance(int columnCount) {
        FeedMessagesFragment fragment = new FeedMessagesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
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
        final View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("ProfileMessagesFragment", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        updateFeed(view);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );


        swipeRefreshLayout.setRefreshing(true);
        updateFeed(view);
        swipeRefreshLayout.setRefreshing(false);

        return view;
    }

    private void updateFeed(final View view) {
        RecyclerView rView = view.findViewById(R.id.list);
        Log.d("onCreateView", "view " + (view instanceof RecyclerView? "is": "is not") + " instance of RecyclerView");

        // Set the adapter
        if (rView != null) {
            final Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) rView;
            LinearLayoutManager llm = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(llm);
            recyclerView.setAdapter(new MyMessageRecyclerViewAdapter(new ArrayList<MessageItem>(), mListener));


            NetworkSingleton network = NetworkSingleton.getInstance();
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    network.createApiUrl("feed"),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject obj) {
                            ArrayList<MessageItem> items = new ArrayList<>();
                            JSONArray jsonItems;
                            try {
                                jsonItems = obj.getJSONArray("timeline_media");
                            } catch (JSONException ex) {
                                Log.e("FeedMessagesFragment", "Response does not have a timeline_media array.");
                                Toast.makeText(getActivity(), R.string.feed_error, Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (jsonItems.length() == 0) {
                                recyclerView.setVisibility(View.GONE);
                                view.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                                return;
                            }
                            for (int i = 0; i < jsonItems.length(); i++) {
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
                                } catch (JSONException ex) {
                                    Log.w("FeedMessagesFragment", "missing some data on message");
                                    continue;
                                }
                            }
                            recyclerView.setAdapter(new MyMessageRecyclerViewAdapter(items, mListener));
                            //context.loadingProgressBar.setVisibility(View.GONE);
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("FeedMessagesFragment", "Couldn't refresh feed");
                            Toast.makeText(getActivity(), R.string.feed_error, Toast.LENGTH_LONG).show();
                        }
                    }
            );
            network.setContext(getActivity());
            network.createQueue();
            network.addToQueue(request);
        }
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
}
