package projet_e3.esiee.com.projet_e3.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import projet_e3.esiee.com.projet_e3.AnalyseData;
import projet_e3.esiee.com.projet_e3.R;

public class LoadingGuestActivity extends AnalyseData {
    private HostActivity hostActivity;
    private static List[] datalist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_guest);
        hostActivity = new HostActivity();

        onSignalReceiveAsyncTask onSignalReceiveAsyncTask = new onSignalReceiveAsyncTask(getApplicationContext(),10014,"firstTimeCo",this);
        onSignalReceiveAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static List[] getLoadingDatalist() {
        return datalist;
    }

    public void startHost()
    {
        Intent intent = new Intent(LoadingGuestActivity.this, hostActivity.getClass());
        intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
        intent.putExtra("host",0);
        analyseData(this.getFilesDir());
        datalist = buildListTab();
        startActivity(intent);
    }


    @SuppressLint("StaticFieldLeak")
    public static class onSignalReceiveAsyncTask extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        private Context mFilecontext;
        private int PORT;
        private String reason;
        private LoadingGuestActivity currentActivity;
        onSignalReceiveAsyncTask(Context context, int port, String rString, LoadingGuestActivity theActivity) {
            this.mFilecontext = context;
            this.PORT = port;
            this.reason = rString;
            currentActivity = theActivity;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(PORT));

                Socket client = serverSocket.accept();
                try {
                    //Recupère l'Ip du client connecté à la socket et supprime le JSON correspondant
                    String IpClient = client.getInetAddress().getHostAddress();
                    serverSocket.close();
                    return reason;
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
                if (!TextUtils.isEmpty(result)) {
                    if(result.equals("firstTimeCo")&&currentActivity!=null){
                        currentActivity.startHost();
                    }
                    onSignalReceiveAsyncTask onSignalReceiveAsyncTask = new onSignalReceiveAsyncTask(mFilecontext,10014,"upDate",null);
                    onSignalReceiveAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }
}
