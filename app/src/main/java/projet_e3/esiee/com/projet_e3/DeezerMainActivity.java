package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;



public class DeezerMainActivity extends Activity {


    /**
     * DeezerConnect objet utilisé pour l'authentifiaction
     */
    protected DeezerConnect mDeezerConnect = null;

    /**
     * Sample app Deezer appId.
     */
    public static final String SAMPLE_APP_ID = "279342";

    /**
     * Permissions requested on Deezer accounts.
     * <p/>
     * cf : http://developers.deezer.com/api/permissions
     */
    protected static final String[] PERMISSIONS = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.OFFLINE_ACCESS,
            Permissions.MANAGE_LIBRARY,
            Permissions.LISTENING_HISTORY
    };

    // The listener for authentication events
    DialogListener listener = new DialogListener() {

        public void onComplete(Bundle values) {
            // store the current authentication info
            SessionStore sessionStore = new SessionStore();
            sessionStore.save(mDeezerConnect, DeezerMainActivity.this);
        }

        public void onCancel() {
        }

        public void onException(Exception e) {
            Log.e("ERROR", "Hey! Error to connect to Deezer!");
        }
    };

    private void disconnectFromDeezer() {
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
        setContentView(R.layout.deezer_main_activity);

        //authentification
        if (mDeezerConnect == null) {
            mDeezerConnect = new DeezerConnect(this, SAMPLE_APP_ID);
            mDeezerConnect.authorize(this, PERMISSIONS, listener);
        } else {
            //restore authentification
            new SessionStore().restore(mDeezerConnect, this);
        }
        Button buttonLogin = (Button) findViewById(R.id.button_disconnect);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromDeezer();
                Intent intent = new Intent(DeezerMainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        Button buttonAlbum = (Button) findViewById(R.id.button_albulm);
        buttonAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeezerMainActivity.this, DeezerAlbumActivity.class);
                startActivity(intent);
            }
        });

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
