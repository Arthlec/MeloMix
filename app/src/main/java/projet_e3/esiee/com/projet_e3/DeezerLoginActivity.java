package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.SessionStore;

public class DeezerLoginActivity extends DeezerMainActivity{

    /**
     * Permissions requested on Deezer accounts.
     * <p/>
     * cf : http://developers.deezer.com/api/permissions
     */
    protected static final String[] PERMISSIONS = new String[]{
            Permissions.BASIC_ACCESS, Permissions.OFFLINE_ACCESS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deezer_login_activity);

        new SessionStore().restore(mDeezerConnect, this);

        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
