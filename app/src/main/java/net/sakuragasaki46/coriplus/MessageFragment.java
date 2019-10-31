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
public class MessageFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MessageFragment newInstance(int columnCount) {
        MessageFragment fragment = new MessageFragment();
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
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        Log.d("onCreateView", "view " + (view instanceof RecyclerView? "is": "is not") + " instance of RecyclerView");

        // Set the adapter
        if (view instanceof RecyclerView) {
            final Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) view;
            LinearLayoutManager llm = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(llm);
            recyclerView.setAdapter(new MyMessageRecyclerViewAdapter(new ArrayList<MessageItem>(), mListener));


            NetworkSingleton network = NetworkSingleton.getInstance();
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    network.createApiUrl( "feed"),
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
                            recyclerView.setAdapter(new MyMessageRecyclerViewAdapter(items, mListener));
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
            network.setContext(getActivity());
            network.createQueue();
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(MessageItem item);
    }
}
