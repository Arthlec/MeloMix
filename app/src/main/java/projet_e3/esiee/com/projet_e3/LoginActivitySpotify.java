package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.private_.TreeNode;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsString;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static junit.framework.Assert.assertTrue;

public class LoginActivitySpotify extends AppCompatActivity {


    private static final String CLIENT_ID = "1aee09c9f4504604b379f867207fd238";
    private static final String REDIRECT_URI = "smooth-i://logincallback";
    private static String authToken = "";
    private static String userName = "";
    private static boolean asyncTaskIsDone = false;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI).setShowDialog(true);
        builder.setScopes(new String[]{"user-top-read", "user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(LoginActivitySpotify.this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Intent getBackToMainActivity = new Intent(LoginActivitySpotify.this, MainActivity.class);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    authToken = response.getAccessToken();
                    requestData();
                    MainActivity.isLoggedInSpotify = true;
                    while(!asyncTaskIsDone){
                        try { Thread.sleep(100); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                    Toast.makeText(LoginActivitySpotify.this,"Connexion réussie", Toast.LENGTH_LONG).show();
                    getBackToMainActivity.putExtra("userName", "Connecté avec le compte : " + LoginActivitySpotify.userName);
                    startActivity(getBackToMainActivity);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    AuthenticationClient.stopLoginActivity(LoginActivitySpotify.this, REQUEST_CODE);
                    Toast.makeText(LoginActivitySpotify.this,"Erreur de connexion", Toast.LENGTH_LONG).show();
                    Log.i("Connection", "ERROR");
                    startActivity(getBackToMainActivity);
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    AuthenticationClient.stopLoginActivity(LoginActivitySpotify.this, REQUEST_CODE);
                    Toast.makeText(LoginActivitySpotify.this,"Connexion annulée", Toast.LENGTH_LONG).show();
                    Log.i("Connection", "Cancelled");
                    startActivity(getBackToMainActivity);
            }
        }
    }

    public static void requestData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    this.getUserName();
                    //this.getSavedTracks();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                LoginActivitySpotify.asyncTaskIsDone = true;
            }

            private void getUserName() throws IOException {
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/me");

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    Log.i("AsyncTask", "Connection réussie pour le nom de l'utilisateur");
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    String jsonString = json.asString(root);
                    Log.i("jsonString", jsonString);
                    JrsString name = (JrsString) root.get("id");
                    Log.i("Display_name", name.asText());
                    LoginActivitySpotify.setUserName(name.asText());

                    myConnection.disconnect();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                }
            }
            private void getSavedTracks() throws IOException {
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/me/tracks");

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    Log.i("AsyncTask", "Connection réussie pour les musiques sauvegardées");
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    String jsonString = json.asString(root);
                    Log.i("jsonString", jsonString);
                    JrsString name = (JrsString) root.get("id");
                    Log.i("Saved_tracks", name.asText());
                    LoginActivitySpotify.setUserName(name.asText());

                    myConnection.disconnect();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                }
            }
        });
    }

    private static void setUserName(String userName){
        LoginActivitySpotify.userName = userName;
    }
}
