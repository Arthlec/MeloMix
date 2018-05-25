package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.private_.TreeNode;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsNumber;
import com.fasterxml.jackson.jr.stree.JrsString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

import static junit.framework.Assert.assertTrue;

public class HostActivity extends Activity {

    ListView userList = null;
    private static Bitmap bmp;
    private static String trackName;
    private static String authToken = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_activity);
        authToken = getIntent().getStringExtra("authToken");
        Log.i("authToken", authToken);
        requestData();
        ImageView trackCover = findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmp);
        TextView trackNameField = findViewById(R.id.trackName);
        trackNameField.setText(trackName);
    }

    public void requestData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL trackURL = new URL(getTrackInfo()[0]);
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
