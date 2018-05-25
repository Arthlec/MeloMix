package projet_e3.esiee.com.projet_e3;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
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

        Intent serviceIntent = new Intent(context, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        if (!TextUtils.isEmpty(HostAdd) && HostAdd.length() > 0) {
            String host = null;
            int sub_port = -1;
            FileTransferService.PORT = 8988;
            host = HostAdd;
            sub_port = FileTransferService.PORT;
            serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        HostAdd);

            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);

            if (host != null && sub_port != -1) {
                context.startService(serviceIntent);
            }
        }
    }
}