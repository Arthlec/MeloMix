package projet_e3.esiee.com.projet_e3;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.private_.TreeNode;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import static junit.framework.Assert.assertTrue;

public class HostActivity extends AppCompatActivity {

    private static Bitmap bmp;
    private static String trackName;
    private static String authToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_activity);



        int host = getIntent().getIntExtra("host", 0);
        authToken = getIntent().getStringExtra("authToken");
        Log.i("authToken", authToken);
        trackName = "Marche po";
        ImageView trackCover = findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmp);
        TextView trackNameField = findViewById(R.id.trackName);
        trackNameField.setText(trackName);
        ImageView nextTrackCover = findViewById(R.id.nextTrackCover);
        nextTrackCover.setImageBitmap(bmp);
        TextView nextTrackNameField = findViewById(R.id.nextTrackName);
        nextTrackNameField.setText(trackName);

        RequestClass requestClass = new RequestClass(authToken, trackCover, nextTrackCover, trackNameField, nextTrackNameField);
        requestClass.start();

        Button buttonAdd = findViewById(R.id.moreButton);
        buttonAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ajoute la musique actuelle aux musiques personnel
            }
        });

        Button buttonDislike = findViewById(R.id.dislikeButton);
        buttonDislike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //dislike la musique actuelle
            }
        });

        Button buttonLike = findViewById(R.id.likeButton);
        buttonLike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //like la musique actuelle
            }
        });


        Button buttonNext = findViewById(R.id.buttonNext);
        Button buttonBack = findViewById(R.id.buttonBack);
        if (/*Si c'est un guest*/ host == 0) {
            buttonNext.setVisibility(View.GONE);
            buttonNext.setActivated(false);
            buttonBack.setVisibility(View.GONE);
            buttonBack.setActivated(false);

        } else if (/*Si c'est un host*/host == 1) {
            buttonNext.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //passe a la musique suivante
                }
            });

            buttonBack.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //revient a la musique precedente
                }
            });
        }
    }
public static class RequestAsyncTask extends AsyncTask<String,String,String>{
        private static Bitmap bmp;
        private static String trackName;
        private static String authToken = "";
        @SuppressLint("StaticFieldLeak")
        private ImageView trackCover;
        @SuppressLint("StaticFieldLeak")
        private TextView trackNameField;
        @SuppressLint("StaticFieldLeak")
        private ImageView nextTrackCover;
        @SuppressLint("StaticFieldLeak")
        private TextView nextTrackNameField;

        RequestAsyncTask(String pTok, ImageView imageView, ImageView pNext, TextView textView, TextView pText) {
            authToken = pTok;
            trackCover = imageView;
            trackNameField = textView;
            nextTrackCover = pNext;
            nextTrackNameField = pText;
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL trackURL = new URL(Objects.requireNonNull(getTrackInfo())[0]);
                bmp = BitmapFactory.decodeStream(trackURL.openConnection().getInputStream());
                trackName = getTrackInfo()[1];
                return "bmp";
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s!=null){
                trackCover.setImageBitmap(bmp);
                trackNameField.setText(trackName);
                nextTrackCover.setImageBitmap(bmp);
                nextTrackNameField.setText(trackName);
                if (!TextUtils.isEmpty(s)) {
                   RequestAsyncTask requestClass = new RequestAsyncTask(authToken,trackCover,nextTrackCover,trackNameField,nextTrackNameField);
                   requestClass.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});
                }
            }
        }

    private String[] getTrackInfo() throws IOException {
        String[] trackInfo = new String[2];
        // Create URL
        URL spotifyEndpoint = new URL("https://api.spotify.com/v1/tracks/11dFghVXANMlKmJXsNCbNl");

        // Create connection
        HttpsURLConnection myConnection;
        String waitTime;
        do{
            myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
            myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
            waitTime = myConnection.getHeaderField("Retry-After");
            if(waitTime != null){
                int waitTimeSeconds = Integer.parseInt(waitTime);
                try {
                    Thread.sleep(waitTimeSeconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("WaitTime", waitTime);
            }
        }while(waitTime != null);

        if (myConnection.getResponseCode() == 200) {
            // Success
            InputStream responseBody = myConnection.getInputStream();

            JSON json = JSON.std.with(new JacksonJrsTreeCodec());
            TreeNode root = json.treeFrom(responseBody);
            assertTrue(root.isObject());
            JrsString imageURL = (JrsString) root.get("album").get("images").get(0).get("url");
            JrsString trackName = (JrsString) root.get("album").get("name");
            trackInfo[0] = imageURL.asText();
            trackInfo[1] = trackName.asText();

            myConnection.disconnect();
            return trackInfo;
        } else {
            Log.i("responseCode", "" + myConnection.getResponseCode());
            return null;
        }
    }

}

    public void requestData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL trackURL = new URL(Objects.requireNonNull(getTrackInfo())[0]);
                    bmp = BitmapFactory.decodeStream(trackURL.openConnection().getInputStream());
                    trackName = getTrackInfo()[1];
                    new Thread(new Runnable() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView trackCover = findViewById(R.id.trackCover);
                                    trackCover.setImageBitmap(bmp);
                                    TextView trackNameField = findViewById(R.id.trackName);
                                    trackNameField.setText(trackName);
                                    ImageView nextTrackCover = findViewById(R.id.nextTrackCover);
                                    nextTrackCover.setImageBitmap(bmp);
                                    TextView nextTrackNameField = findViewById(R.id.nextTrackName);
                                    nextTrackNameField.setText(trackName);
                                }
                            });
                        }
                    }).start();
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private String[] getTrackInfo() throws IOException {
                String[] trackInfo = new String[2];
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/tracks/11dFghVXANMlKmJXsNCbNl");

                // Create connection
                HttpsURLConnection myConnection;
                String waitTime;
                do{
                    myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                    myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                    waitTime = myConnection.getHeaderField("Retry-After");
                    if(waitTime != null){
                        int waitTimeSeconds = Integer.parseInt(waitTime);
                        try {
                            Thread.sleep(waitTimeSeconds * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.i("WaitTime", waitTime);
                    }
                }while(waitTime != null);

                if (myConnection.getResponseCode() == 200) {
                    // Success
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    JrsString imageURL = (JrsString) root.get("album").get("images").get(0).get("url");
                    JrsString trackName = (JrsString) root.get("album").get("name");
                    trackInfo[0] = imageURL.asText();
                    trackInfo[1] = trackName.asText();

                    myConnection.disconnect();
                    return trackInfo;
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                    return null;
                }
            }
        });
    }
}
