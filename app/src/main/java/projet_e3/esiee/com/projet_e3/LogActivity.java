package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class LogActivity extends Activity {

    private String PERSONAL = "personal.txt";
    public static boolean isLoggedInSpotify = false;

    TextView userName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
                if (!MainActivity.isLoggedInSpotify) {
                    Intent intent = new Intent(LogActivity.this, LogSpotifyActivity.class);
                    startActivity(intent);
                } else
                    Toast.makeText(LogActivity.this, "Compte déjà connecté", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LogActivity.this.isOnline())
            Toast.makeText(LogActivity.this, "Aucune connexion internet détectée", Toast.LENGTH_LONG).show();

        TextView textSpotify = findViewById(R.id.textSpotify);
        String userName = this.getIntent().getStringExtra("userName");
        if (userName != null)
            textSpotify.setText(userName);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
