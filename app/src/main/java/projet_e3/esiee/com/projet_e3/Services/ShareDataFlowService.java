package projet_e3.esiee.com.projet_e3.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static final String EXTRAS_URL_BMP = "basic_bmp";
    public static final String EXTRAS_URL_NEXT_BMP = "basic_next_bmp";
    public static final String EXTRAS_BMP_NAME = "basic_bmp_name";
    public static final String EXTRAS_NEXT_BMP_NAME = "basic_next_bmp_name";
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
            String bmp = intent.getExtras().getString(EXTRAS_URL_BMP);
            String name_bmp = intent.getExtras().getString(EXTRAS_BMP_NAME);
            String next_bmp = intent.getExtras().getString(EXTRAS_URL_NEXT_BMP);
            String next_name = intent.getExtras().getString(EXTRAS_NEXT_BMP_NAME);

            String[] objects = new String[4];
            objects[0] = bmp;
            objects[1] = name_bmp;
            objects[2] = next_bmp;
            objects[3] = next_name;
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(target, port)), SOCKET_TIMEOUT);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.flush();

                assert bmp != null;
                InputStream stream = new ByteArrayInputStream(bmp.getBytes());
                sendObjet(stream,outputStream,bmp.getBytes().length);
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

    public static void sendObjet(InputStream inputStream, OutputStream out,int size) {

        byte[] buf = new byte[size];

        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            inputStream.close();
        } catch (IOException e) {
            e.getMessage();
        }
    }


}
