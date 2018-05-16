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
import com.fasterxml.jackson.jr.stree.JrsString;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EmptyStackException;
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
    private static Stack genresStack = new Stack();
    private static Stack artistsIDStack = new Stack();

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
                    String[] playlistIds = this.getPlaylistsIds();
                    for(int i=0; i<playlistIds.length; i++) {
                        if(playlistIds[i] != null) {
                            Stack artistsIds = getArtistsStack(playlistIds[i]);
                            int requestsNumber = artistsIds.size()/50;
                            for (int j=0; j<=requestsNumber; j++) {
                                String severalIDS = artistsIds.pop().toString();
                                for (int artistsCounter=0; artistsCounter<49; artistsCounter++) {
                                    if(artistsIds.size() == 0) break;
                                    severalIDS = severalIDS + "%2C" + artistsIds.pop().toString();
                                }
                                Log.i("Several IDS", severalIDS);
                                getMusicGenreList(severalIDS);
                            }
                        }
                    }
                    Log.i("Liste des genres", genresStack.toString());
                    Log.i("GenresNumber", "" + genresStack.size());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                LoginActivitySpotify.asyncTaskIsDone = true;
            }

            private String getUserName() throws IOException {
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/me");

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    Log.i("AsyncTask", "Connexion réussie pour le nom de l'utilisateur");
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
                    return name.asText();
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                    return "";
                }
            }
            private String[] getPlaylistsIds() throws IOException {
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
                    String jsonString = json.asString(root);
                    int playlistNumber = root.get("items").size();
                    String[] playlistIdList = new String[playlistNumber];
                    String regex = "\\S+" + getUserName() + "\\S+";
                    Log.i("UserPlaylistJsonString", jsonString);
                    //Log.i("Nombre de playlists", "" + playlistNumber);
                    for (int i=0; i<playlistNumber; i++) {
                        JrsString playlistID = (JrsString) root.get("items").get(i).get("tracks").get("href");
                        if(playlistID.asText().matches(regex)) {
                            Log.i("Playlist_ID", playlistID.asText());
                            JrsNumber totalPlaylistTracks = (JrsNumber) root.get("items").get(i).get("tracks").get("total");
                            Log.i("Tracks/playlist", totalPlaylistTracks.asText());
                            playlistIdList[i] = playlistID.asText();
                        }
                    }

                    myConnection.disconnect();
                    return playlistIdList;
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                    return null;
                }
            }
            private Stack getArtistsStack(String playlistID) throws IOException {
                // Create URL
                URL spotifyEndpoint = new URL(playlistID);

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
                    String jsonString = json.asString(root);
                    int tracksNumber = root.get("items").size();
                    Log.i("Nombre de musiques", "" + tracksNumber);
                    Log.i("PlaylistJsonString", jsonString);
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
        });
    }

    private static void setUserName(String userName){
        LoginActivitySpotify.userName = userName;
    }
}
