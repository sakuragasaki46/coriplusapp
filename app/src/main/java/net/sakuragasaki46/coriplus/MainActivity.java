package net.sakuragasaki46.coriplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import net.sakuragasaki46.coriplus.ui.login.LoginActivity;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements MessageFragment.OnListFragmentInteractionListener{
    @Nullable
    private String host = null;
    @Nullable
    private String accessToken = null;
    private MessageFragment messageFragment;
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
                startActivity(new Intent(MainActivity.this, CreateActivity.class));
            }
        });

        loadingProgressBar = findViewById(R.id.loading);
        loadingProgressBar.setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.messages, new MessageFragment())
                .commit();

        loadingProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
