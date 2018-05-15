package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.deezer.sdk.model.Permissions;
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
        if (mDeezerConnect.isSessionValid()) {
            new SessionStore().restore(mDeezerConnect, this);
        }

        //login the current user his Deezer account.
        Button buttonConnect = findViewById(R.id.button_login);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDeezer();
            }
        });

        //logout the current user his Deezer account.
        Button buttonDisconnect = findViewById(R.id.button_logout);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromDeezer();
            }
        });

        //get the album of the current user.
        Button buttonGetAlbum = findViewById(R.id.button_albulm);
        buttonGetAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeezerAlbumActivity.class);
                startActivity(intent);
            }
        });

        //get the most played tracks of the current user.
        Button buttonGetTracks = findViewById(R.id.button_tracks);
        buttonGetTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeezerTracksActivity.class);
                startActivity(intent);
            }
        });
    }

    //Show a toast who indicate if the user is connected.
    @Override
    protected void onResume() {
        super.onResume();

        if (mDeezerConnect.isSessionValid()) {
            Toast.makeText(MainActivity.this, mDeezerConnect.getCurrentUser().getName()+" you are connected to Deezer", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "You are not connected to Deezer", Toast.LENGTH_LONG).show();
        }
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

