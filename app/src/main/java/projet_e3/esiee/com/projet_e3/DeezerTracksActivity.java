package projet_e3.esiee.com.projet_e3;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeezerTracksActivity extends DeezerPlayerActivity {

    /**
     * The list of tracks of displayed by this activity.
     */
    private List<Track> mTracksList = new ArrayList<>();

    private TrackPlayer mTrackPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);
        setContentView(R.layout.deezer_tracks_activity);

        //build the player
        createPlayer();

        // fetch tracks list
        getUserTracks();
    }


    /**
     * Creates the PlaylistPlayer
     */
    private void createPlayer() {
        try {
            mTrackPlayer = new TrackPlayer(getApplication(), mDeezerConnect,
                    new WifiAndMobileNetworkStateChecker());
            //mTrackPlayer.addPlayerListener(this /*il faut mettre un listener*/);
            setAttachedPlayer(mTrackPlayer);
        }
       /*catch (OAuthException e) {
            handleError(e);
        }*/ catch (TooManyPlayersExceptions e) {
            handleError(e);
        } catch (DeezerError e) {
            handleError(e);
        }
    }

    /**
     * Search for all tracks splitted by genre
     */
    private void getUserTracks() {

        DeezerRequest request = DeezerRequestFactory.requestCurrentUserCharts();
        mDeezerConnect.requestAsync(request, new JsonRequestListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void onResult(final Object result, final Object requestId) {
                mTracksList.clear();

                try {
                    mTracksList.addAll((List<Track>) result);
                } catch (ClassCastException e) {
                    handleError(e);
                }

                if (mTracksList.isEmpty()) {
                    Toast.makeText(DeezerTracksActivity.this, "You don't have track", Toast.LENGTH_LONG).show();
                }

                // create Json object
                JSONObject mObject = new JSONObject();
                int m = 0;
                int n = 0;
                for (Track track : mTracksList) {
                    try {
                        mObject.put(track.toString(), m);
                        mObject.put(track.getTitle(), n);
                        //Log.i("Json", mObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    m++;
                    n++;
                    Log.d("title", track.getTitle());
                }
            }

            @Override
            public void onUnparsedResult(final String response, final Object requestId) {
                handleError(new DeezerError("Unparsed reponse"));
            }


            @Override
            public void onException(final Exception exception,
                                    final Object requestId) {
                handleError(exception);
            }
        });
    }
}
