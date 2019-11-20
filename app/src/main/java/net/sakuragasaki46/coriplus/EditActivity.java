package net.sakuragasaki46.coriplus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 2;
    private View loadingProgressBar;
    private EditText messageText;
    private ImageView imageView;
    private Spinner privacySpinner;
    private int messageId;
    private boolean hasChanged = false;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            messageId = getIntent().getExtras().getInt("id");
        } catch(NullPointerException ex){
            Log.e("EditActivity", "no messageId specified");
            finish();
        }
        setContentView(R.layout.activity_create);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            setTitle(R.string.action_edit);
        } else {
            Log.i("CreateActivity", "actionBar missing");
        }

        privacySpinner = (Spinner) findViewById(R.id.message_privacy);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.message_privacy_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(adapter);

        loadingProgressBar = findViewById(R.id.loading);

        imageView = (ImageView) findViewById(R.id.message_visual);

        messageText = findViewById(R.id.message_text);
        messageText.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                hasChanged = true;
                v.setOnClickListener(null);
            }
        });

        Button button = (Button) findViewById(R.id.post_content);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMessage();
            }
        });

        setLoadingProgressBar(true);

        NetworkSingleton network = NetworkSingleton.getInstance();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                network.createApiUrl("request_edit/" + messageId),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response.optString("status", "fail").equals("ok")){
                            try{
                                JSONObject messageInfo = response.getJSONObject("message_info");
                                messageText.setText(messageInfo.getString("text"));
                                privacySpinner.setSelection(messageInfo.getInt("privacy"));
                                NetworkImageFetcher.getInstance().startDownloadImage(imageView, messageInfo.getString("media"));

                                setLoadingProgressBar(false);
                            }catch(JSONException ex){
                                Toast.makeText(EditActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(EditActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditActivity.this, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
        );

        network.addToQueue(request);
    }

    private void publishMessage() {
        NetworkSingleton network = NetworkSingleton.getInstance();
        JSONObject messageData = new JSONObject();
        try {
            messageData.put("text", messageText.getText().toString());
            messageData.put("privacy", privacySpinner.getSelectedItemId());
        } catch(JSONException ex){
            Log.w("EditActivity", "error putting data");
        }
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                network.createApiUrl("save_edit/" + messageId),
                messageData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.optString("status", "fail").equals("ok")) {
                            setLoadingProgressBar(false);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            setLoadingProgressBar(false);
                            Toast.makeText(
                                    EditActivity.this,
                                    "Unknown error",
                                    Toast.LENGTH_LONG).show();
                            Log.e("CreateActivity", "failed: message is " + response.optString("message"));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setLoadingProgressBar(false);
                        Toast.makeText(
                                EditActivity.this,
                                R.string.network_request_failed,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
        network.addToQueue(request);
        setLoadingProgressBar(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_post){
            publishMessage();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasChanged) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.leaving_create)
                    .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton(R.string.action_no, null)
                    .show();
        } else {
            finish();
        }
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
