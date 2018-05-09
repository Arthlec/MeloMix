package projet_e3.esiee.com.projet_e3;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TransfertData extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiDirectBroadcast receiver;
    private IntentFilter intentFilter;
    private ListView DeviceList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfert);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() != true)
            OpenWifi();
        //ListView
        DeviceList = (ListView)findViewById(R.id.mainlist);
        adapter = new ArrayAdapter<String>(DeviceList.getContext(),android.R.layout.simple_list_item_1);
        this.DeviceList.setAdapter(adapter);
        //
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WifiDirectBroadcast(manager, channel, this);

        intentFilter = new IntentFilter();
        this.setAction(intentFilter);
        //Trouve les peers à proximité
        this.discorver();


        //CloseWifi();
    }

    /**
     * AddAction sur l'intentfilter
     */
    public void setAction(IntentFilter intentFilter){
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }
    /**
     * DiscoverPeers
     */
    public void discorver(){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(TransfertData.this, "Recherche de Peers en cours", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(TransfertData.this, "Impossible de trouver des peers !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ListView getListView(){
        return DeviceList;
    }

    public ArrayAdapter<String> getAdapter() {
        return adapter;
    }

    /**
     * En cours de recherche
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(this.receiver,this.intentFilter);
    }

    /**
     * En pause de recherche
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.receiver);
    }

    /**
     * Active le wifi
     */
    public void OpenWifi(){
        WifiManager wM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wM.setWifiEnabled(true);
        Toast.makeText(TransfertData.this,"Wifi Activé",Toast.LENGTH_SHORT).show();
    }

    /*
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
    this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
    */

    /**
     * Recevoir un fichier coté serveur
     */
    public static class FileServer extends AsyncTask<Void,Void, String>{

        private Context aContext;

        /**
         * Constructeur
         * @param pContext
         */
        public FileServer(Context pContext){
            this.aContext = pContext;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                ServerSocket MySocket = new ServerSocket(8888);
                Socket client = MySocket.accept();

                File file = new File(Environment.getExternalStorageDirectory()+"/"+aContext.getPackageName()+"/wifi direct data/"+"image.png");
                File directory = new File(file.getParent());
                if(!directory.exists()){
                    directory.mkdir();
                }
                file.createNewFile(); // Créer le fichier
                InputStream inputStream = client.getInputStream();
               // copyFile(inputStream, new FileOutputStream(file));
                MySocket.close();
                return file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String Result) {
            if(Result != null){
                //statusText.setText("File copied - "+ Result);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://"+ Result), "image/*");
                aContext.startActivity(intent);
            }
            super.onPostExecute(Result);
        }
    }
}

