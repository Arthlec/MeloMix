package projet_e3.esiee.com.projet_e3;


import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TransfertData extends AppCompatActivity {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiDirectBroadcast receiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WifiDirectBroadcast(manager, channel);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(TransfertData.this, "Impossible de trouver des peers !", Toast.LENGTH_LONG).show();

            }
        });
    }


    @Override
    protected void onResume() {
        registerReceiver(receiver,intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }


    public static class FileServer extends AsyncTask<Void,Void, String>{
        private Context aContext;

        public FileServer(Context pContext){
            this.aContext = pContext;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                ServerSocket MySocket = new ServerSocket(8888);
                Socket client = MySocket.accept();

                File file = new File(Environment.getExternalStorageDirectory()+"/"+aContext.getPackageName()+"/wifi direct data/"+"image.jpg");
                File directory = new File(file.getParent());
                if(!directory.exists()){
                    directory.mkdir();
                }
                file.createNewFile();
                InputStream inputStream = client.getInputStream();
                // TODO: enregister le fichier
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
                // TODO: Ouvrir le fichier
            }
            super.onPostExecute(Result);
        }
    }
}

