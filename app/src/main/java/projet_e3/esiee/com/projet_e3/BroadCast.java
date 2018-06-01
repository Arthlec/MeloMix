package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Permet de sonder tous les changements d'états Peer to Peer
 */
public class BroadCast extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private GuestActivity guestActivity;
    private LoadingHostActivity host;
    private WifiManager wifiManager;

    public BroadCast(WifiP2pManager pmanager, WifiP2pManager.Channel pchannel, GuestActivity pGuest,LoadingHostActivity pHost, WifiManager wifi){

        this.manager = pmanager;
        this.channel = pchannel;
        this.guestActivity = pGuest;
        this.host = pHost;
        this.wifiManager = wifi;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //Changement de l'état du WIFIp2p
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                wifiManager.setWifiEnabled(true);
            }
        }
        else if( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //Détection d'appareil à proximité
            if(manager != null){
                if(guestActivity != null && host == null){
                    manager.requestPeers(channel,guestActivity.peerListListener);
                }
                else if(host != null && guestActivity == null) {
                    manager.requestPeers(channel, host.peerListListener);
                }
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //Detection d'une connexion et déco
            if(manager== null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            networkInfo.getDetailedState();
            if(networkInfo.isConnected()){
                if(guestActivity != null && host == null) {
                    manager.requestConnectionInfo(channel, guestActivity.connectionInfoListener);
                }else if(host != null && guestActivity == null) {
                    manager.requestConnectionInfo(channel, host.connectionInfoListener);
                }
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            Log.i("DEVICE CHANGES","Devices changed action");
        }
    }
}
