package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GuestActivity extends AppCompatActivity {
    private Button buttonOnOff, buttonDisco, btnSend;
    private ListView listView;
    private TextView TxtState, TxtMsg, TxtKiKi;

    private WifiManager wifiManager;
    private WifiP2pManager aManager;
    private WifiP2pManager.Channel aChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntent;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private ArrayAdapter<String> hAdapter;
    private String[] deviceName;
    private WifiP2pDevice[] deviceArray;

    private final WifiP2pConfig config = new WifiP2pConfig();

    private HostClass hostClass;
    private GuestClass guestClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);
        work();
        exqWork();
    }

    private void exqWork(){
        buttonOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                    buttonOnOff.setText("TURN ON");
                    TxtKiKi.setText(" ");
                }
                else {
                    wifiManager.setWifiEnabled(true);
                    buttonOnOff.setText("TURN OFF");
                    TxtKiKi.setText(" ");
                }
            }
        });

        buttonDisco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aManager.discoverPeers(aChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        TxtState.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                        TxtState.setText("Discovery Failed");
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                final WifiP2pDevice device = deviceArray[i];
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                aManager.connect(aChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"connected to "+ device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(),"Fail connected to "+ device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



    }

    private void work() {
        buttonOnOff =(Button) findViewById(R.id.onOff);
        buttonDisco = findViewById(R.id.button2);
        listView = findViewById(R.id.ListTamere);
        TxtMsg = findViewById(R.id.msg_b);
        TxtState = findViewById(R.id.status_b);
        TxtKiKi = findViewById(R.id.KieKi);
        btnSend = findViewById(R.id.send);
        btnSend.setText("SHARE TASTE");
        btnSend.setEnabled(false);

        buttonDisco.setText("DISCOVER");

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        aManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        aChannel = aManager.initialize(this,getMainLooper(),null);

        config.groupOwnerIntent = 0;

        if (wifiManager.isWifiEnabled()){
            buttonOnOff.setText("WIFI is ON");
        }
        else    {
            buttonOnOff.setText("WIFI is OFF");
        }

        mReceiver = new GuestBroadCast(aManager,aChannel,this);
        mIntent = new IntentFilter();
        setAction();
    }

    public void setAction(){
        mIntent.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener =  new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersDevice) {
            if (!peersDevice.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peersDevice.getDeviceList());
                deviceName = new String[peersDevice.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peersDevice.getDeviceList().size()];
                int index =0;
                for(WifiP2pDevice device : peersDevice.getDeviceList())
                {
                    deviceName[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                hAdapter = new ArrayAdapter<String>(listView.getContext(),android.R.layout.simple_list_item_1,deviceName);
                listView.setAdapter(hAdapter);
            }
            if (peers.isEmpty()) {
                Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAdress = info.groupOwnerAddress;
            if (info.groupFormed) {
                TxtKiKi.setText("Guest");
                guestClass = new GuestClass(groupOwnerAdress, getApplicationContext());
                btnSend.setEnabled(true);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        guestClass.start();
                    }
                });
            }
        }
    };
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
    }

    public TextView getTxtState() {
        return TxtState;
    }

    public void disconnect() {
        if (aManager != null && aChannel != null) {
            aManager.requestGroupInfo(aChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && aManager != null && aChannel != null
                            && !group.isGroupOwner()) {
                        aManager.removeGroup(aChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(int reason) {
                            }
                        });
                    }
                }
            });
        }
    }
}