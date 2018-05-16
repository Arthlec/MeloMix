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
import java.util.Stack;

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

            private Stack genresStack = new Stack();
            private Stack artistsIDStack = new Stack();

            @Override
            public void run() {
                try {
                    this.setUserName();
                    String[] playlistsURLs = this.getPlaylistsURLs();

                    getArtistsStack("https://api.spotify.com/v1/me/tracks?limit=50&offset=0");

                    for(int i=0; i<playlistsURLs.length; i++) {
                        if(playlistsURLs[i] != null) {
                            getArtistsStack(playlistsURLs[i]);
                        }
                    }

                    int requestsNumber = artistsIDStack.size()/50;
                    for (int j=0; j<=requestsNumber; j++) {
                        String severalIDS = artistsIDStack.pop().toString();
                        for (int artistsCounter=0; artistsCounter<49; artistsCounter++) {
                            if(artistsIDStack.size() == 0) break;
                            severalIDS = severalIDS + "%2C" + artistsIDStack.pop().toString();
                        }
                        Log.i("Several IDS", severalIDS);
                        getMusicGenreList(severalIDS);
                    }

                    Log.i("Liste des genres", genresStack.toString());
                    Log.i("Nombre de genres", "" + genresStack.size());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                LoginActivitySpotify.asyncTaskIsDone = true;
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
                    LoginActivitySpotify.setUserName(name.asText());

                    myConnection.disconnect();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                }
            }

            private String[] getPlaylistsURLs() throws IOException {
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/me/playlists");

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    Log.i("AsyncTask", "Connexion réussie pour les playlists personnelles");
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    int playlistNumber = root.get("items").size();
                    String[] playlistURLsList = new String[playlistNumber];
                    String regex = "\\S+" + userName + "\\S+";
                    for (int i=0; i<playlistNumber; i++) {
                        JrsString playlistURL = (JrsString) root.get("items").get(i).get("tracks").get("href");
                        if(playlistURL.asText().matches(regex)) {
                            Log.i("Playlist_URL", playlistURL.asText());
                            JrsNumber totalPlaylistTracks = (JrsNumber) root.get("items").get(i).get("tracks").get("total");
                            Log.i("Nombre de musiques", totalPlaylistTracks.asText());
                            playlistURLsList[i] = playlistURL.asText();
                        }
                    }

                    myConnection.disconnect();
                    return playlistURLsList;
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                    return null;
                }
            }
            private Stack getArtistsStack(String tracksURL) throws IOException {
                // Create URL
                URL spotifyEndpoint = new URL(tracksURL);

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    Log.i("AsyncTask", "Connexion réussie pour la playlist demandée");
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    int tracksNumber = root.get("items").size();
                    for (int i=0; i<tracksNumber; i++) {
                        int artistsNumber = root.get("items").get(i).get("track").get("artists").size();
                        Log.i("Nombre d'artistes/music", "" + artistsNumber);
                        for (int j=0; j<artistsNumber; j++) {
                            JrsString artistID = (JrsString) root.get("items").get(i).get("track").get("artists").get(j).get("id");
                            if(artistID != null) {
                                Log.i("Artist_ID", artistID.asText());
                                artistsIDStack.push(artistID.asText());
                            }
                        }
                    }
                    Log.i("Artists_Stack", "" + artistsIDStack);
                    myConnection.disconnect();
                    JrsString nextTracksID = (JrsString) root.get("next");
                    if(nextTracksID != null) {
                        getArtistsStack(nextTracksID.asText());
                        Log.i("nextTracksID", nextTracksID.asText());
                    }
                    return artistsIDStack;
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                    return null;
                }
            }
            private void getMusicGenreList(String artistID) throws IOException {
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/artists?ids=" + artistID);

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    Log.i("AsyncTask", "Connexion réussie pour l'artiste demandé");
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    String jsonString = json.asString(root.get("artists"));
                    Log.i("ArtistsJsonString", jsonString);
                    int artistsNumber = root.get("artists").size();
                    Log.i("Artists_Number", "" + artistsNumber);
                    for (int i=0; i<artistsNumber; i++) {
                        int genresNumber = root.get("artists").get(i).get("genres").size();
                        Log.i("Genres_Number", "" + genresNumber);
                        if (genresNumber != 0) {
                            for (int j=0; j<genresNumber; j++) {
                                JrsString genre = (JrsString) root.get("artists").get(i).get("genres").get(j);
                                Log.i("Genre", genre.asText());
                                genresStack.push(genre.asText());
                            }
                        }
                    }

                    myConnection.disconnect();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                }
            }

            /*private List<String> getSavedTracksIds() throws IOException {
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
                            JrsString idTrack = (JrsString) listTracksIterator.next().get("track").get("artists").get("id");
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
            }*/
        });
    }

    private static void setUserName(String userName){
        LoginActivitySpotify.userName = userName;
    }
}
