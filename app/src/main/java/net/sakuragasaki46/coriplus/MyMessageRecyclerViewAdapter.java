package net.sakuragasaki46.coriplus;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.sakuragasaki46.coriplus.MessageFragment.OnListFragmentInteractionListener;
import net.sakuragasaki46.coriplus.dummy.DummyContent.DummyItem;

import org.json.JSONException;

import java.util.List;
import java.util.Date;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMessageRecyclerViewAdapter extends RecyclerView.Adapter<MyMessageRecyclerViewAdapter.ViewHolder> {

    private final List<MessageItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyMessageRecyclerViewAdapter(List<MessageItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        try {
            holder.mUsernameView.setText(mValues.get(position).userInfo.getString("username"));
        } catch(JSONException ex){
            holder.mUsernameView.setText(" -- unknown -- ");
        }
        holder.mContentView.setText(mValues.get(position).content);
        // TODO
        holder.mFooterView.setText(holder.itemView.getContext().getResources()
                .getStringArray(R.array.message_privacy_array)[mValues.get(position).privacy]);
        holder.mFooterView.append(" - ");
        holder.mFooterView.append(new Date((long) (mValues.get(position).timestamp * 1000)).toString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUsernameView;
        public final TextView mContentView;
        public final TextView mFooterView;
        public MessageItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = (TextView) view.findViewById(R.id.author_username);
            mContentView = (TextView) view.findViewById(R.id.content);
            mFooterView = (TextView) view.findViewById(R.id.message_footer);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
