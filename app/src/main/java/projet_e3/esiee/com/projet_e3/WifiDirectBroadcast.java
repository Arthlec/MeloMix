package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WifiDirectBroadcast extends BroadcastReceiver {

    private WifiP2pManager aManager;
    private WifiP2pManager.Channel aChannel;

    public WifiDirectBroadcast(WifiP2pManager pManager, WifiP2pManager.Channel pChannel) {
        this.aManager = pManager;
        this.aChannel = pChannel;
    }


    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            // Notifie lors d'une nouvelle connexion
        }

        if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            // Si l'état du wifi change, ou si je reçois/envoi des données
        }
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){

            }
            else{
                Toast.makeText(context, "Wifi Direct non disponible !", Toast.LENGTH_LONG).show();
            }
        }
        if( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if(aManager != null)
                aManager.requestPeers(aChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        for(final WifiP2pDevice device : peers.getDeviceList()) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;
                            aManager.connect(aChannel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Socket socketServ = new Socket();
                                    byte[] buffer = new byte[1024];
                                    int len;
                                    try {
                                        socketServ.bind(null);
                                        socketServ.connect(new InetSocketAddress(device.deviceAddress, 8888));

                                        OutputStream outputStream = socketServ.getOutputStream();
                                        ContentResolver contentResolver = context.getContentResolver();

                                        InputStream inputStream = null;
                                        //TODO: choisir le fichier à envoyer
                                        inputStream = contentResolver.openInputStream(Uri.parse("image.JPG"));
                                        while ((len = inputStream.read(buffer)) != -1){
                                            outputStream.write(buffer,0,len);
                                        }
                                        outputStream.close();
                                        inputStream.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }finally {
                                        if(socketServ != null){
                                            if (socketServ.isConnected()){
                                                try {
                                                    socketServ.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                    //Ajouter le device à une liste et l'afficher
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Toast.makeText(context, "Impossible de se co à "+ device.deviceName, Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                    }
                });
        }
    }
}