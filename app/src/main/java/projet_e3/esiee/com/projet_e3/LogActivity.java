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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LogActivity extends AppCompatActivity {

    public static boolean isLoggedInSpotify = false;
    private String PERSONAL = "personal.txt";

    TextView userName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_activity);

        userName = findViewById(R.id.userName);

        /* Lecture du pseudo */
        try {
            FileInputStream input = openFileInput(PERSONAL);
            int value;
            // On utilise un StringBuffer pour construire la chaîne au fur et à mesure
            StringBuffer lu = new StringBuffer();
            // On lit les caractères les uns après les autres
            while ((value = input.read()) != -1) {
                // On écrit dans le fichier le caractère lu
                lu.append((char) value);
            }
            userName.setText("Wesh " + lu.toString());
            if (input != null)
                input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageButton logoSpotify = findViewById(R.id.imageButton);
        logoSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!LogActivity.isLoggedInSpotify){
                    Intent intent = new Intent(LogActivity.this, LoginActivitySpotify.class);
                    startActivity(intent);
                }else
                    Toast.makeText(LogActivity.this,"Compte déjà connecté", Toast.LENGTH_LONG).show();
            }
        });

        Button buttonDisconnect = findViewById(R.id.buttonDisconnect);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LogActivity.isLoggedInSpotify){
                    LogActivity.isLoggedInSpotify = false;
                    Toast.makeText(LogActivity.this,"Déconnexion réussie", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(LogActivity.this,"Aucun compte n'est connecté", Toast.LENGTH_LONG).show();
                }
                TextView textSpotify = findViewById(R.id.textSpotify);
                textSpotify.setText("Non connecté");

            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!LogActivity.this.isOnline())
            Toast.makeText(LogActivity.this,"Aucune connexion internet détectée", Toast.LENGTH_LONG).show();

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