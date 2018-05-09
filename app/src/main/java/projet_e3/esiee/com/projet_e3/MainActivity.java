package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Spotify;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textSpotify = findViewById(R.id.textSpotify);
        //textSpotify.setText(this.getIntent().getStringExtra("message"));

        ImageButton logoSpotify = findViewById(R.id.imageButton);
        logoSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.this.isOnline()){
                    if((LoginActivitySpotify.mPlayer != null) && LoginActivitySpotify.mPlayer.isLoggedIn()){
                        Toast.makeText(MainActivity.this,"Compte déjà connecté", Toast.LENGTH_LONG).show();
                    }else{
                        Intent intent = new Intent(MainActivity.this, LoginActivitySpotify.class);
                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(MainActivity.this,"Aucune connexion internet détectée", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonDisconnect = findViewById(R.id.buttonDisconnect);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((LoginActivitySpotify.mPlayer != null) && LoginActivitySpotify.mPlayer.isLoggedIn()){
                    LoginActivitySpotify.mPlayer.logout();
                    Toast.makeText(MainActivity.this,"Déconnexion réussie", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this,"Aucun compte n'est connecté", Toast.LENGTH_LONG).show();
                }
                if((LoginActivitySpotify.mPlayer != null) && !LoginActivitySpotify.mPlayer.isTerminated())
                    Spotify.destroyPlayer(LoginActivitySpotify.mPlayer);
            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create URL
                    URL githubEndpoint = new URL("https://api.github.com/");

                    // Create connection
                    HttpsURLConnection myConnection = (HttpsURLConnection) githubEndpoint.openConnection();

                    myConnection.setRequestProperty("User-Agent", "smooth-i-app-v1.0");

                    if (myConnection.getResponseCode() == 200) {
                        // Success
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        jsonReader.beginObject(); // Start processing the JSON object
                        while (jsonReader.hasNext()) { // Loop through all keys
                            String key = jsonReader.nextName(); // Fetch the next key
                            if (key.equals("organization_url")) { // Check if desired key
                                // Fetch the value as a String
                                String value = jsonReader.nextString();

                                // Do something with the value
                                Log.i("Value", value);

                                break; // Break out of the loop
                            } else {
                                jsonReader.skipValue(); // Skip values of other keys
                            }
                        }
                        jsonReader.close();
                        myConnection.disconnect();
                    } else {
                        Log.i("responseCode", "" + myConnection.getResponseCode());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
