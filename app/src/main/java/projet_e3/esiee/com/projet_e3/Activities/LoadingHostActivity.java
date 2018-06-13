package projet_e3.esiee.com.projet_e3.Activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import projet_e3.esiee.com.projet_e3.AnalyseData;
import projet_e3.esiee.com.projet_e3.BroadCast;
import projet_e3.esiee.com.projet_e3.HostClass;
import projet_e3.esiee.com.projet_e3.R;
import projet_e3.esiee.com.projet_e3.Services.FileTransferService;

public class LoadingHostActivity extends AnalyseData {

    private static WifiP2pManager aManager; //Manager de P2p
    private static WifiP2pManager.Channel aChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntent;
    private final WifiP2pConfig config = new WifiP2pConfig();
    private WifiP2pGroup wifiP2pGroup;
    private int guestNb;
    private ProgressBar progressBar;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_host);
        InitAttribut();
    }

    private void InitAttribut() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiManager != null;
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        aManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        aChannel = aManager.initialize(this,getMainLooper(),null);

        deletePersistentGroup();

        config.groupOwnerIntent = 15;

        mReceiver = new BroadCast(aManager,aChannel,null,this, wifiManager);
        mIntent = new IntentFilter();
        setAction();

        this.createGrp();

        if (getIntent().hasExtra("guestNumber")){
            guestNb = getIntent().getIntExtra("guestNumber",-1);
            Toast.makeText(getApplicationContext(), "Guestnumber : " + guestNb, Toast.LENGTH_SHORT).show();
        }

        loadingText = findViewById(R.id.loading_text);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(guestNb);
    }

    public void setAction(){
        mIntent.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    /**
     * Supprime les groupes persistants
     */
    private void deletePersistentGroup(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePersistentGroup")) {
                    for (int netid = 0; netid < 32; netid++) {
                        method.invoke(aManager, aChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static WifiP2pManager getManager() {
        return aManager;
    }

    public static WifiP2pManager.Channel getChannel() {
        return aChannel;
    }

    //Listener de connexion appelé dans à chaque modification du groupe
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            if(info.groupFormed && info.isGroupOwner){
                HostClass hostClass = new HostClass(getApplicationContext());
                hostClass.start();

                progressBar.setProgress(wifiP2pGroup.getClientList().size());
                int diff = guestNb-wifiP2pGroup.getClientList().size();
                loadingText.setText("Vous devez encore attendre "+ diff +" invités");

                Toast.makeText(getApplicationContext(), "ClientList : " + wifiP2pGroup.getClientList().size(), Toast.LENGTH_SHORT).show();
                if(wifiP2pGroup.getClientList().size()>= guestNb){
                    progressBar.setMax(1);
                    progressBar.setProgress(1);
                    loadingText.setText("C'est bon!");
                    Intent intent = new Intent(LoadingHostActivity.this, HostActivity.class);
                    intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                    intent.putExtra("wifip2pGroup", wifiP2pGroup);
                    intent.putExtra("availableGenres", getIntent().getStringArrayListExtra("availableGenres"));
                    intent.putExtra("host",1);
                    startActivity(intent);
                }

            }
        }
    };

    //Listener du groupP2p appelé dans le broadCast
    public WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if(group!=null)
            {
                Log.i("pass",group.getPassphrase());
                wifiP2pGroup = group;
                aManager.requestConnectionInfo(aChannel,connectionInfoListener);
            }
        }
    };

    /**
     * Créer le groupe P2P
     */
    public void createGrp(){
        aManager.createGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {Toast.makeText(getApplicationContext(), "Groupe crée", Toast.LENGTH_SHORT).show(); }

            @Override
            public void onFailure(int i) {Toast.makeText(getApplicationContext(), "Groupe déjà crée", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Supprime le Groupe P2p
     */
    public void disconnect(){
        aManager.removeGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"Groupe fermé",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(),"Echec de la suppression du groupe",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
        aManager.cancelConnect(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

    /**
     * AsyncTask permettant à l'Host d'attendre l'envoi d'un signal de déconnexion venant d'un guest
     */
    public static class onDecoAsyncTask extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        private Context mFilecontext;
        private int PORT;
        public onDecoAsyncTask(Context context, int port) {
            this.mFilecontext = context;
            this.PORT = port;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(PORT));

                Socket client = serverSocket.accept();
                try {
                    //Recupère l'Ip du client connecté à la socket et supprime le JSON correspondant
                    String IpClient = client.getInetAddress().getHostAddress();
                    File file =  new File(mFilecontext.getFilesDir(),IpClient+".json");
                    file.delete();

                    serverSocket.close();
                    return IpClient;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //S'il n'y a eu aucune exception relance une asyncTask parallèle
                if (!TextUtils.isEmpty(result)) {
                    onDecoAsyncTask decoObj = new
                            onDecoAsyncTask(mFilecontext, FileTransferService.PORT);
                    decoObj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});
                }
            }
        }
    }

    /**
     * AsyncTask permettant à l'hote la reception d'un fichier JSON
     */
    public static class FileServerAsyncTask extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        private Context mFilecontext;
        private int PORT;
        public FileServerAsyncTask(Context context, int port) {
            this.mFilecontext = context;
            this.PORT = port;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(PORT));

                Socket client = serverSocket.accept();
                try {
                    String IpClient = client.getInetAddress().getHostAddress();
                    final File f = new File(mFilecontext.getFilesDir(), IpClient+".json");

                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();

                    InputStream inputstream = client.getInputStream();

                    copyFile(inputstream, new FileOutputStream(f));

                    serverSocket.close();
                    return f.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(mFilecontext,"Fichier reçu avec succes",Toast.LENGTH_SHORT).show();
                if (!TextUtils.isEmpty(result)) {
                    FileServerAsyncTask FileServerobj = new
                            FileServerAsyncTask(mFilecontext, FileTransferService.PORT);
                    FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});
                }
            }
        }

        void copyFile(InputStream inputStream, OutputStream out) {

            byte buf[] = new byte[8500];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.close();
                inputStream.close();
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }
}