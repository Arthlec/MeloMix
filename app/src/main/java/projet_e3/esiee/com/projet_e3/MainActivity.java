package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static boolean isLoggedInSpotify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton logoSpotify = findViewById(R.id.imageButton);
        logoSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.isLoggedInSpotify){
                    Intent intent = new Intent(MainActivity.this, LoginActivitySpotify.class);
                    startActivity(intent);
                }else
                    Toast.makeText(MainActivity.this,"Compte déjà connecté", Toast.LENGTH_LONG).show();
            }
        });

        Button buttonDisconnect = findViewById(R.id.buttonDisconnect);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.isLoggedInSpotify){
                    MainActivity.isLoggedInSpotify = false;
                    Toast.makeText(MainActivity.this,"Déconnexion réussie", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this,"Aucun compte n'est connecté", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!MainActivity.this.isOnline())
            Toast.makeText(MainActivity.this,"Aucune connexion internet détectée", Toast.LENGTH_LONG).show();

        TextView textSpotify = findViewById(R.id.textSpotify);
        String userName = this.getIntent().getStringExtra("userName");
        if(userName != null)
            textSpotify.setText(userName);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
