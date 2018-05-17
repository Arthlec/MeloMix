package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

public class HostBroadCast extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private HostActivity hostActivity;


    public HostBroadCast(WifiP2pManager pmanager,WifiP2pManager.Channel pchannel,HostActivity pmainActivity){
        this.manager = pmanager;
        this.channel = pchannel;
        this.hostActivity = pmainActivity;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("p2p","receive");
        Toast.makeText(context, "receive", Toast.LENGTH_SHORT).show();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            Log.d("p2p","state change");
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(context, "Wifi Direct utilisable !", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, "Wifi Direct non disponible !", Toast.LENGTH_SHORT).show();
            }

        }
        else if( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(context, "peer change", Toast.LENGTH_SHORT).show();
            Log.d("p2p","peer change");
            if(manager != null){
                manager.requestPeers(channel,hostActivity.peerListListener);
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            Toast.makeText(context, "connection change", Toast.LENGTH_SHORT).show();
            Log.d("p2p","connection change");
            if(manager== null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            networkInfo.getDetailedState();
            Log.i("Network",networkInfo.getTypeName());
            if(networkInfo.isConnected()){
                manager.requestConnectionInfo(channel, hostActivity.connectionInfoListener);
            }
            else {
                hostActivity.getTxtState().setText("Disconnected");
            }

        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            Toast.makeText(context, "device change!", Toast.LENGTH_SHORT).show();
            Log.d("p2p","Device change");
        }
    }

}

