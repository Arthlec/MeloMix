package projet_e3.esiee.com.projet_e3.Activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.private_.TreeNode;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsString;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchListResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

import projet_e3.esiee.com.projet_e3.AnalyseData;
import projet_e3.esiee.com.projet_e3.BroadCast;
import projet_e3.esiee.com.projet_e3.Fragments.GuestsListFragment;
import projet_e3.esiee.com.projet_e3.Fragments.HistoryFragment;
import projet_e3.esiee.com.projet_e3.Fragments.MainFragment;
import projet_e3.esiee.com.projet_e3.Fragments.SavedMusicsFragment;
import projet_e3.esiee.com.projet_e3.Fragments.StatsFragment;
import projet_e3.esiee.com.projet_e3.HostClass;
import projet_e3.esiee.com.projet_e3.R;
import projet_e3.esiee.com.projet_e3.ReceiveDataFlow;
import projet_e3.esiee.com.projet_e3.ShareDataToTarget;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static junit.framework.Assert.assertTrue;

public class HostActivity extends AnalyseData implements EasyPermissions.PermissionCallbacks, NavigationView.OnNavigationItemSelectedListener, MainFragment.OnSavedMusicSelectedListener, MainFragment.OnArrowListener, HistoryFragment.OnSaveMusicHistorySelectedListener {

    GoogleAccountCredential mCredential;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    public ArrayList<String> frequentGenres = new ArrayList<>();
    private static ArrayList<String> trackNamesList = new ArrayList<>();
    private static ArrayList<String> trackIDList = new ArrayList<>();
    private boolean isInitialisation;
    private static String trackID;
    private static String nextTrackID;
    private static Bitmap bmp;
    private static String trackName;
    private static Bitmap nextBmp;
    private static String nextTrackName;
    public ArrayList<String> availableGenresList = new ArrayList<>();
    public static String authToken = "";
    private DrawerLayout mDrawerLayout;
    private Stack<String> tracksIDS = new Stack<>();
    private Stack<Bitmap> tracksCovers = new Stack<>();
    private Stack<String> tracksNames = new Stack<>();
    private String MY_PREFS = "my_prefs";
    private WifiP2pGroup wifiP2pGroup;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter mIntent;
    private List[] dataList = new List[2];
    private int host;
    private String[] objects;

    //FOR FRAGMENTS
    // 1 - Declare fragment handled by Navigation Drawer
    private MainFragment fragmentMain;
    private Fragment fragmentStats;
    private Fragment fragmentSavedMusics;
    private Fragment fragmentHistory;
    private GuestsListFragment fragmentGuestsList;

    //FOR DATAS
    // 2 - Identify each fragment with a number
    private static final int FRAGMENT_MAIN = 0;
    private static final int FRAGMENT_STATS = 1;
    private static final int FRAGMENT_SAVED_MUSICS = 2;
    private static final int FRAGMENT_HISTORY = 3;
    private static final int FRAGMENT_GUESTS_LIST = 4;

    //Getters
    public static Bitmap getBmp() {
        return bmp;
    }
    public static Bitmap getNextBmp() {
        return nextBmp;
    }
    public static String getTrackName() {
        return trackName;
    }
    public static String getNextTrackName() {
        return nextTrackName;
    }
    //Setters
    public static void setBmp(Bitmap bmp) {
        HostActivity.bmp = bmp;
    }
    public static void setNextBmp(Bitmap nextBmp) {
        HostActivity.nextBmp = nextBmp;
    }
    public static void setTrackName(String trackName) {
        HostActivity.trackName = trackName;
    }
    public static void setNextTrackName(String nextTrackName) {
        HostActivity.nextTrackName = nextTrackName;
    }

    public void setObjects(String[] objects) {
        this.objects = objects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_activity);

        isInitialisation = true;
        mCredential = MainActivity.mCredential;
        host = getIntent().getIntExtra("host", 0);

