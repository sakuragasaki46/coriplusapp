package net.sakuragasaki46.coriplus;

import androidx.annotation.NonNull;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.sakuragasaki46.coriplus.OnListFragmentInteractionListener;
import net.sakuragasaki46.coriplus.dummy.DummyContent.DummyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMessageRecyclerViewAdapter extends RecyclerView.Adapter<MyMessageRecyclerViewAdapter.ViewHolder> {

    private final List<MessageItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private static final Pattern USERNAME_CHARACTERS = Pattern.compile("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)*");

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
    public void onBindViewHolder(final ViewHolder holder, final int position) {

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
            new DownloadImageTask(holder.mVisualView).execute(holder.mItem.imageUrl);
            holder.mVisualView.setVisibility(View.VISIBLE);
        }
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
        public final ImageView mVisualView;
        public final TextView mFooterView;
        public MessageItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = (TextView) view.findViewById(R.id.author_username);
            mContentView = (TextView) view.findViewById(R.id.content);
            mVisualView = (ImageView) view.findViewById(R.id.message_visual);
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        // TODO cache this
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("DownloadImageTask", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
