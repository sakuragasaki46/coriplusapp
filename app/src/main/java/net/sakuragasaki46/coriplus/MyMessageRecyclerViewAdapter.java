package net.sakuragasaki46.coriplus;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.sakuragasaki46.coriplus.dummy.DummyContent.DummyItem;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected final List<MessageItem> mValues;
    protected final OnListFragmentInteractionListener mListener;
    private static final Pattern USERNAME_CHARACTERS = Pattern.compile("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)*");

    public MyMessageRecyclerViewAdapter(List<MessageItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder rHolder, final int position) {

        final ViewHolder holder = (ViewHolder) rHolder;

        holder.mItem = mValues.get(position);

        try {
            String username = mValues.get(position).userInfo.getString("username");
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
                    intent.putExtra("userid", mValues.get(position).userInfo.getString("id"));
                } catch(JSONException ex){
                    Log.e("MyMessageRecyclerViewAd", "cannot start activity");
                    return;
                }
                context.startActivity(intent);
            }
        });
        SpannableString content = new SpannableString(mValues.get(position).content);
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

        if (holder.mItem.imageUrl != null) {
            NetworkImageFetcher.getInstance().startDownloadImage(holder.mVisualView, holder.mItem.imageUrl);
            // new DownloadImageTask(holder.mVisualView).execute(holder.mItem.imageUrl);
            holder.mVisualView.setVisibility(View.VISIBLE);
            holder.mVisualView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(v.getContext(), holder.mVisualView);
                    popup.inflate(R.menu.image_options);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_download: {
                                    NetworkImageFetcher.getInstance().startSaveImage(holder.mItem.imageUrl);
                                    break;
                                }
                            }
                            return true;
                        }
                    });

                    popup.show();
                    return false;
                }
            });
        }
        // TODO
        holder.mFooterView.setText(holder.itemView.getContext().getResources()
                .getStringArray(R.array.message_privacy_array)[mValues.get(position).privacy]);
        holder.mFooterView.append(" - ");
        holder.mFooterView.append(new Date((long) (mValues.get(position).timestamp * 1000)).toString());

        holder.mOptionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                final Context context = v.getContext();
                PopupMenu popup = new PopupMenu(context, holder.mOptionsView);
                //inflating menu from xml resource
                try {
                    if (NetworkSingleton.getInstance().getUserId().equals(holder.mItem.userInfo.getString("id"))) {
                        popup.inflate(R.menu.message_options_self);
                    } else {
                        popup.inflate(R.menu.message_options_other);
                    }
                } catch (JSONException ex){
                    Log.e("MyMessageRecyclerViewAd", "no userid");
                }
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_edit: {
                                Intent intent = new Intent(context, EditActivity.class);
                                intent.putExtra("id", holder.mItem.id);
                                context.startActivity(intent);
                                break;
                            }
                            case R.id.action_report: {
                                Intent intent = new Intent(context, ReportActivity.class);
                                intent.putExtra("url", "message/" + holder.mItem.id);
                                context.startActivity(intent);
                                break;
                            }
                        }
                        return true;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });

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

    public void setMessages(ArrayList<MessageItem> items) {
        Log.d("MyMessageRecyclerViewAd", "setting " + items.size() + " items");
        mValues.addAll(items);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUsernameView;
        public final TextView mContentView;
        public final ImageView mVisualView;
        public final TextView mFooterView;
        public final TextView mOptionsView;
        public MessageItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = (TextView) view.findViewById(R.id.author_username);
            mContentView = (TextView) view.findViewById(R.id.content);
            mVisualView = (ImageView) view.findViewById(R.id.message_visual);
            mFooterView = (TextView) view.findViewById(R.id.message_footer);
            mOptionsView = (TextView) view.findViewById(R.id.message_options);
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
}
