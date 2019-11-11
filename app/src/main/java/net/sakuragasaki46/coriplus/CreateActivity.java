package net.sakuragasaki46.coriplus;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 2;
    private View loadingProgressBar;
    private EditText messageText;
    private ImageView imageView;
    private Spinner privacySpinner;
    private boolean isSetImageView = false;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

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

        privacySpinner = (Spinner) findViewById(R.id.message_privacy);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.message_privacy_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(adapter);

        loadingProgressBar = findViewById(R.id.loading);

        imageView = (ImageView) findViewById(R.id.message_visual);

        messageText = findViewById(R.id.message_text);

        try {
            messageText.setText(getIntent().getExtras().getString("preload"));
        } catch(NullPointerException ex){
            messageText.setText("");
        }

        Button button = (Button) findViewById(R.id.post_content);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMessage();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CreateActivity", "setting ImageView");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                        CreateActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(CreateActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    return;
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                    galleryIntent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                }
            }
        });
    }

    private void publishMessage() {
        NetworkSingleton network = NetworkSingleton.getInstance();
        JSONObject messageData = new JSONObject();
        try {
            messageData.put("text", messageText.getText().toString());
            messageData.put("privacy", privacySpinner.getSelectedItemId());
        } catch(JSONException ex){
            Log.w("CreateActivity", "error putting data");
        }
        Request request;
        if (isSetImageView) {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Log.d("CreateActivity", "bitmap is " + bitmap);
            request = createUploadBitmapRequest(bitmap);
        } else {
            request = new JsonObjectRequest(
                    Request.Method.POST,
                    network.createApiUrl("create"),
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
                                        CreateActivity.this,
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
                                    CreateActivity.this,
                                    R.string.network_request_failed,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
        network.addToQueue(request);
        setLoadingProgressBar(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);

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
        if (!messageText.getText().toString().trim().isEmpty()) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                    galleryIntent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                } else {
                    Toast.makeText(CreateActivity.this, R.string.upload_image_denied, Toast.LENGTH_LONG).show();
                    break;
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            Uri selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            isSetImageView = true;
        }
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private Request createUploadBitmapRequest(final Bitmap bitmap) {

        //getting the tag from the edittext
        final String text = messageText.getText().toString().trim();
        final long privacy = privacySpinner.getSelectedItemId();

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                NetworkSingleton.getInstance().createApiUrl("create2"),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.optString("status", "fail").equals("ok")) {
                                setLoadingProgressBar(false);
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                setLoadingProgressBar(false);
                                Toast.makeText(
                                        CreateActivity.this,
                                        "Unknown error",
                                        Toast.LENGTH_LONG).show();
                                Log.e("CreateActivity", "failed: message is " + obj.optString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setLoadingProgressBar(false);
                        Toast.makeText(
                                CreateActivity.this,
                                R.string.network_request_failed,
                                Toast.LENGTH_LONG).show();
                        Log.e("CreateActivity", "Network request failed: " + error.getMessage());
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("text", text);
                params.put("privacy", String.valueOf(privacy));
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("file", new DataPart(imagename + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        return volleyMultipartRequest;
    }
}
