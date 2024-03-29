package net.sakuragasaki46.coriplus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements OnListFragmentInteractionListener, ProfileHeaderFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CREATE = 10001;

    String userId;
    private ViewPager viewPager;
    private String username;
    private TabLayout tabLayout;

    private View headerView;
    private boolean followState = false;
    private ProfileViewPagerAdapter adapter;
    private JSONObject userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            userId = getIntent().getExtras().getString("userid");
        } catch(NullPointerException ex){
            userId = "self";
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final NetworkSingleton network = NetworkSingleton.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, CreateActivity.class);
                if (username != null && !(userId.equals("self") || userId.equals(network.getUserId()))) {
                    intent.putExtra("preload", "+" + username + " ");
                }
                startActivityForResult(intent, REQUEST_CREATE);
            }
        });

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("ProfileMessagesFragment", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        refreshProfile();

                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        AppBarLayout appBarLayout = findViewById(R.id.header_app_bar);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                swipeRefreshLayout.setEnabled(i == 0);
            }
        });

        viewPager = findViewById(R.id.viewpager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        adapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), String.valueOf(userId));
        viewPager.setAdapter(adapter);

        refreshProfile();

        //headerView = (View) findViewById(R.id.header_layout);
        //getSupportFragmentManager().beginTransaction()
        //        .replace(R.id.header_layout, new ProfileHeaderFragment()).commit();

        /*
        ProfileMessagesFragment fragment = new ProfileMessagesFragment();

        Bundle bundle = new Bundle();
        bundle.putString("userid", userid);
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.messages, fragment)
                .commit();
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public void onListFragmentInteraction(MessageItem item) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_refresh){

            /*
            ProfileMessagesFragment fragment = new ProfileMessagesFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userid", userId);
            fragment.setArguments(bundle);

            adapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), userId);
            viewPager.setAdapter(adapter);

            tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
             */

            refreshProfile();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE && resultCode == RESULT_OK) {
            refreshProfile();

        }
    }

    public JSONObject getUserInfo(){
        return userInfo;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //
    }

    private void refreshProfile(){
        final NetworkSingleton network = NetworkSingleton.getInstance();

        // View profileLayout = findViewById(R.id.profile_layout);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                network.createApiUrl("profile_info/" + userId),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            userInfo = response.getJSONObject("user");
                        } catch(JSONException ex){
                            Toast.makeText(ProfileActivity.this, R.string.profile_missing, Toast.LENGTH_LONG).show();
                            ProfileActivity.this.finish();
                            return;
                        }
                        try{
                            Log.d("ProfileActivity", "userId is " + userId);
                            String username = userInfo.getString("username");
                            ProfileActivity.this.username = username;
                            getSupportActionBar().setTitle(username);

                            try {
                                setProfileInfo(userInfo);
                            }catch(JSONException ex){
                                Toast.makeText(ProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                            }
                        }catch (JSONException ex){
                            Toast.makeText(ProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                        }catch(NullPointerException ex){
                            Log.e("ProfileActivity", "NullPointerException caught");
                            ex.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                    }
                }
        );
        network.setContext(ProfileActivity.this);
        network.createQueue();
        network.addToQueue(request);
    }

    public void setProfileInfo(JSONObject userInfo) throws JSONException{
        adapter.notifyDataSetChanged();
        View headerView = findViewById(R.id.profile_header);
        ViewPager contentView = findViewById(R.id.viewpager);
        final TextView mFollowersCount = findViewById(R.id.followers_count);
        
        userId = String.valueOf(userInfo.getInt("id"));
        Log.d("ProfileActivity", "userId is " + userId);

        String fullName = userInfo.getString("full_name");
        ((TextView) headerView.findViewById(R.id.full_name)).setText(fullName);

        int messages_count = userInfo.getInt("messages_count");
        ((TextView) headerView.findViewById(R.id.messages_count)).setText(String.valueOf(messages_count));

        int followers_count = userInfo.getInt("followers_count");
        ((TextView) headerView.findViewById(R.id.followers_count)).setText(String.valueOf(followers_count));

        int following_count = userInfo.getInt("following_count");
        ((TextView) headerView.findViewById(R.id.following_count)).setText(String.valueOf(following_count));

        LinearLayout buttonArea = headerView.findViewById(R.id.action_buttons);
        if (buttonArea != null){
            if (NetworkSingleton.getInstance().getUserId().equals(String.valueOf(userId))) {
                // edit profile
                Button button1 = (Button) buttonArea.findViewById(R.id.button_edit_profile);
                button1.setVisibility(View.VISIBLE);
                button1.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        v.getContext().startActivity(new Intent(v.getContext(), EditProfileActivity.class ));
                    }
                });
            } else {
                // follow
                final Button button1 = (Button) buttonArea.findViewById(R.id.button_follow);
                button1.setVisibility(View.VISIBLE);
                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NetworkSingleton network = NetworkSingleton.getInstance();
                        if (!followState){
                            // follow
                            JsonObjectRequest request = new JsonObjectRequest(
                                    Request.Method.POST,
                                    network.createApiUrl("relationships/" + userId + "/follow"),
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            if(!response.optString("status", "fail").equals("ok")){
                                                setButtonState(false);
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            setButtonState(false);
                                        }
                                    }
                            );
                            network.addToQueue(request);
                            setButtonState(true);
                        } else {
                            // unfollow
                            JsonObjectRequest request = new JsonObjectRequest(
                                    Request.Method.POST,
                                    network.createApiUrl("relationships/" + userId + "/unfollow"),
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            if(!response.optString("status", "fail").equals("ok")){
                                                setButtonState(true);
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            setButtonState(true);
                                        }
                                    }
                            );
                            network.addToQueue(request);
                            setButtonState(false);
                        }
                    }

                    void setButtonState(boolean state) {
                        if (!state){
                            button1.setText(R.string.action_follow);
                            mFollowersCount.setText(String.valueOf(Integer.valueOf(
                                    mFollowersCount.getText().toString()) - 1));
                        } else {
                            button1.setText(R.string.action_following);
                            mFollowersCount.setText(String.valueOf(Integer.valueOf(
                                    mFollowersCount.getText().toString()) + 1));
                        }
                        followState = state;
                    }
                });
            }
        } else {
            Log.d("MyProfileMessageRecycle", "buttonArea not found");
        }

        if(!NetworkSingleton.getInstance().getUserId().equals(String.valueOf(userId))) {
            JSONObject relationships = userInfo.getJSONObject("relationships");
            followState = relationships.getBoolean("following");

            Button button1 = headerView.findViewById(R.id.button_follow);

            Log.d("setProfileInfo", "followState is " + followState);

            if (followState) {
                button1.setText(R.string.action_following);
            } else {
                button1.setText(R.string.action_follow);
            }
        }
    }
}
