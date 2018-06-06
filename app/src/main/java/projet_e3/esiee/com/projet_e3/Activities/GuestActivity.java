package projet_e3.esiee.com.projet_e3.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import projet_e3.esiee.com.projet_e3.BroadCast;
import projet_e3.esiee.com.projet_e3.Services.DisconnectSignal;
import projet_e3.esiee.com.projet_e3.GuestClass;
import projet_e3.esiee.com.projet_e3.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GuestActivity extends AppCompatActivity {

    private ListView listView;
    private TextView TxtKiKi;
    private Button btnTry;
    private WifiP2pManager aManager;
    private WifiManager wifiManager;
    private WifiP2pManager.Channel aChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntent;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private ArrayAdapter<String> hAdapter;
    private ArrayList<String> devicename;
    private ArrayList<WifiP2pDevice> deviceArray;
    private InetAddress GoAdress;

    private final WifiP2pConfig config = new WifiP2pConfig();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosehost_activity);
        work();
        exqWork();
    }

    private void exqWork(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                final WifiP2pDevice device = deviceArray.get(i);
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
        btnTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discover();
            }
        });

    }

    private void deletePersistentGroup(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        method.invoke(aManager, aChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void work() {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        aManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        aChannel = aManager.initialize(this,getMainLooper(),null);

        deletePersistentGroup();
        aManager.removeGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "success suppro", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "fail suppr", Toast.LENGTH_SHORT).show();
            }
        });

        config.groupOwnerIntent = 0;

        mReceiver = new BroadCast(aManager,aChannel,this,null, wifiManager);
        mIntent = new IntentFilter();
        setAction();
        this.discover();

        listView = findViewById(R.id.HostList);
        TxtKiKi = findViewById(R.id.KieKi);
        btnTry = findViewById(R.id.TryBtn);
        btnTry.setText("Relancer la recherche de groupe");
    }

    public void setAction(){
        mIntent.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public WifiP2pManager.PeerListListener peerListListener =  new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersDevice) {
            if (!peersDevice.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peersDevice.getDeviceList());
                devicename = new ArrayList<String>();
                deviceArray = new ArrayList<WifiP2pDevice>();
                for(WifiP2pDevice device : peersDevice.getDeviceList())
                {
                    if(device.isGroupOwner())
                    {
                    devicename.add(device.deviceName);
                    deviceArray.add(device);
                    }
                }
                if(devicename!=null) {
                    hAdapter = new ArrayAdapter<>(listView.getContext(), android.R.layout.simple_list_item_1, devicename);
                    listView.setAdapter(hAdapter);
                }
            }
            if (peers.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Aucun appareil à proximité", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            GoAdress = info.groupOwnerAddress;
            if (info.groupFormed && !info.isGroupOwner) {
                TxtKiKi.setText("Guest");
                final String ipGuest = String.valueOf(wifiManager.getConnectionInfo().getIpAddress());
                GuestClass guestClass = new GuestClass(GoAdress, getApplicationContext(),ipGuest);
                guestClass.start();
            }
        }
    };
    public void discover(){
        aManager.discoverPeers(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "success disco", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason){
                Toast.makeText(getApplicationContext(), "fail disco", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void copyFile(InputStream inputStream, OutputStream out) {

        byte[] buf = new byte[8500];

        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            inputStream.close();
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public void DeathRattle(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        Intent serviceIntent = new Intent(getApplicationContext(), DisconnectSignal.class);
                        serviceIntent.setAction(DisconnectSignal.ACTION_SEND_DEATH);
                        String HostAdd = GoAdress.getHostAddress();
                        if (!TextUtils.isEmpty(HostAdd) && HostAdd.length() > 0) {
                            DisconnectSignal.PORT = 9899;
                            int sub_port = DisconnectSignal.PORT;
                            serviceIntent.putExtra(DisconnectSignal.EXTRAS_GROUP_OWNER_ADDRESS,HostAdd);
                            serviceIntent.putExtra(DisconnectSignal.EXTRAS_GROUP_OWNER_PORT, DisconnectSignal.PORT);

                            if (HostAdd != null && sub_port != -1) {
                                getApplication().startService(serviceIntent);
                            }
                        }
                    }
                }).start();
            }
        });
    }

    public void disconnect(){
        aManager.removeGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
              DeathRattle();
              Toast.makeText(getApplicationContext(),"Remove S",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(),"Remove F "+ i,Toast.LENGTH_SHORT).show();
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
}
