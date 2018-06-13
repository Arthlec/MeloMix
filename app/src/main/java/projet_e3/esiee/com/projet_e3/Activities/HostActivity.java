package projet_e3.esiee.com.projet_e3.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;


import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.private_.TreeNode;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

import projet_e3.esiee.com.projet_e3.BroadCast;
import projet_e3.esiee.com.projet_e3.Fragments.GuestsListFragment;
import projet_e3.esiee.com.projet_e3.Fragments.HistoryFragment;
import projet_e3.esiee.com.projet_e3.Fragments.SavedMusicsFragment;
import projet_e3.esiee.com.projet_e3.Fragments.MainFragment;
import projet_e3.esiee.com.projet_e3.Fragments.StatsFragment;
import projet_e3.esiee.com.projet_e3.R;

import static junit.framework.Assert.assertTrue;

public class HostActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainFragment.OnSavedMusicSelectedListener, MainFragment.OnArrowListener {

    private static Bitmap bmp;
    private static String trackName;
    private static Bitmap nextBmp;
    private static String nextTrackName;
    private static String authToken = "";
    private DrawerLayout mDrawerLayout;
    private Stack<Bitmap> tracksCovers = new Stack<>();
    private Stack<String> tracksNames = new Stack<>();
    private String MY_PREFS = "my_prefs";
    private WifiP2pGroup wifiP2pGroup;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadCast mReceiver;
    private IntentFilter mIntent;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_activity);

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
        Log.i("authToken", authToken);
        requestData();

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        manager = LoadingHostActivity.getManager();
        channel = LoadingHostActivity.getChannel();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        wifiP2pGroup =  bundle.getParcelable("wifip2pGroup");

        mReceiver = new BroadCast(manager,channel,null,null,this, wifiManager);
        mIntent = new IntentFilter();
        setAction();
    }

    public void setAction(){
        mIntent.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
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
        if(direction.equals("next")) {
            tracksCovers.push(bmp);
            tracksNames.push(trackName);
            bmp = nextBmp;
            trackName = nextTrackName;
            nextBmp = null;
            nextTrackName = null;
            HistoryFragment.trackCoverList.add(0, bmp);
            HistoryFragment.trackNameList.add(0, trackName);
        }
        else {
            if(!tracksCovers.isEmpty()) {
                nextBmp = bmp;
                nextTrackName = trackName;
                bmp = tracksCovers.pop();
                trackName = tracksNames.pop();
                HistoryFragment.trackCoverList.add(0, bmp);
                HistoryFragment.trackNameList.add(0, trackName);
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

        }
    };

    public WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if(group!=null)
            {
                wifiP2pGroup = group;
                Toast.makeText(getApplicationContext(),"clientGroup : " + wifiP2pGroup.getClientList(),Toast.LENGTH_SHORT).show();
                manager.requestConnectionInfo(channel,connectionInfoListener);
            }
            fragmentGuestsList.setWifiP2PGroup(wifiP2pGroup);
        }
    };

    public void requestData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL trackURL = new URL(getTrackInfo()[0]);
                    bmp = BitmapFactory.decodeStream(trackURL.openConnection().getInputStream());
                    trackName = getTrackInfo()[1];

                    // Might cause a issue if requestData isn't used only during the initialisation
                    HistoryFragment.trackCoverList.add(0, bmp);
                    HistoryFragment.trackNameList.add(0, trackName);
                    // Might cause a issue if requestData isn't used only during the initialisation

                    URL nextTrackURL = new URL("https://www.theedgesusu.co.uk/wp-content/uploads/2017/10/post-malone-e1508708621268.jpg");
                    nextBmp = BitmapFactory.decodeStream(nextTrackURL.openConnection().getInputStream());
                    nextTrackName = "Rockstar - Post Malone";
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
                String[] trackInfo = new String[2];
                // Create URL
                URL spotifyEndpoint = new URL("https://api.spotify.com/v1/tracks/11dFghVXANMlKmJXsNCbNl");

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
                    JrsString imageURL = (JrsString) root.get("album").get("images").get(0).get("url");
                    JrsString trackName = (JrsString) root.get("album").get("name");
                    trackInfo[0] = imageURL.asText();
                    trackInfo[1] = trackName.asText();

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
        deleteCache(this);
        ProfileActivity.isLoggedInSpotify = false; //disconnect from spotify
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        pref.edit().remove("user_name").apply(); //clear pref pseudo
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
}
