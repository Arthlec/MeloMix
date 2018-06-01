package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ChooseGroupActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    private static Context context;

    public static synchronized Context getGlobalContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosegroup_activity);
        if (ChooseGroupActivity.context == null) {
            ChooseGroupActivity.context = getApplicationContext();
        }
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Button buttonDJ = findViewById(R.id.DJ_button);
        buttonDJ.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(isWifi()) {
                    enablewifi();
                }
                Intent intent = new Intent(ChooseGroupActivity.this, LoadingHostActivity.class);
                intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                intent.putExtra("host",1);
                startActivity(intent);
            }
        });

        Button buttonSearchForGroup = findViewById(R.id.searchforgroup_button);
        buttonSearchForGroup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(isWifi()) {
                    enablewifi();
                }
                Intent intent = new Intent(ChooseGroupActivity.this, GuestActivity.class);
                intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                intent.putExtra("host",0);
                startActivity(intent);
            }
        });
    }

    public boolean isWifi() {
        assert wifiManager != null;
        return !wifiManager.isWifiEnabled();
    }

    public void enablewifi(){
            wifiManager.setWifiEnabled(true);
            while (wifiManager.getWifiState()!= WifiManager.WIFI_STATE_ENABLED){
                Log.i("tag",wifiManager.getWifiState()+" "+ WifiManager.WIFI_STATE_ENABLED);
            }
    }
}
