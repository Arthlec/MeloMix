package projet_e3.esiee.com.projet_e3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    Handler mHandler;

    public static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public static  int PORT = 8988;//8888;
    public static final String inetaddress = "inetaddress";
    public static final int ByteSize = 512;
    public static final String Extension = "extension";
    public static final String Filelength = "filelength";
    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mHandler = new Handler();
    }
    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @SuppressLint("ResourceType")
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {

            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                OutputStream stream = socket.getOutputStream();
                stream.flush();

                InputStream is = null;
                is = context.getResources().openRawResource(R.drawable.ic_launcher_background);//cr.openInputStream(Uri.parse(fileUri));

                assert is != null;
                GuestActivity.copyFile(is, stream);

            } catch (IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {

                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(FileTransferService.this, "Paired Device is not Ready to receive the file", Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}

