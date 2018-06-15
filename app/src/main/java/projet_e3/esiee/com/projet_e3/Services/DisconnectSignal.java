package projet_e3.esiee.com.projet_e3.Services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

public class DisconnectSignal extends IntentService{
        Handler mHandler;

        public static final int SOCKET_TIMEOUT = 5000;
        public static final String ACTION_SEND_DEATH = "com.example.android.wifidirect.SEND_DEATH";
        public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
        public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

        public static int PORT = 9899;

        public DisconnectSignal() {
            super("DisconnectSignal");
        }

        @Override
        public void onCreate() {
            super.onCreate();
            mHandler = new Handler();
        }

        @SuppressLint("ResourceType")
        @Override
        protected void onHandleIntent(Intent intent) {

            if (Objects.equals(intent.getAction(), ACTION_SEND_DEATH)) {

                String host = Objects.requireNonNull(intent.getExtras()).getString(EXTRAS_GROUP_OWNER_ADDRESS);
                Socket socket = new Socket();
                int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
                try {
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(DisconnectSignal.this, "L'hôte n'a pas été notifié de votre départ...", Toast.LENGTH_LONG).show();
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
