package projet_e3.esiee.com.projet_e3.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

public class ShareDataFlowService extends IntentService {

    Handler mHandler;

    public static final int SOCKET_TIMEOUT = 50000;
    public static final String ACTION_SEND_FLOW = "com.example.android.wifidirect.SEND_DEATH";
    public static final String EXTRAS_TARGET_ADDRESS = "go_host";
    public static final String EXTRAS_TARGET_PORT = "go_port";

    public static int PORT = 10014;

    public ShareDataFlowService() {
        super("ShareDataFlowService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Objects.equals(intent.getAction(), ACTION_SEND_FLOW)) {

            String target = Objects.requireNonNull(intent.getExtras()).getString(EXTRAS_TARGET_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_TARGET_PORT);
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(target, port)), SOCKET_TIMEOUT);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(ShareDataFlowService.this, "Impossible de joindre la cible", Toast.LENGTH_LONG).show();
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
