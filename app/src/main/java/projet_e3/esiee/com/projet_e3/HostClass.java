package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HostClass extends Thread{

    private ServerSocket serverSocket;
    private Context context;
    private String FileName;
    private Socket client;


    public HostClass(Context pCo, String file){
        context = pCo;
        FileName = file;
    }
    @Override
    public void run() {
        super.run();
        new HostActivity.FileServerAsyncTask(context,8988).execute();
    }
}