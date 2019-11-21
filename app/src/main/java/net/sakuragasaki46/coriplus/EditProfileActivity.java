package net.sakuragasaki46.coriplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity {
    private EditText userNameView;
    private EditText fullNameView;
    private EditText biographyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.edit_profile);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final NetworkSingleton network = NetworkSingleton.getInstance();

        userNameView = findViewById(R.id.username);
        userNameView.setFilters(new InputFilter[] {
                new InputFilter.AllCaps() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return String.valueOf(source).toLowerCase();
                    }
                }
        });
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                // s.replace(0, s.length(), s.toString().toLowerCase());
                if(userNameView.getText().toString().isEmpty()){
                    userNameView.setError(getResources().getString(R.string.invalid_username));
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.GET,
                        network.createApiUrl("username_availability/" + userNameView.getText().toString()),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    if (!response.getBoolean("is_valid")){
                                        userNameView.setError(getResources().getString(R.string.invalid_username2));
                                    } else if (!response.getBoolean("is_available")){
                                        userNameView.setError(getResources().getString(R.string.username_unavailable));
                                    } else {
                                        userNameView.setError(null);
                                    }
                                } catch (JSONException ex){
                                    Toast.makeText(EditProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(EditProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                            }
                        }
                );
                network.addToQueue(request);
            }
        };
        userNameView.addTextChangedListener(afterTextChangedListener);

        fullNameView = findViewById(R.id.full_name);

        biographyView = findViewById(R.id.field_biography);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                network.createApiUrl("profile_info/self"),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONObject userInfo = response.getJSONObject("user");
                            userNameView.setText(userInfo.getString("username"));
                            fullNameView.setText(userInfo.getString("full_name"));
                            biographyView.setText(userInfo.getString("biography"));
                        } catch(JSONException ex){
                            Toast.makeText(EditProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
        );
        network.addToQueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_save){
            saveProfile();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        NetworkSingleton network = NetworkSingleton.getInstance();

        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("username", userNameView.getText().toString());
            jsonData.put("full_name", fullNameView.getText().toString());
            jsonData.put("biography", biographyView.getText().toString());
        } catch(JSONException ex){
            Log.e("EditProfileActivity", "could not build jsonData");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                network.createApiUrl("edit_profile"),
                jsonData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.optString("status", "fail").equals("ok")){
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditProfileActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                    }
                }
        );
        network.addToQueue(request);
    }
}
