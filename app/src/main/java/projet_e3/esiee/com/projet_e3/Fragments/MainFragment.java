package projet_e3.esiee.com.projet_e3.Fragments;


import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.transition.Scene;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import projet_e3.esiee.com.projet_e3.Activities.MainActivity;
import projet_e3.esiee.com.projet_e3.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchListResponse;

import java.io.IOException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private YouTubePlayer YPlayer;
    private static final String YoutubeDeveloperKey = "0D:22:BC:A1:22:C5:0F:6B:BD:42:9D:FE:31:81:05:BE:92:12:BD:84";
    private static final int RECOVERY_DIALOG_REQUEST = 1;


    private int host;
    private Scene basicScene;
    private Scene dislikeScene;
    private Scene likeScene;
    private static View view;
    private static Bitmap bmpCover;
    private static Bitmap bmpNextCover;
    private static String name;
    private static String nextName;
    private static boolean songLiked = false;
    private OnSavedMusicSelectedListener mSavedMusicListener;
    private OnArrowListener mArrowListener;
    GoogleAccountCredential mCredential;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    public static MainFragment newInstance() {
        return (new MainFragment());
    }

    public interface OnSavedMusicSelectedListener {
        void onSavedMusicSelected();
    }

    public interface OnArrowListener {
        void onArrowSelected(String direction);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        host = getActivity().getIntent().getIntExtra("host", 0);

        mCredential = MainActivity.mCredential;

        if(host == 1)
            getResultsFromApi();

        final ViewGroup mSceneRoot = view.findViewById(R.id.like_dislike_root);
        basicScene = Scene.getSceneForLayout(mSceneRoot, R.layout.basic_like_dislike, getContext());
        dislikeScene = Scene.getSceneForLayout(mSceneRoot, R.layout.final_dislike, getContext());
        likeScene = Scene.getSceneForLayout(mSceneRoot, R.layout.final_like, getContext());

        ImageView trackCover = view.findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmpCover);
        TextView trackNameField = view.findViewById(R.id.trackName);
        trackNameField.setText(name);
        ImageView nextTrackCover = view.findViewById(R.id.nextTrackCover);
        nextTrackCover.setImageBitmap(bmpNextCover);
        TextView nextTrackNameField = view.findViewById(R.id.nextTrackName);
        nextTrackNameField.setText(nextName);
        ProgressBar nextTrackLoading = view.findViewById(R.id.nextTrackLoading);
        nextTrackLoading.setVisibility(View.INVISIBLE);
        nextTrackLoading.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

        if(songLiked){
            mSceneRoot.findViewById(R.id.likeButton).setVisibility(View.INVISIBLE);
            mSceneRoot.findViewById(R.id.dislikeButton).setVisibility(View.INVISIBLE);
        }

        setListeners();

        return view;
    }

    public void setListeners() {
        final ViewGroup mSceneRoot = view.findViewById(R.id.like_dislike_root);
        final android.transition.Transition mLikeDislikeTransition =
                TransitionInflater.from(getContext()).
                        inflateTransition(R.transition.like_dislike_transition);
        mLikeDislikeTransition.setDuration(1500);
        final android.transition.Transition mFadeTransition =
                TransitionInflater.from(getContext()).
                        inflateTransition(R.transition.fade_transition);
        mLikeDislikeTransition.setDuration(800);

        Button buttonAdd = view.findViewById(R.id.moreButton);
        buttonAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ajoute la musique actuelle aux musiques personnelles
                mSavedMusicListener.onSavedMusicSelected();
            }
        });

        final Button buttonDislike = mSceneRoot.findViewById(R.id.dislikeButton);
        buttonDislike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //dislike la musique actuelle
                TransitionManager.go(dislikeScene, mLikeDislikeTransition);
                Toast.makeText(getActivity().getApplicationContext(), "Votre avis a été pris en compte", Toast.LENGTH_SHORT).show();
                songLiked = true;
                setListeners();
            }
        });
        Button buttonLike = mSceneRoot.findViewById(R.id.likeButton);
        buttonLike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //like la musique actuelle
                TransitionManager.go(likeScene, mLikeDislikeTransition);
                Toast.makeText(getActivity().getApplicationContext(), "Votre avis a été pris en compte", Toast.LENGTH_SHORT).show();
                songLiked = true;
                setListeners();
            }
        });


        Button buttonNext = view.findViewById(R.id.buttonNext);
        Button buttonBack = view.findViewById(R.id.buttonBack);
        if (/*Si c'est un guest*/ host == 0) {
            buttonNext.setVisibility(View.GONE);
            buttonNext.setActivated(false);
            buttonBack.setVisibility(View.GONE);
            buttonBack.setActivated(false);

        } else if (/*Si c'est un host*/host == 1) {
            buttonNext.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //passe a la musique suivante
                    YPlayer.release();
                    mArrowListener.onArrowSelected("next");
                    TransitionManager.go(basicScene, mFadeTransition);
                    songLiked = false;
                    setListeners();
                    getResultsFromApi();
                }
            });

            buttonBack.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //revient a la musique precedente
                    YPlayer.release();
                    mArrowListener.onArrowSelected("back");
                    TransitionManager.go(basicScene, mFadeTransition);
                    songLiked = false;
                    setListeners();
                    getResultsFromApi();
                }
            });
        }
    }

    public void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(getContext(), "No network connection available", Toast.LENGTH_SHORT).show();
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
                getContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
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
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != getActivity().RESULT_OK) {
                    Toast.makeText(getContext(), "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == getActivity().RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == getActivity().RESULT_OK) {
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
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
                apiAvailability.isGooglePlayServicesAvailable(getContext());
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
                apiAvailability.isGooglePlayServicesAvailable(getContext());
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
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void displayYoutubeVideo(final String videoID) {
        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_layout, youTubePlayerFragment).commit();

        youTubePlayerFragment.initialize(YoutubeDeveloperKey, new OnInitializedListener() {

            @Override
            public void onInitializationSuccess(Provider arg0, YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    YPlayer = youTubePlayer;
                    YPlayer.loadVideo(videoID);
                    YPlayer.play();
                }
            }

            @Override
            public void onInitializationFailure(Provider arg0, YouTubeInitializationResult arg1) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSavedMusicListener = (OnSavedMusicSelectedListener) context;
            mArrowListener = (OnArrowListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + e.getMessage());
        }
    }

    public static void updateCovers(Bitmap bmpTrackCover, String trackName, Bitmap bmpNextTrackCover, String nextTrackName) {
        ImageView trackCover = view.findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmpTrackCover);
        bmpCover = bmpTrackCover;
        TextView trackNameField = view.findViewById(R.id.trackName);
        trackNameField.setText(trackName);
        name = trackName;
        ImageView nextTrackCover = view.findViewById(R.id.nextTrackCover);
        TextView nextTrackNameField = view.findViewById(R.id.nextTrackName);
        if ((bmpNextTrackCover != null) && (nextTrackName != null)) {
            view.findViewById(R.id.nextTrackLoading).setVisibility(View.GONE);
            nextTrackCover.setImageBitmap(bmpNextTrackCover);
            bmpNextCover = bmpNextTrackCover;
            nextTrackNameField.setText(nextTrackName);
            nextName = nextTrackName;
        }
        else {
            view.findViewById(R.id.nextTrackLoading).setVisibility(View.VISIBLE);
            nextTrackCover.setImageBitmap(bmpNextTrackCover);
            nextTrackNameField.setText(nextTrackName);
        }
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, String> {
        com.google.api.services.youtube.YouTube mService;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
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
        public String doInBackground(Void... params) {
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
        private String getDataFromApi() throws IOException {
            Log.i("mService", mService.toString());
            SearchListResponse searchListResponse = mService.search().list("id")
                    .setMaxResults(Long.parseLong("1"))
                    .setQ(name)
                    .setType("video")
                    .execute();
            String id = searchListResponse.getItems().get(0).getId().getVideoId();
            Log.i("reponse", id);
            displayYoutubeVideo(id);

            return id;
        }


        @Override
        protected void onPreExecute() {
            //En cours de traitement
        }

        @Override
        protected void onPostExecute(String output) {
            if (output == null) {
                MainActivity mainActivity = new MainActivity();
                Toast.makeText(mainActivity.getApplicationContext(), "No results returned", Toast.LENGTH_SHORT).show();
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
                            REQUEST_AUTHORIZATION);
                } else {
                    Log.i("ERROR", "The following error occurred :" + mLastError.getMessage());
                }
            } else {
                Log.i("Status", "Request canceled");
            }
        }
    }
}

