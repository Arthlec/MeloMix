package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class LoginActivitySpotify extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {


    private static final String CLIENT_ID = "1aee09c9f4504604b379f867207fd238";
    private static final String REDIRECT_URI = "smooth-i://logincallback";

    private Player mPlayer;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(LoginActivitySpotify.this, "LoginActivitySpotify",
                Toast.LENGTH_LONG).show();

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-top-read", "user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        // The next 19 lines of the code are what you need to copy & paste! :)
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(LoginActivitySpotify.this);
                        mPlayer.addNotificationCallback(LoginActivitySpotify.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("LoginActivitySpotify", "Could not initialize player: " + throwable.getMessage());
                    }
                });

                Intent getBackToMainActivity = new Intent(LoginActivitySpotify.this, MainActivity.class);
                startActivity(getBackToMainActivity);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("LoginActivitySpotify", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            case kSpPlaybackNotifyPlay:
                TextView text = findViewById(R.id.text);
                String metadata = mPlayer.getMetadata().currentTrack.name;
                if (metadata == null)
                    text.setText("NULL");
                else
                    text.setText(metadata);
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("LoginActivitySpotify", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("LoginActivitySpotify", "User logged in");

        // This is the line that plays a song.
        mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("LoginActivitySpotify", "User logged out");
    }

    @Override
    public void onLoginFailed(Error var1) {
        Log.d("LoginActivitySpotify", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("LoginActivitySpotify", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("LoginActivitySpotify", "Received connection message: " + message);
    }
}
