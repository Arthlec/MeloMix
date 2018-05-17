package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HostClass extends Thread{
    Socket socket;
    ServerSocket serverSocket;
    View view;
    Context context;

    public HostClass(View view1, Context pCo){
        view = view1;
        context = pCo;
    }
    @Override
    public void run() {
        super.run();
        try {
            serverSocket = new ServerSocket(8988);
            socket = serverSocket.accept();
            new HostActivity.FileServerAsyncTask(context,view).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}