package projet_e3.esiee.com.projet_e3;

import java.io.IOException;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

public class WiFiClientIPTransferService extends IntentService{

    public WiFiClientIPTransferService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }
    public WiFiClientIPTransferService() {
        super("WiFiClientIPTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = MenuActivity.getGlobalContext();
        if (intent.getAction().equals(FileTransferService.ACTION_SEND_FILE)) {
            String host = intent.getExtras().getString(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS);
            String InetAddress =  intent.getExtras().getString(FileTransferService.inetaddress);

            Socket socket = new Socket();
            int port = intent.getExtras().getInt(FileTransferService.EXTRAS_GROUP_OWNER_PORT);

            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), FileTransferService.SOCKET_TIMEOUT);
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;

                ObjectOutputStream oos = new ObjectOutputStream(stream);
                WiFiTransferModal transObj = new WiFiTransferModal(InetAddress);

                oos.writeObject(transObj);
                System.out.println("Sending request to Socket Server");

                oos.close();	//close the ObjectOutputStream after sending data.
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

}
