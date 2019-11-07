package net.sakuragasaki46.coriplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.sakuragasaki46.coriplus.data.LoginDataSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class NetworkSingleton {
    private static final NetworkSingleton ourInstance = new NetworkSingleton();
    private RequestQueue queue;
    private String accessToken = null;
    private Context context;

    public static NetworkSingleton getInstance() {
        return ourInstance;
    }

    private NetworkSingleton() {
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void createQueue(){
        if (queue == null){
            queue = Volley.newRequestQueue(context);
        }
    }

    public void addToQueue(Request request) {
        queue.add(request);
    }

    public String createUrl(String path){
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        String host = sharedPreferences.getString("host", null);
        Log.d("createUrl", "host is " + (host != null? host : "null"));
        if(host == null || host.equals("development")){
            return "http://10.0.2.2:5000" + path;
        }
        return "https://" + host.split("/")[0] + path;
    }

    public String getAccessToken(){
        if(accessToken == null){
            File file = new File(context.getFilesDir(), "accessToken");
            try {
                BufferedReader fileReader = new BufferedReader(new FileReader(file));
                accessToken = fileReader.readLine();
                fileReader.close();
            }catch(IOException e){
                Log.d("getAccessToken", "accessToken is null");
                return null;
            }
        }
        return accessToken;
    }

    public void setAccessToken(String token){
        accessToken = token;
        File file = new File(context.getFilesDir(), "accessToken");
        if(token == null){
            file.delete();
        } else {
            try {
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
                fileWriter.write(token);
                fileWriter.close();
            } catch (IOException e) {
                Log.e("setAccessToken", "couldn't write");
            }
        }
    }

    public String createApiUrl(String path){
        String accessToken = getAccessToken();
        accessToken = accessToken != null? accessToken : "";
        return createUrl("/api/V1/" + path + "?access_token=" + accessToken);
    }

    public String getUserId() {
        try {
            return getAccessToken().split(":")[0];
        }catch(NullPointerException ex){
            return null;
        }
    }
}
