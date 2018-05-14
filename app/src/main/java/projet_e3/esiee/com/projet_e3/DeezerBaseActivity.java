package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;


public class DeezerBaseActivity extends Activity {

    /**
     * DeezerConnect object used to auth
     */
    protected DeezerConnect mDeezerConnect = null;

    /**
     * Sample app Deezer appId.
     */
    public static final String SAMPLE_APP_ID = "279342";

    /**
     * DeezerConnect method used to disconnect
     */
    public void disconnectFromDeezer() {
        // if deezerConnect is still valid, clear all auth info
        if (mDeezerConnect != null) {
            mDeezerConnect.logout(this);
        }
        // also clear the session store
        new SessionStore().clear(this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeezerConnect = new DeezerConnect(this, SAMPLE_APP_ID);
    }

    /**
     * Handle errors by displaying a toast and logging.
     *
     * @param exception the exception that occured while contacting Deezer services.
     */
    protected void handleError(final Exception exception) {
        String message = exception.getMessage();
        if (TextUtils.isEmpty(message)) {
            message = exception.getClass().getName();
        }

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(Color.RED);
        toast.show();

        Log.e("BaseActivity", "Exception occured " + exception.getClass().getName(), exception);
    }
}
