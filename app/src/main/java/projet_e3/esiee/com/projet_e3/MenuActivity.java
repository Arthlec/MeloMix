package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {
    Button Hbtn, Gbtn;
    WifiManager wifiManager;
    Intent intent;
    private static Context context;
    public static synchronized Context getGlobalContext() {
        return context;
    }
    private int state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        if (MenuActivity.context == null) {
            MenuActivity.context = getApplicationContext();
        }
        setWork();
        exqBtn();
    }

    public void setWork() {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Hbtn = findViewById(R.id.H_btn);
        Hbtn.setText("Je crée mon groupe");
        Gbtn = findViewById(R.id.G_btn);
        Gbtn.setText("Je rejoins un groupe");
    }

    public void exqBtn(){
        Hbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isWifi()) {
                    wifiManager.setWifiEnabled(true);
                    /*while (wifiManager.getWifiState()!=wifiManager.WIFI_STATE_ENABLED){
                        Log.i("tag",wifiManager.getWifiState()+" "+wifiManager.WIFI_STATE_ENABLED);
                    }*/
                }
                Intent HostIntent = new Intent(MenuActivity.this, HostActivity.class);
                startActivity(HostIntent);
            }
        });

        Gbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isWifi()){
                    wifiManager.setWifiEnabled(true);
                    while (wifiManager.getWifiState()!=wifiManager.WIFI_STATE_ENABLED){Log.i("tag",wifiManager.getWifiState()+" "+wifiManager.WIFI_STATE_ENABLED);}
                }
                Intent GuestIntent = new Intent(MenuActivity.this, GuestActivity.class);
                startActivity(GuestIntent);
            }
        });

    }

    public boolean isWifi() {
        assert wifiManager != null;
        return wifiManager.isWifiEnabled();
    }
}
