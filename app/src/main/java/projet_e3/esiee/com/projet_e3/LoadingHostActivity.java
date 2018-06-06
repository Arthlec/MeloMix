package projet_e3.esiee.com.projet_e3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class LoadingHostActivity extends AppCompatActivity {

    private ListView listView;
    private TextView TxtStatus;
    private Button NextBtn;
    private WifiP2pManager aManager;
    private WifiP2pManager.Channel aChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntent;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private List<WifiP2pDevice> PartyPeers = new ArrayList<>();
    private ArrayAdapter<String> hAdapter;
    private String[] deviceName;
    private WifiP2pDevice[] deviceArray;
    private final WifiP2pConfig config = new WifiP2pConfig();
    private static HashMap<String,String> DeviceDico = new HashMap<>();
    private static List<String> StackDevice = new ArrayList<>();
    private WifiP2pGroup wifiP2pGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_host);
        InitAttribut();
        InitOnClick();
    }

    private void InitAttribut() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiManager != null;
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        aManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        aChannel = aManager.initialize(this,getMainLooper(),null);

        deletePersistentGroup();

        config.groupOwnerIntent = 15;

        mReceiver = new BroadCast(aManager,aChannel,null,this, wifiManager);
        mIntent = new IntentFilter();
        setAction();

        this.createGrp();

        listView = findViewById(R.id.HostList);
        TxtStatus = findViewById(R.id.KieKi);
        NextBtn = findViewById(R.id.NextBtn);
    }

    public void setAction(){
        mIntent.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntent.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void InitOnClick() {

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

        NextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoadingHostActivity.this, HostActivity.class);
                intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                intent.putExtra("host",1);
                startActivity(intent);
            }
        });

    }

    private void deletePersistentGroup(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePersistentGroup")) {
                    for (int netid = 0; netid < 32; netid++) {
                        method.invoke(aManager, aChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> getDeviceDico() {
        return DeviceDico;
    }

    public static void setDeviceDico(String device,String ip) {
        DeviceDico.put(device,ip);
    }

    public static List<String> getStackDevice() {
        return StackDevice;
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            if(info.groupFormed && info.isGroupOwner){
                TxtStatus.setText("Host");
                HostClass hostClass = new HostClass(getApplicationContext());
                hostClass.start();
            }
        }
    };

    WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if(group!=null)
            {
                Toast.makeText(getApplicationContext(),group.getPassphrase(),Toast.LENGTH_SHORT).show();
                Log.i("pass",group.getPassphrase());
                wifiP2pGroup = group;
                try {
                    NetworkInterface networkInterface = NetworkInterface.getByName(wifiP2pGroup.getInterface());
                    List<InterfaceAddress> id = networkInterface.getInterfaceAddresses();
                    Log.i("Tg",id+"");
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                if(!wifiP2pGroup.getClientList().isEmpty())
                {
                    WifiP2pDevice device = wifiP2pGroup.getClientList().iterator().next();
                    Log.i("TT",wifiP2pGroup.getClientList().iterator().next()+"\n"+wifiP2pGroup.getClientList());
                    if(!PartyPeers.contains(device))
                    {PartyPeers.add(device);}
                    if(!StackDevice.contains(device.deviceAddress))
                    {StackDevice.add(device.deviceAddress);}
                }
                SuppressJson();
                aManager.requestConnectionInfo(aChannel,connectionInfoListener);
            }
        }
    };

    public void createGrp(){
        aManager.createGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {Toast.makeText(getApplicationContext(), "Succeed create", Toast.LENGTH_SHORT).show(); }

            @Override
            public void onFailure(int i) {Toast.makeText(getApplicationContext(), "fail creat", Toast.LENGTH_SHORT).show(); }
        });
    }

    public Stack<String> getAwayDevices(){
        Stack<String> AwayDevices = new Stack<>();
        Collection<WifiP2pDevice> clientList = wifiP2pGroup.getClientList();
        Log.i("AwayDevices",clientList+"   n");
        if(!PartyPeers.isEmpty()){
            for (WifiP2pDevice device : PartyPeers){
                if(!clientList.contains(device)) {
                    AwayDevices.add(device.deviceAddress);
                }
            }
        }
        return AwayDevices;
    }

    public void SuppressJson(){
        for(String device: getAwayDevices()){
            String Name = getDeviceDico().get(device);
            Log.i("AwayDevices",getDeviceDico()+"   n");
            File file =  new File(getApplicationContext().getFilesDir(),Name+".json");
            file.delete();
        }
    }

    public void disconnect(){
        aManager.removeGroup(aChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
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

    public static class FileServerAsyncTask extends AsyncTask<String, String, String> {

        private Context mFilecontext;
        private int PORT;
        private Socket CurrentClient;
        private static int instanceCount = 0;
        FileServerAsyncTask(Context context, int port,int instance) {
            this.mFilecontext = context;
            this.PORT = port;
            instanceCount = instance;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(PORT));

                Socket client = serverSocket.accept();
                CurrentClient = client;
                try {
                    String IpClient = client.getInetAddress().getHostAddress();
                   final File f = new File(mFilecontext.getFilesDir(), IpClient+".json");

                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();

                    InputStream inputstream = client.getInputStream();

                    copyFile(inputstream, new FileOutputStream(f));

                    serverSocket.close();
                    return f.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if(!getStackDevice().isEmpty()){
                    String IpClient = CurrentClient.getInetAddress().getHostAddress();
                    String device = getStackDevice().get(instanceCount);
                    getStackDevice().remove(instanceCount);
                    setDeviceDico(device,IpClient);
                    Log.i("r",getStackDevice()+"");
                    Log.i("dico",getDeviceDico()+"");
                    Toast.makeText(mFilecontext,"pas empty",Toast.LENGTH_SHORT).show();
                }
                else {Toast.makeText(mFilecontext,"EmptyStack",Toast.LENGTH_SHORT).show();}
                Toast.makeText(mFilecontext,"File transmis"+result,Toast.LENGTH_SHORT).show();

                if (!TextUtils.isEmpty(result)) {
                    FileServerAsyncTask FileServerobj = new
                            FileServerAsyncTask(mFilecontext, FileTransferService.PORT,instanceCount+1);
                    FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});
                }
            }
        }

        void copyFile(InputStream inputStream, OutputStream out) {

            byte buf[] = new byte[8500];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.close();
                inputStream.close();
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }
}
