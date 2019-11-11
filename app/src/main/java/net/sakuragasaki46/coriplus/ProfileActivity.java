package net.sakuragasaki46.coriplus;

import android.content.Intent;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements OnListFragmentInteractionListener {

    private static final int REQUEST_CREATE = 10001;

    private String userid;
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
            userid = getIntent().getExtras().getString("userid");
        } catch(NullPointerException ex){
            userid = "self";
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
                if (username != null && !(userid.equals("self") || userid.equals(network.getUserId()))) {
                    intent.putExtra("preload", "+" + username + " ");
                }
                startActivityForResult(intent, REQUEST_CREATE);
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        adapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), userid);
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        headerView = (View) findViewById(R.id.header_layout);

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

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                network.createApiUrl("profile_info/" + userid),
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
                            String username = userInfo.getString("username");
                            ProfileActivity.this.username = username;
                            getSupportActionBar().setTitle(username);

                            try {
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
                                    if (NetworkSingleton.getInstance().getUserId().equals(String.valueOf(userid))) {
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
                                                            network.createApiUrl("relationships/" + userid + "/follow"),
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
                                                            network.createApiUrl("relationships/" + userid + "/unfollow"),
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

                                                TextView mFollowersCount = findViewById(R.id.followers_count);
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

                                    //adapter.setUserInfo(userInfo);
                                    try {
                                        ((ProfileAboutFragment) getSupportFragmentManager().findFragmentById(R.id.profile_about))
                                                .refreshFragment();
                                    } catch(NullPointerException ex){
                                        Log.d("ProfileActivity", "ProfileAboutFragment not present");
                                    }
                                } else {
                                    Log.d("MyProfileMessageRecycle", "buttonArea not found");
                                }

                                if(!network.getUserId().equals(String.valueOf(userid))){
                                    JSONObject relationships = userInfo.getJSONObject("relationships");
                                    followState = relationships.getBoolean("following");

                                    Button button1 = headerView.findViewById(R.id.button_follow);

                                    Log.d("setProfileInfo", "followState is " + followState);

                                    if (followState){
                                        button1.setText(R.string.action_following);
                                    } else {
                                        button1.setText(R.string.action_follow);
                                    }
                                }
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
            ProfileMessagesFragment fragment = new ProfileMessagesFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userid", userid);
            fragment.setArguments(bundle);

            adapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), userid);
            viewPager.setAdapter(adapter);

            tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE && resultCode == RESULT_OK) {
            ProfileMessagesFragment fragment = new ProfileMessagesFragment();

            adapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), userid);
            viewPager.setAdapter(adapter);

            tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);

        }
    }

    public JSONObject getUserInfo(){
        return userInfo;
    }
}