        if(host == 1)
            getResultsFromApi();

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showFirstFragment();
        authToken = getIntent().getStringExtra("authToken");
        availableGenresList = getIntent().getStringArrayListExtra("availableGenres");
        frequentGenres = getIntent().getStringArrayListExtra("frequentGenres");

        Log.i("authToken", authToken);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (host == 1)
        {
            manager = LoadingHostActivity.getManager();
            channel = LoadingHostActivity.getChannel();
            dataList = LoadingHostActivity.getLoadingDatalist();
        }else if(host ==0){
            bmp = getIntent().getExtras().getParcelable("GuestBmp");
            manager = GuestActivity.getaManager();
            channel = GuestActivity.getaChannel();
            dataList = LoadingGuestActivity.getLoadingDatalist();
            ReceiveDataFlow receiveDataFlow = new ReceiveDataFlow(getApplicationContext(),10014,"upDate",null);
            receiveDataFlow.start();
            Log.i("bm",getIntent().getStringExtra("bmp_url"));
        }
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        wifiP2pGroup = bundle.getParcelable("wifip2pGroup");
        BroadCast mReceiver = new BroadCast(manager, channel, null, null, this, wifiManager);
        mIntent = new IntentFilter();
        setAction();

        giveListToStat();
    }

    public void setAction(){
        mIntent.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Voulez-vous vraiment quitter cette page?")
                .setMessage("Les données en cours d'utilisation seront perdues")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        if(host==1){
                            LoadingHostActivity.hostContext.finish();
                        } else {
                            GuestActivity.guestContext.finish();
                        }
                        HostActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    public void makeAnalyse() {
        frequentGenres = this.analyseData(this.getFilesDir());
        dataList = buildListTab();
        giveListToStat();
        Log.i("GenresFréquents", frequentGenres.toString());
    }

    public void giveListToStat(){
        StatsFragment.setDataList(getDataList());
    }

    public void sendDataToTarget(String targetAdress){
        ShareDataToTarget shareDataToTarget = new ShareDataToTarget(targetAdress, getApplicationContext(),objects);
        shareDataToTarget.start();
    }

    public void lauchSignalToTargets(Context context){
        File[] files = getJSONFiles(context.getFilesDir());
        ArrayList<String> adresses = new ArrayList<>();
        for (File currentFile : files) {
            if (!currentFile.getName().contains("userGenres")) {
                adresses.add(currentFile.getName());
            }
        }
        if(!adresses.isEmpty())
        {
            adresses = applyRegex(adresses,".json","");
            for(String adress : adresses){
                sendDataToTarget(adress);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // set item as selected to persist highlight
        menuItem.setChecked(true);
        // close drawer when item is tapped
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_main:
                this.showFragment(FRAGMENT_MAIN);
                break;
            case R.id.nav_stats:
                this.showFragment(FRAGMENT_STATS);
                break;
            case R.id.nav_musics:
                this.showFragment(FRAGMENT_SAVED_MUSICS);
                break;
            case R.id.nav_history:
                this.showFragment(FRAGMENT_HISTORY);
                break;
            case R.id.nav_guests:
                this.showFragment(FRAGMENT_GUESTS_LIST);
                break;
            case R.id.nav_disconnect:
                this.disconnect();
                break;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onArrowSelected(String direction) {
        if(direction.equals("next") && nextBmp != null && nextTrackName != null && nextTrackID != null) {
            tracksIDS.push(trackID);
            tracksCovers.push(bmp);
            tracksNames.push(trackName);
            trackID = nextTrackID;
            bmp = nextBmp;
            trackName = nextTrackName;
            nextTrackID = null;
            nextBmp = null;
            nextTrackName = null;
            HistoryFragment.trackCoverList.add(0, bmp);
            HistoryFragment.trackNameList.add(0, trackName);
            //requestData();
            getResultsFromApi();
            lauchSignalToTargets(getApplicationContext());
        }
        else if (direction.equals("next") && (nextBmp == null || nextTrackName == null || nextTrackID == null)) {
            Toast.makeText(getApplicationContext(), "Veuillez attendre la recherche du prochain titre", Toast.LENGTH_SHORT).show();
        }
        else {
            if(!tracksCovers.isEmpty()) {
                nextTrackID = trackID;
                nextBmp = bmp;
                nextTrackName = trackName;
                trackID = tracksIDS.pop();
                bmp = tracksCovers.pop();
                trackName = tracksNames.pop();
                HistoryFragment.trackCoverList.add(0, bmp);
                HistoryFragment.trackNameList.add(0, trackName);
                fragmentMain.displayYoutubeVideo(trackID);
                //getResultsFromApi();
            }
        }
        MainFragment.updateCovers(bmp, trackName, nextBmp, nextTrackName);
    }

    @Override
    public void onSavedMusicSelected() {
        if (SavedMusicsFragment.trackNameList.contains(trackName) && SavedMusicsFragment.trackCoverList.contains(bmp)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ajouter à mes musiques")
                    .setMessage("Vous avez déjà enregistré cette musique, voulez-vous l'enregistrer à nouveau ?")
                    .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SavedMusicsFragment.trackCoverList.add(0, bmp);
                            SavedMusicsFragment.trackNameList.add(0, trackName);
                            Toast.makeText(getApplicationContext(), "Musique ajoutée à vos musiques", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }
        else {
            SavedMusicsFragment.trackCoverList.add(0, bmp);
            SavedMusicsFragment.trackNameList.add(0, trackName);
            Toast.makeText(getApplicationContext(), "Musique ajoutée à vos musiques", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveMusicHistorySelected(final Bitmap cover, final String name) {
        if (SavedMusicsFragment.trackNameList.contains(name) && SavedMusicsFragment.trackCoverList.contains(cover)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ajouter à mes musiques")
                    .setMessage("Vous avez déjà enregistré cette musique, voulez-vous l'enregistrer à nouveau ?")
                    .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SavedMusicsFragment.trackCoverList.add(0, cover);
                            SavedMusicsFragment.trackNameList.add(0, name);
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }
        else {
            SavedMusicsFragment.trackCoverList.add(0, cover);
            SavedMusicsFragment.trackNameList.add(0, name);
        }
    }

    private void showFragment(int fragmentIdentifier){
        switch (fragmentIdentifier){
            case FRAGMENT_MAIN :
                this.showMainFragment();
                break;
            case FRAGMENT_STATS :
                this.showStatsFragment();
                break;
            case FRAGMENT_SAVED_MUSICS:
                this.showSavedMusicsFragment();
                break;
            case FRAGMENT_HISTORY:
                this.showHistoryFragment();
                break;
            case FRAGMENT_GUESTS_LIST:
                this.showGuestsListFragment();
                break;
            default:
                break;
        }
    }

    // 4 - Create each fragment page and show it

    private void showMainFragment(){
        if (this.fragmentMain == null) this.fragmentMain = MainFragment.newInstance();
        this.startTransactionFragment(this.fragmentMain);
    }

    private void showStatsFragment(){
        if (this.fragmentStats == null) this.fragmentStats = StatsFragment.newInstance();
        this.startTransactionFragment(this.fragmentStats);
    }

    private void showSavedMusicsFragment(){
        if (this.fragmentSavedMusics == null) this.fragmentSavedMusics = SavedMusicsFragment.newInstance();
        this.startTransactionFragment(this.fragmentSavedMusics);
    }

    private void showHistoryFragment(){
        if (this.fragmentHistory == null) this.fragmentHistory = HistoryFragment.newInstance();
        this.startTransactionFragment(this.fragmentHistory);
    }

    private void showGuestsListFragment(){
        if (this.fragmentGuestsList == null) this.fragmentGuestsList = GuestsListFragment.newInstance();
        manager.requestGroupInfo(channel, groupInfoListener);
        fragmentGuestsList.setWifiP2PGroup(wifiP2pGroup);
        fragmentGuestsList.setManager(manager);
        fragmentGuestsList.setChannel(channel);
        fragmentGuestsList.setGroupInfoListener(groupInfoListener);
        this.startTransactionFragment(this.fragmentGuestsList);
    }

    // 3 - Generic method that will replace and show a fragment inside the MainActivity Frame Layout
    private void startTransactionFragment(Fragment fragment){
        if (!fragment.isVisible()){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_main_constraint_layout, fragment).commit();
        }
    }

    // 1 - Show first fragment when activity is created
    private void showFirstFragment(){
        Fragment visibleFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_main_constraint_layout);
        if (visibleFragment == null){
            // 1.1 - Show Profile Fragment
            this.showFragment(FRAGMENT_MAIN);
            // 1.2 - Mark as selected the menu item corresponding to ProfileFragment
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            if(host==1){
                HostClass hostClass = new HostClass(getApplicationContext());
                hostClass.start();
            }
        }
    };

    public WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if(group!=null)
            {
                wifiP2pGroup = group;
                //Toast.makeText(getApplicationContext(),"clientGroup : " + wifiP2pGroup.getClientList(),Toast.LENGTH_SHORT).show();
                manager.requestConnectionInfo(channel,connectionInfoListener);
            }
            fragmentGuestsList.setWifiP2PGroup(wifiP2pGroup);
        }
    };
    public List[] getDataList(){
        return dataList;
    }

    public void requestData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] trackInfos = getTrackInfo();
                    URL trackURL = new URL(trackInfos[0]);


                    trackInfos = getTrackInfo();
                    URL nextTrackURL = new URL(trackInfos[0]);
                    new Thread(new Runnable() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainFragment.updateCovers(bmp, trackName, nextBmp, nextTrackName);
                                }
                            });
                        }
                    }).start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private String[] getTrackInfo() throws IOException {
                ArrayList<String> frequentAvailableGenresList = new ArrayList<>();

                for (int i=0; i<frequentGenres.size(); i++) {
                    String frequentGenreWithHyphen = frequentGenres.get(i).replace(' ', '-');
                    if (availableGenresList.contains(frequentGenres.get(i)))
                        frequentAvailableGenresList.add(frequentGenres.get(i));
                    else if (availableGenresList.contains(frequentGenreWithHyphen))
                        frequentAvailableGenresList.add(frequentGenreWithHyphen);
                    else if (frequentGenres.get(i).equals("r&b"))
                        frequentAvailableGenresList.add("r-n-b");
                }

                String genreSeed;
                if (frequentAvailableGenresList.size() == 0) {
                    Log.i("Note : ", "Search less accurate");
                    for (int i=0; i<frequentGenres.size(); i++) {
                        String[] frequentGenresSplit = frequentGenres.get(i).split(" ");
                        for (int j=0; j<frequentGenresSplit.length; j++)
                            if (availableGenresList.contains(frequentGenresSplit[j]))
                                frequentAvailableGenresList.add(frequentGenresSplit[j]);
                    }
                }

                genreSeed = "?limit=100&seed_genres=" + frequentAvailableGenresList.get(0);
                for (int i=1; i<frequentAvailableGenresList.size(); i++) {
                    genreSeed = genreSeed + "%2C" + frequentAvailableGenresList.get(i);
                }

                Log.i("frequent",frequentAvailableGenresList.toString());


                String[] trackInfo = new String[2];
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/recommendations" + genreSeed + "&min_popularity=50");

                // Create connection
                HttpsURLConnection myConnection;
                String waitTime;
                do {
                    myConnection = (HttpsURLConnection) spotifyEndpoint.openConnection();
                    myConnection.setRequestProperty("Authorization", "Bearer " + authToken);
                    waitTime = myConnection.getHeaderField("Retry-After");
                    if (waitTime != null) {
                        int waitTimeSeconds = Integer.parseInt(waitTime);
                        try {
                            Thread.sleep(waitTimeSeconds * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.i("WaitTime", waitTime);
                    }
                } while (waitTime != null);

                if (myConnection.getResponseCode() == 200) {
                    // Success
                    InputStream responseBody = myConnection.getInputStream();

                    JSON json = JSON.std.with(new JacksonJrsTreeCodec());
                    TreeNode root = json.treeFrom(responseBody);
                    assertTrue(root.isObject());
                    int tracksNumber = root.get("tracks").size();
                    int i;
                    for (i=0; i<tracksNumber; i++) {
                        JrsString trackName = (JrsString) root.get("tracks").get(i).get("name");
                        if (!trackNamesList.contains(trackName.asText())) {
                            trackNamesList.add(trackName.asText());
                            trackInfo[1] = trackName.asText();
                            JrsString imageURL = (JrsString) root.get("tracks").get(i).get("album").get("images").get(0).get("url");
                            trackInfo[0] = imageURL.asText();
                            break;
                        }
                    }

                    myConnection.disconnect();
                    return trackInfo;
                } else {
                    Log.i("responseCode", "" + myConnection.getResponseCode());
                    return null;
                }
            }
        });
    }

    private void disconnect() {
        //deleteCache(this);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        pref.edit().remove("user_name").apply(); //clear pref pseudo
        if(pref.contains("userAccountSpotify"))
            pref.edit().remove("userAccountSpotify").apply(); //clear pref user account Spotify
        if(host==1){
            LoadingHostActivity.hostContext.finish();
        }else if(host==0){
            GuestActivity.guestContext.finish();
        }
        deleteJson();
        Intent intent = new Intent(HostActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear stack activity
        startActivity(intent);
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public void deleteJson(){
        File[] files = getJSONFiles(this.getFilesDir());
        for (File current : files) {
            if (!current.getName().contains("userGenres")) {
                current.delete();
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
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
            String accountName = this.getPreferences(Context.MODE_PRIVATE)
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
                if (resultCode != this.RESULT_OK) {
                    Toast.makeText(this, "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == this.RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == this.RESULT_OK) {
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
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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
        private String getDataFromApi() {
            String[] trackInfos;
            try {
                if(isInitialisation) {
                    trackInfos = getTracksAttributes();
                    URL trackURL = new URL(trackInfos[2]);
                    trackID = trackInfos[0];
                    bmp = BitmapFactory.decodeStream(trackURL.openConnection().getInputStream());
                    trackName = trackInfos[1];

                    HistoryFragment.trackCoverList.add(0, bmp);
                    HistoryFragment.trackNameList.add(0, trackName);

                    isInitialisation = false;
                }

                fragmentMain.displayYoutubeVideo(trackID);

                trackInfos = getTracksAttributes();
                URL nextTrackURL = new URL(trackInfos[2]);
                nextTrackID = trackInfos[0];
                nextBmp = BitmapFactory.decodeStream(nextTrackURL.openConnection().getInputStream());
                nextTrackName = trackInfos[1];
                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainFragment.updateCovers(bmp, trackName, nextBmp, nextTrackName);
                            }
                        });
                    }
                }).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            setObjects(trackInfos);
            return trackInfos[0];
        }

        private String[] getTracksAttributes() throws IOException {

            String[] trackInfo = new String[3];

            Random genreSelector = new Random();
            int genreNumber = genreSelector.nextInt(frequentGenres.size());

            Log.i("mService", mService.toString());
            SearchListResponse searchListResponse = mService.search().list("snippet")
                    .setMaxResults(Long.parseLong("50"))
                    .setQ(frequentGenres.get(genreNumber) + "music" + " -live -radio")
                    .setVideoDuration("short")
                    .setType("video")
                    .execute();
            Log.i("musicGenre", frequentGenres.get(genreNumber));
            String trackID;
            for (int i=0; i<searchListResponse.getItems().size(); i++) {
                trackID = searchListResponse.getItems().get(i).getId().getVideoId();
                if (!trackIDList.contains(trackID)){
                    trackIDList.add(trackID);
                    trackInfo[0] = trackID;
                    trackInfo[1] = searchListResponse.getItems().get(i).getSnippet().getTitle();
                    trackInfo[2] = searchListResponse.getItems().get(i).getSnippet().getThumbnails().getHigh().getUrl();
                    break;
                }
            }
            return trackInfo;
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
