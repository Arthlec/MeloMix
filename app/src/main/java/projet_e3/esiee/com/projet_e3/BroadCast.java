package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import projet_e3.esiee.com.projet_e3.Activities.GuestActivity;
import projet_e3.esiee.com.projet_e3.Activities.HostActivity;
import projet_e3.esiee.com.projet_e3.Activities.LoadingHostActivity;

/**
 * Permet de sonder tous les changements d'états Peer to Peer
 */
public class BroadCast extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private GuestActivity guestActivity;
    private LoadingHostActivity loadingHost;
    private WifiManager wifiManager;

    public BroadCast(WifiP2pManager pmanager, WifiP2pManager.Channel pchannel, GuestActivity pGuest,LoadingHostActivity pLoadingHost, WifiManager wifi){

        this.manager = pmanager;
        this.channel = pchannel;
        this.guestActivity = pGuest;
        this.loadingHost = pLoadingHost;
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
                if(guestActivity != null && loadingHost == null){
                    manager.requestPeers(channel,guestActivity.peerListListener);
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
                if(guestActivity != null && loadingHost == null) {
                    manager.requestConnectionInfo(channel, guestActivity.connectionInfoListener);
                }else if(loadingHost != null && guestActivity == null) {
                    manager.requestGroupInfo(channel,loadingHost.groupInfoListener);
                }
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            if(guestActivity != null && loadingHost == null) {
                Log.i("DEVICE guest","Devices changed "+action);
            }else if(loadingHost != null && guestActivity == null) {
                Log.i("DEVICE Host","Devices changed host "+action);
            }

        }
    }
}
