package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HostClass extends Thread{

    private ServerSocket serverSocket;
    private Context context;
    private String FileName;

    public HostClass(Context pCo, String file){
        context = pCo;
        FileName = file;
    }
    @Override
    public void run() {
        super.run();
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(8988));
            Socket client = serverSocket.accept();
            new HostActivity.FileServerAsyncTask(context, client,serverSocket,FileName).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}