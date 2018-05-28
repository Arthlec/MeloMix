package projet_e3.esiee.com.projet_e3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;
/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    Handler mHandler;

    public static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public static int PORT = 8988;

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (Objects.equals(intent.getAction(), ACTION_SEND_FILE)) {

            String host = Objects.requireNonNull(intent.getExtras()).getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                OutputStream outputStream = socket.getOutputStream();
                outputStream.flush();

                //File JsonFile = new File(this.getFilesDir(),"userGenres.json");
                //InputStream inputStream = new FileInputStream(JsonFile);
                InputStream inputStream = context.getResources().openRawResource(R.drawable.ic_launcher_background);

                assert inputStream != null;
                GuestActivity.copyFile(inputStream, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(FileTransferService.this, "Veuillez patienter votre fichier est en cours de traitement", Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
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

