package net.sakuragasaki46.coriplus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class NetworkImageFetcher {
    private static final NetworkImageFetcher ourInstance = new NetworkImageFetcher();
    private Context context;
    private File cacheDir;
    private static int counter = 1;
    public static final int REQUEST_DOWNLOAD_IMAGE = 10002;

    private NetworkImageFetcher(){ }

    public static NetworkImageFetcher getInstance() {
        return ourInstance;
    }

    public void setContext(Context context){
        this.context = context;
        cacheDir = new File(context.getCacheDir(), "images");
        Log.d("NetworkImageFetcher", "cacheDir create result " + cacheDir.mkdirs());
    }

    public void startDownloadImage(ImageView imageView, String imageUrl){
        new DownloadImageTask(imageView).execute(imageUrl);
    }

    public void startSaveImage(String imageUrl){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("SaveImageTask", "requesting permissions...");
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_DOWNLOAD_IMAGE);
        }
        Log.d("SaveImageTask", "save path created " + getSavePath().mkdirs());
        Log.d("SaveImageTask", "save path " + (getSavePath().exists()? "exists": "not exists"));
        new SaveImageTask().execute(imageUrl);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        // TODO cache this
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            // first look for the cached file
            File cachedFile = new File(cacheDir, getHexMd5(urlDisplay) + ".clean");
            Log.d("DownloadImageTask", "cacheDir is " + cacheDir);
            try {
                InputStream in;
                if(!cachedFile.exists()) {
                    Log.d("DownloadImageTask", "cache miss");
                    in = new java.net.URL(urlDisplay).openStream();
                    OutputStream out = new FileOutputStream(cachedFile);
                    byte[] buffer = new byte[2048];
                    int length;

                    while ((length = in.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();
                }
                in = new FileInputStream(cachedFile);
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("DownloadImageTask", e.getMessage());
                e.printStackTrace();
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private class SaveImageTask extends AsyncTask<String, Void, Void> {
        // TODO cache this
        ImageView bmImage;

        protected Void doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            // first look for the cached file
            File cachedFile = new File(cacheDir, getHexMd5(urlDisplay) + ".clean");
            try {
                InputStream in;
                OutputStream out;
                byte[] buffer = new byte[2048];
                int length;
                if(!cachedFile.exists()) {
                    Log.d("SaveImageTask", "cache miss");
                    in = new java.net.URL(urlDisplay).openStream();
                    out = new FileOutputStream(cachedFile);

                    while ((length = in.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();
                }
                in = new FileInputStream(cachedFile);
                out = new FileOutputStream(new File(getSavePath(), System.currentTimeMillis() + getFileExtension(urlDisplay)));

                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
            } catch (Exception e) {
                Log.e("SaveImageTask", e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            Toast.makeText(context, R.string.image_save_success, Toast.LENGTH_LONG).show();
        }
    }

    private String getFileExtension(String urlDisplay) {
        String[] urlParts = urlDisplay.split("/");
        String lastPart = urlParts[urlParts.length - 1];
        int lastIndex = lastPart.lastIndexOf(".");
        if (lastIndex == -1) {
            return "";
        }
        return lastPart.substring(lastIndex);
    }

    private File getSavePath() {
        return new File(Environment.getExternalStorageDirectory().getPath(), "Cori+");
    }

    private String getHexMd5(String url) {
        try {
            return bytesToHex(MessageDigest.getInstance("MD5").digest(url.getBytes()));
        } catch(Exception ex){
            // fall back on a counter
            return String.valueOf(counter++);
        }
    }

    // https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
