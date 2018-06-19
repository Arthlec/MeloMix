package projet_e3.esiee.com.projet_e3.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import projet_e3.esiee.com.projet_e3.AnalyseData;
import projet_e3.esiee.com.projet_e3.R;
import projet_e3.esiee.com.projet_e3.ReceiveDataFlow;

public class LoadingGuestActivity extends AnalyseData {
    private HostActivity hostActivity;
    private static List[] datalist;
    private static String bmp_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_guest);
        hostActivity = new HostActivity();
        ReceiveDataFlow receiveDataFlow = new ReceiveDataFlow(10014,"firstTimeCo",this);
        receiveDataFlow.start();
    }

    public static List[] getLoadingDatalist() {
        return datalist;
    }

    public void startHost()
    {
        Intent intent = new Intent(LoadingGuestActivity.this, hostActivity.getClass());
        intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
        intent.putExtra("host",0);
        intent.putExtra("bmp_url",bmp_string);
        Log.i("bmp",bmp_string);
        analyseData(this.getFilesDir());
        datalist = buildListTab();
        startActivity(intent);
    }


    public static class onSignalReceiveAsyncTask extends AsyncTask<String, String, String> {

        private int PORT;
        private String reason;
        private LoadingGuestActivity currentActivity;
        public onSignalReceiveAsyncTask(int port, String rString, LoadingGuestActivity theActivity) {
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
                    InputStream inputStream = client.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line).append('\n');
                    }
                    bmp_string = String.valueOf(total);
                    serverSocket.close();
                    return String.valueOf(total);
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
                    if(reason.equals("firstTimeCo")&&currentActivity!=null){
                        currentActivity.startHost();
                        Log.i("first",reason);
                    }
                    else{
                        HostActivity.setBmp(result);
                    }
                    Log.i("up",reason);
                    onSignalReceiveAsyncTask onSignalReceiveAsyncTask = new onSignalReceiveAsyncTask(10014,"upDate",null);
                    onSignalReceiveAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }
}
