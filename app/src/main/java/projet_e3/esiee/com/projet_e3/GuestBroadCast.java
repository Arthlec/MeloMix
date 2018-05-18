package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

public class GuestBroadCast extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private GuestActivity guestActivity;

    public GuestBroadCast(WifiP2pManager pmanager,WifiP2pManager.Channel pchannel,GuestActivity pmainActivity){
        this.manager = pmanager;
        this.channel = pchannel;
        this.guestActivity = pmainActivity;

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(context, "Wifi Direct utilisable !", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, "Wifi Direct non disponible !", Toast.LENGTH_SHORT).show();
            }

        }
        else if( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if(manager != null){
                manager.requestPeers(channel,guestActivity.peerListListener);
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(manager== null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            networkInfo.getDetailedState();
            if(networkInfo.isConnected()){
                manager.requestConnectionInfo(channel, guestActivity.connectionInfoListener);
            }
            else {
                guestActivity.getTxtState().setText("Disconnected");
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
        }
    }
}
