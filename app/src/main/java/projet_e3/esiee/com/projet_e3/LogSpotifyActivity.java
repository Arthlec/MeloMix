package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class LogSpotifyActivity extends Activity {

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
        builder.setScopes(new String[]{"user-top-read", "user-library-read", "playlist-read-private"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(LogSpotifyActivity.this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Intent getBackToLogActivity = new Intent(LogSpotifyActivity.this, LogActivity.class);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    authToken = response.getAccessToken();
                    //requestData();
                    MainActivity.isLoggedInSpotify = true;
                    while(!asyncTaskIsDone){
                        try { Thread.sleep(100); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                    Toast.makeText(LogSpotifyActivity.this,"Connexion réussie", Toast.LENGTH_LONG).show();
                    getBackToLogActivity.putExtra("userName", "Connecté avec le compte : " + LogSpotifyActivity.userName);
                    startActivity(getBackToLogActivity);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    AuthenticationClient.stopLoginActivity(LogSpotifyActivity.this, REQUEST_CODE);
                    Toast.makeText(LogSpotifyActivity.this,"Erreur de connexion", Toast.LENGTH_LONG).show();
                    Log.i("Connection", "ERROR");
                    startActivity(getBackToLogActivity);
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    AuthenticationClient.stopLoginActivity(LogSpotifyActivity.this, REQUEST_CODE);
                    Toast.makeText(LogSpotifyActivity.this,"Connexion annulée", Toast.LENGTH_LONG).show();
                    Log.i("Connection", "Cancelled");
                    startActivity(getBackToLogActivity);
            }
        }
    }
    private void setUserName() throws IOException {
        // Create URL
        URL spotifyEndpoint = new URL("https://api.spotify.com/v1/me");

        // Create connection
        HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
        myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
        if (myConnection.getResponseCode() == 200) {
            // Success
            InputStream responseBody = myConnection.getInputStream();

            JSON json = JSON.std.with(new JacksonJrsTreeCodec());
            TreeNode root = json.treeFrom(responseBody);
            assertTrue(root.isObject());
            JrsString name = (JrsString) root.get("id");
            Log.i("Display_name", name.asText());
            LogSpotifyActivity.setUserName(name.asText());

            myConnection.disconnect();
        } else {
            Log.i("responseCode", "" + myConnection.getResponseCode());
        }
    }

    private static void setUserName(String userName){
        LogSpotifyActivity.userName = userName;
    }
}
