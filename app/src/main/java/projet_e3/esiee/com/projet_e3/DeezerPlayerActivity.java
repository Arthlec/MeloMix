package projet_e3.esiee.com.projet_e3;

import android.widget.SeekBar;

import com.deezer.sdk.player.PlayerWrapper;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.exception.NotAllowedToPlayThatSongException;
import com.deezer.sdk.player.exception.StreamLimitationException;


public class DeezerPlayerActivity extends DeezerBaseActivity {


    private PlayerHandler mPlayerHandler = new PlayerHandler();
    private SeekBar mSeekBar;
    private PlayerWrapper mPlayer;

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);

    }


    @Override
    protected void onDestroy() {
        doDestroyPlayer();
        super.onDestroy();
    }

    /**
     * Will destroy player. Subclasses can override this hook.
     */
    protected void doDestroyPlayer() {

        if (mPlayer == null) {
            // No player, ignore
            return;
        }

        if (mPlayer.getPlayerState() == PlayerState.RELEASED) {
            // already released, ignore
            return;
        }

        // first, stop the player if it is not
        if (mPlayer.getPlayerState() != PlayerState.STOPPED) {
            mPlayer.stop();
        }

        // then release it
        mPlayer.release();
    }

    protected void setAttachedPlayer(final PlayerWrapper player) {
        mPlayer = player;
        player.addOnBufferErrorListener(mPlayerHandler);
        player.addOnBufferStateChangeListener(mPlayerHandler);
        player.addOnBufferProgressListener(mPlayerHandler);

        player.addOnPlayerErrorListener(mPlayerHandler);
        player.addOnPlayerStateChangeListener(mPlayerHandler);
        player.addOnPlayerProgressListener(mPlayerHandler);

        if (mPlayer.isAllowedToSeek()) {
            mSeekBar.setEnabled(true);
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////
    // Player Handler
    //////////////////////////////////////////////////////////////////////////////////////


    /**
     * Handler for messages sent by the player and buffer
     */
    private class PlayerHandler
            implements
            OnPlayerProgressListener,
            OnBufferProgressListener,
            OnPlayerStateChangeListener,
            OnPlayerErrorListener,
            OnBufferStateChangeListener,
            OnBufferErrorListener {

        @Override
        public void onBufferError(final Exception ex, final double percent) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    handleError(ex);
                }
            });
        }

        @Override
        public void onBufferStateChange(final BufferState state, final double percent) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onPlayerError(final Exception ex, final long timePosition) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    handleError(ex);
                    if (ex instanceof NotAllowedToPlayThatSongException) {
                        mPlayer.skipToNextTrack();
                    } else if (ex instanceof StreamLimitationException) {
                        // Do nothing ,
                    } else {
                        finish();
                    }
                }
            });
        }

        @Override
        public void onPlayerStateChange(final PlayerState state, final long timePosition) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onBufferProgress(final double percent) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onPlayerProgress(final long timePosition) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                }
            });
        }
    }
}

