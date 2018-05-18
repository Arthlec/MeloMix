package projet_e3.esiee.com.projet_e3;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class GuestClass extends Thread {
    private Socket socket;
    private String HostAdd;
    private Context context;

    public GuestClass(InetAddress hostAdd, Context app) {
        HostAdd = hostAdd.getHostAddress();
        socket = new Socket();
        context = app;
    }

    @SuppressLint("ResourceType")
    @Override
    public void run() {
        byte[] buffer = new byte[8500];
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(HostAdd, 8988), 50000);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.flush();
            InputStream inputStream = null;
            File file = new File(String.valueOf(R.drawable.ic_launcher_background));//"/storage/download/IMG_4057.png");
            inputStream = context.getResources().openRawResource(R.drawable.ic_launcher_background); //cr.openInputStream(Uri.parse(file.getAbsolutePath()));

            int len;
            assert inputStream != null;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
    }
}