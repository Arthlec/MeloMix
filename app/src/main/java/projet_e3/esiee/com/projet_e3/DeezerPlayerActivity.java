package projet_e3.esiee.com.projet_e3;

import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.player.PlayerWrapper;
import com.deezer.sdk.player.PlayerWrapper.RepeatMode;
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



public class DeezerPlayerActivity extends DeezerMainActivity {



    private PlayerHandler mPlayerHandler = new PlayerHandler();
    private OnClickHandler mOnClickHandler = new OnClickHandler();

    protected ImageButton mButtonPlayerStop;
    protected ImageButton mButtonPlayerPause;
    protected ImageButton mButtonPlayerSkipForward;
    protected ImageButton mButtonPlayerSkipBackward;
    protected ImageButton mButtonPlayerSeekBackward;
    protected ImageButton mButtonPlayerSeekForward;

    protected ImageButton mButtonPlayerRepeat;


    private SeekBar mSeekBar;
    private boolean mIsUserSeeking = false;
    private TextView mTextTime;
    private TextView mTextLength;

    private TextView mTextArtist;
    private TextView mTextTrack;

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
    // Click handler
    //////////////////////////////////////////////////////////////////////////////////////

    private class OnClickHandler implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            if (v == mButtonPlayerPause) {
                if (mPlayer.getPlayerState() == PlayerState.PLAYING) {
                    mPlayer.pause();
                } else {
                    mPlayer.play();
                }
            } else if (v == mButtonPlayerStop) {
                mPlayer.stop();
                //setPlayerVisible(false);
            } else if (v == mButtonPlayerSkipForward) {
                onSkipToNextTrack();
            } else if (v == mButtonPlayerSkipBackward) {
                onSkipToPreviousTrack();
            } else if (v == mButtonPlayerSeekBackward) {
                try {
                    mPlayer.seek(mPlayer.getPosition() - (10 * 1000));
                }
                catch (Exception e) {
                    handleError(e);
                }
            } else if (v == mButtonPlayerSeekForward) {
                try {
                    mPlayer.seek(mPlayer.getPosition() + (10 * 1000));
                }
                catch (Exception e) {
                    handleError(e);
                }
            } else if (v == mButtonPlayerRepeat) {
                switchRepeatMode();
            }
        }
    }

    protected void onSkipToNextTrack() {

    }

    protected void onSkipToPreviousTrack() {

    }

    protected void switchRepeatMode() {
        RepeatMode current = mPlayer.getRepeatMode();
        RepeatMode next;
        String toast;

        switch (current) {
            case NONE:
                next = RepeatMode.ONE;
                toast = "Repeat mode set to : Repeat One";
                break;
            case ONE:
                next = RepeatMode.ALL;
                toast = "Repeat mode set to : Repeat All";
                break;
            case ALL:
            default:
                next = RepeatMode.NONE;
                toast = "Repeat mode set to : No Repeat";
                break;
        }

        mPlayer.setRepeatMode(next);
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
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

