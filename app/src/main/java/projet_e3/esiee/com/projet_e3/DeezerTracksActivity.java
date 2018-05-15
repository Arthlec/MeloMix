package projet_e3.esiee.com.projet_e3;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

    /** the tracks list adapter */
    private ArrayAdapter<Track> mTracksAdapter;

    // create the player track
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
     * Search for all tracks
     */
    private void getUserTracks() {

        DeezerRequest request = DeezerRequestFactory.requestCurrentUserCharts();
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect, new JsonRequestListener() {

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
                for (Track track : mTracksList) {
                    try {
                        //Store track in the JSONObject
                        //mObject.put(track.toString(), true);
                        mObject.put(track.getTitle(), true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("title", track.getTitle());
                }
                Log.i("Json", mObject.toString());
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
        task.execute(request);
    }
}
