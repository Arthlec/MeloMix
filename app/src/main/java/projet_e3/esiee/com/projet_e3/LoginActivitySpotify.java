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
import com.fasterxml.jackson.jr.stree.JrsArray;
import com.fasterxml.jackson.jr.stree.JrsNumber;
import com.fasterxml.jackson.jr.stree.JrsObject;
import com.fasterxml.jackson.jr.stree.JrsString;
import com.fasterxml.jackson.jr.stree.JrsValue;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        builder.setScopes(new String[]{"user-top-read", "user-library-read", "playlist-read-private"});
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
                    this.getTrackGenre(this.getSavedTracksIds());
                } catch (Exception e) {
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
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    JrsString name = (JrsString) root.get("id");
                    Log.i("Display_name", name.asText());
                    LoginActivitySpotify.setUserName(name.asText());

                    myConnection.disconnect();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                }
            }
            private void getTrackGenre(List<String> idsTracksList) throws IOException {
                String[] idsTracksFull = idsTracksList.toArray(new String[idsTracksList.size()]);
                int indiceMax = 50;
                for(int i =0; i<idsTracksList.size();i+50){

                }
                String[] idsTracks = idsTracksFull[]
                List<String> trackGenres = new ArrayList<>();
                // Create URL
                String request = "https://api.spotify.com/v1/tracks?ids=";
                for (int i=0; i<idsTracks.length-1; i++){ //s'arrête à l'avant-dernier élément pour ne pas mettre de virgule après
                    request += idsTracks[i] + "%2C"; //le "%2C" correspond à la virgule entre les ids des musiques
                }
                request += idsTracks[idsTracks.length-1]; //ajout du dernier élément
                URL spotifyEndpoint = new URL(request);

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    for(int m = 0; m< idsTracks.length; m++) {
                        JrsArray artistsArray = (JrsArray) root.get("tracks").get(m).get("album").get("artists");
                        Iterator<JrsValue> artistsIterator = artistsArray.elements();
                        for (int i = 0; artistsIterator.hasNext(); i++) {
                            JrsString id = (JrsString) artistsIterator.next().get("id");
                            String[] artistGenres = this.getArtistGenre(id.asText());
                            for (String artistGenre : artistGenres) {
                                trackGenres.add(artistGenre);
                                Log.i("trackGenres", artistGenre);
                            }
                        }
                    }

                    myConnection.disconnect();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                }
            }

            private String[] getArtistGenre(String idArtist) throws IOException {
                String[] artistGenres = null;
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/artists/" + idArtist);

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    JrsArray listGenresArray = (JrsArray) root.get("genres");
                    Iterator<JrsValue> listGenresIterator = listGenresArray.elements();
                    artistGenres = new String[listGenresArray.size()];
                    for(int i = 0; listGenresIterator.hasNext(); i++){
                        artistGenres[i] = listGenresIterator.next().asText();
                        Log.i("artist", artistGenres[i]);
                    }

                    myConnection.disconnect();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                }
                return artistGenres;
            }

            private List<String> getSavedTracksIds() throws IOException {
                // Create URL
                List<String> listTracksIds = new ArrayList<>();
                String request = "https://api.spotify.com/v1/me/tracks?limit=50&offset=0";
                JrsString nextRequest = null;
                do{
                    URL spotifyEndpoint = new URL(request);

                    // Create connection
                    HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                    myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                    if (myConnection.getResponseCode() == 200) {
                        // Success
                        InputStream responseBody = myConnection.getInputStream();

                        JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                        TreeNode root = json.treeFrom(responseBody);
                        assertTrue(root.isObject());
                        JrsArray listTracksArray = (JrsArray) root.get("items");
                        Iterator<JrsValue> listTracksIterator = listTracksArray.elements();
                        for(int i = 0; listTracksIterator.hasNext(); i++){
                            JrsString idTrack = (JrsString) listTracksIterator.next().get("track").get("id");
                            listTracksIds.add(idTrack.asText());
                            //Log.i("i", "" + i);
                        }
                        myConnection.disconnect();

                        nextRequest = (JrsString) root.get("next");
                        if(nextRequest != null)
                            request = nextRequest.asText();
                    } else {
                        Log.i("responseCode", "" + myConnection.getResponseCode());
                    }
                }while (nextRequest != null);
                return listTracksIds;
            }
        });
    }

    private static void setUserName(String userName){
        LoginActivitySpotify.userName = userName;
    }
}
