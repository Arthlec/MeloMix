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

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.List;


public class DeezerMainActivity extends Activity {


    /** DeezerConnect objet utilisé pour l'authentifiaction */
    protected DeezerConnect mDeezerConnect = null;

    /** Sample app Deezer appId. */
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

        public void onComplete(Bundle values) {}

        public void onCancel() {}

        public void onException(Exception e) {
           Log.e("ERROR","Hey! Error to connect to Deezer!");
        }
    };

    // the request listener
    RequestListener nListener = new JsonRequestListener() {

        public void onResult(Object result, Object requestId) {
            List<Album> albums = (List<Album>) result;
            Log.i("DATA",albums.toString());


            public void onUnparsedResult(String requestResponse, Object requestId) {}

            public void onException(Exception e, Object requestId) {}
        }

    /* SessionStore sessionStore = new SessionStore();
    sessionStore.save(DeezerConnect, Context);

    SessionStore sessionStore = new SessionStore();
    if (sessionStore.restore(deezerConnect, context)) {
        // The restored session is valid, navigate to the Home Activity
        Intent intent = new Intent(context, HomeActivity.class);
        startActivity(intent);
    }*/

    private final void disconnectFromDeezer() {
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

        mDeezerConnect = new DeezerConnect(this, SAMPLE_APP_ID);
        mDeezerConnect.authorize(this, PERMISSIONS, listener);

        Button button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromDeezer();
                Intent intent = new Intent(DeezerMainActivity.this, MainActivity.class);
                startActivity(intent);
            }
    }



    /**
     * Handle errors by displaying a toast and logging.
     *
     * @param exception
     *            the exception that occured while contacting Deezer services.
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
