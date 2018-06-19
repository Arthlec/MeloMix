package projet_e3.esiee.com.projet_e3.Activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Scanner;

import projet_e3.esiee.com.projet_e3.R;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    private String defaut = "Veuillez entrer un nom d'utilisateur valide";
    private Button mEnter = null;
    private String MY_PREFS = "my_prefs";
    private String userName = null;
    private boolean agreed = false;


    EditText pseudo = null;
    TextView result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //splash screen
        setTheme(R.style.MeloTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        getResultsFromApi();

        if (myprefs_name() != null && myprefs_license()) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }

        //Récupération des vues
        pseudo = findViewById(R.id.pseudo);
        result = findViewById(R.id.result);

        pseudo.addTextChangedListener(textWatcher);

        /* use ENTER key on softkeyboard */

        pseudo.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("user_name", pseudo.getText().toString());
                            editor.apply();
                            if (myprefs_name() != null && myprefs_license()/*les conditions sont respectées*/) {
                                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                startActivity(intent);
                            } else if (!myprefs_license()) {
                                showDialogue();
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        mEnter = findViewById(R.id.enter);
        mEnter.setEnabled(false);
        mEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pView) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("user_name", pseudo.getText().toString());
                editor.apply();
                if (myprefs_name() != null && myprefs_license()/*les conditions sont respectées*/) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else if (!myprefs_license()) {
                    showDialogue();
                }
            }
        });
    }


    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().trim().length() == 0) {
                mEnter.setEnabled(false);
            } else {
                mEnter.setEnabled(true);
                result.setText(defaut);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void showDialogue() {

        /* Boite de dialogue
         * S'ouvre seulement au permier lancement de l'application
         */
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Conditions d'utilisation")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setPositiveButton("J'accepte", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("agreed", true);
                            editor.apply();
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Je refuse", null)
                    .setMessage(readFile())
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile() throws IOException {
        Scanner scanner = null;
        String text = "";
        InputStream inputStream = this.getResources().openRawResource(R.raw.license_agreement);
        try {
            scanner = new Scanner(inputStream);
        } catch (Exception e) {
            //Send error message.
        }
        if (scanner != null) {
            while (scanner.hasNext()) {
                String[] lines = scanner.nextLine().split("\t");
                for (String line : lines) {
                    text += line + "\n";
                }
            }
        }
        return text;
    }

    public String myprefs_name() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        userName = pref.getString("user_name", null);//null is the default value.
        return userName;
    }

    private boolean myprefs_license() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        agreed = pref.getBoolean("agreed", false);//false is the default value.
        return agreed;
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected String doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                cancel(true);
                mLastError = e;
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        public String getDataFromApi() throws IOException {
            SearchListResponse searchListResponse = mService.search().list("id")
                    .setMaxResults(Long.parseLong("1"))
                    .setQ("Camila Cabello Havana audio")
                    .setType("video")
                    .execute();
            String id = searchListResponse.getItems().get(0).getId().getVideoId();
            Log.i("reponse", id);

            return id;
        }


        @Override
        protected void onPreExecute() {
            //En cours de traitement
        }

        @Override
        protected void onPostExecute(String output) {
            if (output == null) {
                Toast.makeText(getApplicationContext(), "No results returned", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("Resultat", output);
            }
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.i("ERROR", "The following error occurred :" + mLastError.getMessage());
                }
            } else {
                Log.i("Status", "Request canceled");
            }
        }
    }
}
