package net.sakuragasaki46.coriplus;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateActivity extends AppCompatActivity {

    private View loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            setTitle(R.string.action_create);
        } else {
            Log.i("CreateActivity", "actionBar missing");
        }

        final Spinner spinner = (Spinner) findViewById(R.id.message_privacy);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.message_privacy_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        loadingProgressBar = findViewById(R.id.loading);

        final EditText messageText = findViewById(R.id.message_text);

        Button button = (Button) findViewById(R.id.post_content);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkSingleton network = NetworkSingleton.getInstance();
                JSONObject messageData = new JSONObject();
                try {
                    messageData.put("text", messageText.getText().toString());
                    messageData.put("privacy", spinner.getSelectedItemId());
                } catch(JSONException ex){
                    Log.w("CreateActivity", "error putting data");
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        network.createApiUrl("create"),
                        messageData,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if(response.optString("status", "fail").equals("ok")){
                                    setLoadingProgressBar(false);
                                    finish();
                                } else {
                                    Toast.makeText(
                                            CreateActivity.this,
                                            "Unknown error",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(
                                        CreateActivity.this,
                                        R.string.network_request_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                );
                network.addToQueue(request);
                setLoadingProgressBar(true);
            }
        });
    }

    private void setLoadingProgressBar(boolean value){
        if (value){
            loadingProgressBar.setVisibility(View.VISIBLE);
            Log.d("loadingProgressBar", "set to visible");
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            Log.d("loadingProgressBar", "set to gone");

        }
    }
}
