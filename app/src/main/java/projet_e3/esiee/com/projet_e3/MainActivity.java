package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

public class MainActivity extends DeezerBaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // restore auth
        if (mDeezerConnect != null) {
            new SessionStore().restore(mDeezerConnect, this);
        }

        Button buttonConnect = (Button) findViewById(R.id.button_login);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDeezer();
            }
        });

        Button buttonDisconnect = (Button) findViewById(R.id.button_logout);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromDeezer();
            }
        });
        Button buttonGetAlbum = (Button) findViewById(R.id.button_albulm);
        buttonGetAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeezerAlbumActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Asks the SDK to display a log in dialog for the user
     */
    private void connectToDeezer() {
        mDeezerConnect.authorize(this, PERMISSIONS, listener);
    }


    /**
     * The listener for authentication events
     */
    DialogListener listener = new DialogListener() {

        public void onComplete(Bundle values) {
            // store the current authentication info
            SessionStore sessionStore = new SessionStore();
            sessionStore.save(mDeezerConnect, MainActivity.this);
        }

        public void onCancel() {
        }

        public void onException(Exception e) {
            Log.e("ERROR", "Hey! Fail to connect to Deezer!");
        }
    };
}

