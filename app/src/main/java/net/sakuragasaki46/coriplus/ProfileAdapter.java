package net.sakuragasaki46.coriplus;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_CONTENT = 2;
    private HeaderViewHolder headerHolder;
    private ContentViewHolder contentHolder;
    private int userId;
    private JSONObject userInfo;
    private boolean followState;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_profile_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_profile_content, parent, false);
            return new ContentViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_HEADER;
        }
        return TYPE_CONTENT;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder){
            headerHolder = (HeaderViewHolder) holder;
        } else if (holder instanceof ContentViewHolder) {
            contentHolder = (ContentViewHolder) holder;
        } else {
            Log.w("ProfileAdapter", "neither meat nor fish");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
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

    private class ContentViewHolder extends RecyclerView.ViewHolder {
        private final View mView;

        public ContentViewHolder(View view) {
            super(view);
            mView = view;

        }
    }
}
