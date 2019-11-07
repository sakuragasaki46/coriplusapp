package net.sakuragasaki46.coriplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.sakuragasaki46.coriplus.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchProfilesActivity extends AppCompatActivity implements ProfileFragment.OnListFragmentInteractionListener {


    private ProfileFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profiles);

        fragment = new ProfileFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profiles, fragment)
                .commit();

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = findViewById(R.id.menu_search);
        // Assumes current activity is the searchable activity
        if (searchView != null){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    String text = (String) searchView.getQuery().toString();
                    Log.d("SearchProfilesActivity", "search is: " + text);
                    fragment.updateSearch(text);
                    return false;
                }
            });
        } else{
            Log.w("SearchProfilesActivity", "searchView is null");
        }
    }


    @Override
    public boolean onSearchRequested(){
        //final SearchView searchView = (SearchView) findViewById(R.id.menu_search);

        return true;
    }

    @Override
    public void onListFragmentInteraction(ProfileItem item) {
        Intent intent = new Intent(this, ProfileActivity.class);
        Log.d("SearchProfilesActivity", "userid is " + item.id);
        intent.putExtra("userid", String.valueOf(item.id));
        startActivity(intent);
    }
}
