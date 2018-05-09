package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import static android.content.ContentValues.TAG;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static projet_e3.esiee.com.projet_e3.R.layout.support_simple_spinner_dropdown_item;

public class WifiDirectBroadcast extends BroadcastReceiver {

    private WifiP2pManager aManager; //WifiManager
    private WifiP2pManager.Channel aChannel; //WifiChannel
    private TransfertData TheActivity;
    private WifiP2pManager.PeerListListener MyPeersListener;
    private ArrayList<WifiP2pDevice> peers;
    private List<String> NamePeers;

    /**
     * Constructeur
     * @param pManager
     * @param pChannel
     * @param TheActivity
     */
    public WifiDirectBroadcast(WifiP2pManager pManager, WifiP2pManager.Channel pChannel, TransfertData TheActivity) {
        this.aManager = pManager;
        this.aChannel = pChannel;
        this.TheActivity = TheActivity;
        this.peers = new ArrayList<WifiP2pDevice>();
        this.NamePeers = new ArrayList<String>();
    }

    /**
     * A la récéption, on scan les actions écoutées.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Toast.makeText(context, "Receive", Toast.LENGTH_SHORT).show();
        if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            // Notifie lors d'une connexion / déconnexion
            //Toast.makeText(context, "new connection", Toast.LENGTH_SHORT).show();
        }
        if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            // Si l'état du wifi change, ou si je reçois/envoi des données
            TheActivity.OpenWifi();
            //Toast.makeText(context, "wifi device action", Toast.LENGTH_SHORT).show();
        }
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            /**
             * Wifi direct activé ou pas ?
             */
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                // SI oui :
                // TheActivity.setIsWifiP2pEnabled(true);
                // Toast.makeText(context, "Wifi Direct utilisable !", Toast.LENGTH_SHORT).show();
            }
            else{
                //Toast.makeText(context, "Wifi Direct non disponible !", Toast.LENGTH_SHORT).show();
                TheActivity.OpenWifi();
            }
        }
        //DES Qu'un nouveau peer apparait
       // Toast.makeText(context, "On rentre ou pas ?", Toast.LENGTH_LONG).show();
        if( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //Toast.makeText(context, "number of peers changed", Toast.LENGTH_SHORT).show();
            if(aManager != null) {
               // Toast.makeText(context, "Manager non null \n"+aChannel, Toast.LENGTH_SHORT).show();
                //Obtient la liste des peers en wifi-direct
               aManager.requestPeers(aChannel,new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peersDevice) {
                        //Toast.makeText(context, "On_Peers_Avail \n"+peersDevice.getDeviceList(), Toast.LENGTH_SHORT).show();
                        if(peersDevice.getDeviceList().isEmpty())
                        {
                            NamePeers.clear();
                            TheActivity.getAdapter().clear();
                        }
                        if (!peersDevice.getDeviceList().equals(peers)){
                            peers.clear();
                            peers.addAll(peersDevice.getDeviceList());
                            NamePeers.clear();
                            for(WifiP2pDevice peerDevice : peers)
                            {
                                NamePeers.add(peerDevice.deviceName);
                            }
                            // Toast.makeText(context, "Ajout list \n"+peers, Toast.LENGTH_SHORT).show();
                            TheActivity.getAdapter().clear();
                            TheActivity.getAdapter().addAll(NamePeers);
                        }
                        for (final WifiP2pDevice device : peersDevice.getDeviceList()) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;
                            //Toast.makeText(context, "For Device", Toast.LENGTH_SHORT).show();

                            //Connexion au peers detecté
                            Toast.makeText(context, "On va se coco\n"+device.deviceName, Toast.LENGTH_SHORT).show();
                            connectPeers(context, device, config);
                        }
                        if (peers.isEmpty()) {
                            Toast.makeText(context, "No devices found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
            }
        }
    }

    public void connectPeers(final Context context, final WifiP2pDevice device, WifiP2pConfig config){
        aManager.connect(aChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Connecté à "+ device.deviceName, Toast.LENGTH_LONG).show();
                Socket socketServ = new Socket();
                byte[] buffer = new byte[1024];
                int len;
                try {
                    Toast.makeText(context, "Try connexion", Toast.LENGTH_SHORT).show();
                    //PROCEDURE D'ENVOI
                    socketServ.bind(null);
                    socketServ.connect(new InetSocketAddress(device.deviceAddress, 8888),500);

                    OutputStream outputStream = socketServ.getOutputStream();
                    ContentResolver contentResolver = context.getContentResolver();
                    InputStream inputStream = null;
                    //TODO: choisir le fichier à envoyer
                    inputStream = contentResolver.openInputStream(Uri.parse("image.png"));
                    while ((len = inputStream.read(buffer)) != -1){
                        outputStream.write(buffer,0,len);
                    }
                    outputStream.close();
                    inputStream.close();

                } catch (IOException e) {
                   e.printStackTrace();
                    Toast.makeText(context, "Catch", Toast.LENGTH_SHORT).show();
                }finally {
                    Toast.makeText(context, "Finally", Toast.LENGTH_SHORT).show();
                    /*if(socketServ != null){
                        if (socketServ.isConnected()){
                            try {
                                socketServ.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }*/
                }
                //TODO:Ajouter le device à une liste et l'afficher
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(context, "Impossible de se co à "+ device.deviceName, Toast.LENGTH_LONG).show();

            }
        });

    }
}