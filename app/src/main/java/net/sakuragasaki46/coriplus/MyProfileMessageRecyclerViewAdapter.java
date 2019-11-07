package net.sakuragasaki46.coriplus;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.sakuragasaki46.coriplus.dummy.DummyContent.DummyItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyProfileMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER = 1;
    private static final int CONTENT = 2;
    private final List<MessageItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private static final Pattern USERNAME_CHARACTERS = Pattern.compile("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)*");
    private HeaderViewHolder headerHolder;

    public MyProfileMessageRecyclerViewAdapter(List<MessageItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_profile_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_message, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder mHolder, final int position) {

        Log.d("onBindViewHolder", "binding " + mHolder + " " + position);

        if (mHolder instanceof HeaderViewHolder){
            final HeaderViewHolder holder = (HeaderViewHolder) mHolder;
            headerHolder = holder;

        } else if (mHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) mHolder;
            holder.mItem = mValues.get(position - 1);
            try {
                String username = holder.mItem.userInfo.getString("username");
                holder.mUsernameView.setText(username);
            } catch(JSONException ex){
                holder.mUsernameView.setText(" -- unknown -- ");
            }
            holder.mUsernameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = holder.itemView.getContext();
                    Intent intent = new Intent(context, ProfileActivity.class);
                    try {
                        intent.putExtra("userid", holder.mItem.userInfo.getString("id"));
                    } catch(JSONException ex){
                        Log.e("MyMessageRecyclerViewAd", "cannot start activity");
                        return;
                    }
                    context.startActivity(intent);
                }
            });
            SpannableString content = new SpannableString(holder.mItem.content);
            for (int i = 0; i < content.length(); i++){
                if(content.charAt(i) == '+'){
                    Matcher matcher = USERNAME_CHARACTERS.matcher(content.toString());
                    if (!matcher.find(i + 1)){
                        continue;
                    }
                    int endpos = matcher.end();
                    Log.d("MyMessageRecyclerViewAd", "span is (" + (i+1) + ", " + endpos + ")");
                    ProfileClickableSpan clickable = new ProfileClickableSpan(content.subSequence(i, endpos).toString());
                    content.setSpan(clickable, i+1, endpos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = endpos;
                }
            }

            holder.mContentView.setText(content);
            holder.mContentView.setMovementMethod(LinkMovementMethod.getInstance());
            // TODO
            holder.mFooterView.setText(holder.itemView.getContext().getResources()
                    .getStringArray(R.array.message_privacy_array)[holder.mItem.privacy]);
            holder.mFooterView.append(" - ");
            holder.mFooterView.append(new Date((long) (holder.mItem.timestamp * 1000)).toString());

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
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0){
            return HEADER;
        } else {
            return CONTENT;
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size() + 1;
    }

    public void setProfileInfo(ProfileActivity context, JSONObject userInfo){
        try {
            String fullName = userInfo.getString("full_name");
            ((TextView) headerHolder.mView.findViewById(R.id.full_name)).setText(fullName);

            int messages_count = userInfo.getInt("messages_count");
            ((TextView) headerHolder.mView.findViewById(R.id.messages_count)).setText(String.valueOf(messages_count));

            int followers_count = userInfo.getInt("followers_count");
            ((TextView) headerHolder.mView.findViewById(R.id.followers_count)).setText(String.valueOf(followers_count));

            int following_count = userInfo.getInt("following_count");
            ((TextView) headerHolder.mView.findViewById(R.id.following_count)).setText(String.valueOf(following_count));
        }catch(JSONException ex){
            Toast.makeText(context, R.string.network_request_failed, Toast.LENGTH_LONG).show();
        }
    }

    public void setMessages(ArrayList<MessageItem> items) {
        mValues.addAll(items);
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

    private class ProfileClickableSpan extends ClickableSpan {
        private String span;

        ProfileClickableSpan(String span){
            super();
            this.span = span;
        }

        @Override
        public void onClick(@NonNull View widget) {
            Log.d("ProfileClickableSpan", "clicked on: " + widget.toString());
            Context context = widget.getContext();
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userid", span);
            context.startActivity(intent);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mFullName;
        public final TextView mMessagesCount;
        public final TextView mFollowersCount;
        public final TextView mFollowingCount;
        public HeaderViewHolder(View view) {
            super(view);
            mView = view;
            mFullName = (TextView) view.findViewById(R.id.full_name);
            mMessagesCount = (TextView) view.findViewById(R.id.messages_count);
            mFollowersCount = (TextView) view.findViewById(R.id.followers_count);
            mFollowingCount = (TextView) view.findViewById(R.id.following_count);
        }
    }
}
