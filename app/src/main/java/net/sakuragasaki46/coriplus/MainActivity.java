package net.sakuragasaki46.coriplus;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import net.sakuragasaki46.coriplus.ui.login.LoginActivity;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements OnListFragmentInteractionListener{
    private static final int REQUEST_CREATE = 10001;
    @Nullable
    private String host = null;
    @Nullable
    private String accessToken = null;
    private FeedMessagesFragment messageFragment;
    private View loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        host = sharedPreferences.getString("host", null);
        if(host != null){
            NetworkSingleton.getInstance().setContext(this);
            accessToken = NetworkSingleton.getInstance().getAccessToken();
            Log.v("MainActivity", "accessToken is " + (accessToken == null? "null" : "not null"));
            if(accessToken == null) {
                startActivity(new Intent(MainActivity.this,
                        net.sakuragasaki46.coriplus.ui.login.LoginActivity.class));
                finish();
            }
            setContentView(R.layout.activity_feed);
        } else {
            setContentView(R.layout.activity_main);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, CreateActivity.class), REQUEST_CREATE);
            }
        });

        if (host != null) {
            loadingProgressBar = findViewById(R.id.loading);
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.VISIBLE);
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.messages, new FeedMessagesFragment())
                    .commit();

            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
        }

        NetworkImageFetcher.getInstance().setContext(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        } else {
            Log.w("MainActivity", "could not create search bar");
        }

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

        if (id == R.id.action_create){
            startActivity(new Intent(this, CreateActivity.class));
            return true;
        }

        if (id == R.id.action_profile){
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }

        if (id == R.id.action_search){
            startActivity(new Intent(this, SearchProfilesActivity.class));
            return true;
        }

        if (id == R.id.action_refresh){
            FeedMessagesFragment fragment = new FeedMessagesFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.messages, fragment)
                    .commit();
        }

        if (id == R.id.action_logout){
            NetworkSingleton.getInstance().setAccessToken(null);
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(MessageItem item) {
        // TODO
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE && resultCode == RESULT_OK) {
            FeedMessagesFragment fragment = new FeedMessagesFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.messages, fragment)
                    .commit();
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == NetworkImageFetcher.REQUEST_DOWNLOAD_IMAGE){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                NetworkImageFetcher.getInstance().startSaveImage();
            }
        }
    }*/
}
