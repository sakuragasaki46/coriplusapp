package net.sakuragasaki46.coriplus.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.sakuragasaki46.coriplus.MainActivity;
import net.sakuragasaki46.coriplus.NetworkSingleton;
import net.sakuragasaki46.coriplus.R;
import net.sakuragasaki46.coriplus.data.model.LoggedInUser;
import net.sakuragasaki46.coriplus.ui.login.LoginActivity;
import net.sakuragasaki46.coriplus.ui.login.LoginViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static androidx.core.content.ContextCompat.startActivity;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private final LoginActivity context;

    LoggedInUser user;

    public LoginDataSource(LoginActivity context){
        this.context = context;
    }

    public void login(final String username, final String password) {

        try {
            NetworkSingleton network = NetworkSingleton.getInstance();
            network.setContext(context);
            network.createQueue();

            String url = network.createUrl("/get_access_token");
            Log.d("LoginDataSource", "Connecting to: " + url);
            final JSONObject jsonData = new JSONObject();
            jsonData.put("username", username);
            jsonData.put("password", password);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonData,
                    new Response.Listener<JSONObject>(){
                        @Override
                        public void onResponse(JSONObject response){
                            try{
                                String token = response.getString("token");
                                LoginDataSource.this.setAccessToken(token);
                                LoggedInUser user = new LoggedInUser(token, username);
                                LoginRepository loginRepository = LoginRepository.getInstance(LoginDataSource.this);
                                loginRepository.setLoggedInUser(user);
                                context.setLoadingProgressBar(false);
                                context.startActivity(new Intent(context, MainActivity.class));
                                context.finish();

                            } catch (JSONException ex){
                                try {
                                    Log.i("LoginDataSource", "login attempt failed, message: " + response.getString("message"));
                                } catch (JSONException ex2){
                                    Log.i("LoginDataSource", "login attempt failed, no message");
                                }
                                Toast.makeText(LoginDataSource.this.context, "Login incorrect", Toast.LENGTH_LONG).show();
                            }
                            LoginDataSource.this.context.setLoadingProgressBar(false);
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error){
                            Toast.makeText(LoginDataSource.this.context, "Unknown error", Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                            context.setLoadingProgressBar(false);
                        }
                    }
            );

            network.addToQueue(jsonObjectRequest);
            //LoggedInUser user = new LoggedInUser(accessToken, username);

        } catch (Exception e) {
            //return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        //SharedPreferences sharedPreferences = getDefaultSharedPreferences(LoginDataSource.this.context);
        //sharedPreferences.edit().remove("accessToken").apply();
        NetworkSingleton.getInstance().setAccessToken(null);
    }

    public void setAccessToken(String userToken) {
        //SharedPreferences sharedPreferences = getDefaultSharedPreferences(LoginDataSource.this.context);
        //sharedPreferences.edit().putString("accessToken", userToken).apply();
        NetworkSingleton.getInstance().setAccessToken(userToken);
        Log.d("LoginDataSource", "set accessToken");
    }

    //public class LoginTask extends AsyncTask<String, Integer, Void> {
    //    @Override
    //    protected Void doInBackground(String... strings) {
    //
    //    }
    //
    //
    //}
}
