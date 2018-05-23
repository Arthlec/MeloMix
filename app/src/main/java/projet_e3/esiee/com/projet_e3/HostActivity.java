package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.lang.System;

public class HostActivity extends AppCompatActivity {

    private Button buttonOnOff, buttonDisco;
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

    private String FileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        work();
        exqWork();
    }

    private void exqWork() {
        buttonOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                    buttonOnOff.setText("TURN ON");
                }
                else {
                    wifiManager.setWifiEnabled(true);
                    buttonOnOff.setText("TURN OFF");
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

    private void deletePersistentGroup(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(aManager, aChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void work() {
        buttonOnOff =(Button) findViewById(R.id.onOff);
        buttonDisco = findViewById(R.id.button2);
        listView = findViewById(R.id.ListTamere);
        TxtMsg = findViewById(R.id.msg_b);
        TxtState = findViewById(R.id.status_b);
        TxtKiKi = findViewById(R.id.KieKi);

        buttonDisco.setText("DISCOVER");
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        aManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        aChannel = aManager.initialize(this,getMainLooper(),null);
        deletePersistentGroup();
        aManager.removeGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });

        config.groupOwnerIntent = 15;


        if (wifiManager.isWifiEnabled()){
            buttonOnOff.setText("WIFI is ON");
            TxtKiKi.setText(" ");
        }
        else    {
            buttonOnOff.setText("WIFI is OFF");
            TxtKiKi.setText(" ");
        }

        mReceiver = new HostBroadCast(aManager,aChannel,this);
        mIntent = new IntentFilter();
        setAction();

        aManager.createGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"Group create ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(),"Fail create group ", Toast.LENGTH_SHORT).show();
            }
        });

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
                    //if(!device.isGroupOwner()) {
                        deviceName[index] = device.deviceName;
                        deviceArray[index] = device;
                    //}
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
            if(info.groupFormed && info.isGroupOwner){
                TxtKiKi.setText("Host");
                FileName = String.valueOf(System.currentTimeMillis());
                hostClass = new HostClass(getApplicationContext(), FileName);
                hostClass.start();
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
        aManager.removeGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

    public TextView getTxtState() {
        return TxtState;
    }

    public static class FileServerAsyncTask extends AsyncTask<Void,Void,String> {

        private Context context;
        private  Socket client;
        private ServerSocket serverSocket;
        private  String theFile;
        private HostClass host;

        public FileServerAsyncTask(Context context, Socket soc,ServerSocket serv, String file) {
            this.context = context;
            this.client = soc;
            this.serverSocket = serv;
            this.theFile = file;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                final File f = new File(context.getFilesDir(),theFile+".xml");

                File dirs = new File(f.getParent());
                if (!dirs.exists()){
                    dirs.mkdirs();
                }
                f.createNewFile();

                f.setReadable(true,true);
                f.setWritable(true,true);

                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();

                return f.getAbsolutePath();
            } catch (SocketException e){
                e.getMessage();
                return  null;
            } catch (IOException e) {
                e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(context,"File copied - " + result,Toast.LENGTH_SHORT).show();
            }
        }


        public static boolean copyFile(InputStream inputStream, OutputStream out) {

            byte buf[] = new byte[8500];
            int len;
            long startTime=System.currentTimeMillis();

            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.close();
                inputStream.close();
                long endTime=System.currentTimeMillis()-startTime;
                Log.v("","Time taken to transfer all bytes is : "+endTime);

            } catch (IOException e) {
                Log.d("exp", e.toString());
                return false;
            }
            return true;
        }
    }
}
