package projet_e3.esiee.com.projet_e3.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;

import java.io.File;
import java.util.HashMap;

import projet_e3.esiee.com.projet_e3.R;

public class ProfileActivity extends AppCompatActivity {

    public static boolean isLoggedInSpotify = false;
    private String PERSONAL = "personal.txt";
    private String authToken = "";

    TextView userName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!ProfileActivity.this.isOnline())
            Toast.makeText(ProfileActivity.this,"Aucune connexion internet détectée", Toast.LENGTH_LONG).show();
        setContentView(R.layout.profile_activity);

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
                if (!ProfileActivity.isLoggedInSpotify){
                    Intent intent = new Intent(ProfileActivity.this, LoginActivitySpotify.class);
                    startActivityForResult(intent, 1);
                }else
                    Toast.makeText(ProfileActivity.this,"Compte déjà connecté", Toast.LENGTH_LONG).show();
            }
        });

        ImageButton buttonDisconnect = findViewById(R.id.disconnectButton);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ProfileActivity.isLoggedInSpotify){
                    ProfileActivity.isLoggedInSpotify = false;
                    Toast.makeText(ProfileActivity.this,"Déconnexion réussie", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(ProfileActivity.this,"Aucun compte n'est connecté", Toast.LENGTH_LONG).show();
                }
                TextView textSpotify = findViewById(R.id.textSpotify);
                textSpotify.setText("Non connecté");

            }
        });

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChooseGroupActivity.class);
                intent.putExtra("authToken", authToken);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode == 1){
            Bundle bundle = intent.getExtras();
            HashMap<String, Float> userGenres = null;
            String userName = null;
            if(bundle != null) {
                userGenres = (HashMap<String, Float>) bundle.getSerializable("userGenres");
                userName = bundle.getString("userName", "Non connectée");
                authToken = bundle.getString("authToken", "");
            }

            TextView textSpotify = findViewById(R.id.textSpotify);
            //String userName = this.getIntent().getStringExtra("userName");
            //String userName = LoginActivitySpotify.userName;
            if(userName != null)
                textSpotify.setText("Connecté avec le compte : " + userName);

            //HashMap<String, Float> userGenres = this.getIntent().getSerializableExtra("genres");
            //HashMap<String, Float> userGenres = LoginActivitySpotify.userGenres;
            if(userGenres != null)
                this.writeJSONfile(userGenres);
        }
    }

    private void writeJSONfile(HashMap<String, Float> userGenres){
        try {
            //MainActivity.this.deleteFile("userGenres.json");
            File file = new File(this.getFilesDir(), "userGenres.json");

            Log.i("FileExists", "" + file.exists());
            //Log.i("FileIsHidden", "" + file.isHidden());

            JSON json = JSON.std.with(new JacksonJrsTreeCodec())
                    .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                    .without(JSON.Feature.WRITE_NULL_PROPERTIES);

            Log.i("prettyPrintEnabled", "" + json.isEnabled(JSON.Feature.PRETTY_PRINT_OUTPUT));

            json.write(userGenres, file);

            Log.i("FileLength", "" + file.length());
            Log.i("MainActivity", "Fichier créé !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}