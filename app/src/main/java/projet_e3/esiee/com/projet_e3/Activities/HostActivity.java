package projet_e3.esiee.com.projet_e3.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
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


import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.private_.TreeNode;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import projet_e3.esiee.com.projet_e3.Fragments.GuestsListFragment;
import projet_e3.esiee.com.projet_e3.Fragments.HistoryFragment;
import projet_e3.esiee.com.projet_e3.Fragments.SavedMusicsFragment;
import projet_e3.esiee.com.projet_e3.Fragments.MainFragment;
import projet_e3.esiee.com.projet_e3.Fragments.StatsFragment;
import projet_e3.esiee.com.projet_e3.R;

import static junit.framework.Assert.assertTrue;

public class HostActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainFragment.OnSavedMusicSelectedListener {

    private static Bitmap bmp;
    private static String trackName;
    private static String authToken = "";
    private DrawerLayout mDrawerLayout;

    //FOR FRAGMENTS
    // 1 - Declare fragment handled by Navigation Drawer
    private MainFragment fragmentMain;
    private Fragment fragmentStats;
    private Fragment fragmentSavedMusics;
    private Fragment fragmentHistory;
    private Fragment fragmentGuestsList;

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
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onSavedMusicSelected() {
        if (SavedMusicsFragment.trackNameList.contains(trackName) && SavedMusicsFragment.trackCoverList.contains(bmp)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ajouter à mes musiques")
                    .setMessage("Vous avez déjà enregistré cette musique, voulez-vous l'enregistrer à nouveau ?")
                    .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SavedMusicsFragment.trackCoverList.add(bmp);
                            SavedMusicsFragment.trackNameList.add(trackName);
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
            SavedMusicsFragment.trackCoverList.add(bmp);
            SavedMusicsFragment.trackNameList.add(trackName);
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

    public void requestData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL trackURL = new URL(getTrackInfo()[0]);
                    bmp = BitmapFactory.decodeStream(trackURL.openConnection().getInputStream());
                    trackName = getTrackInfo()[1];
                    new Thread(new Runnable() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainFragment.updateCovers(bmp, trackName, bmp, trackName);
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
}
