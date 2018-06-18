package projet_e3.esiee.com.projet_e3.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.HashMap;

import projet_e3.esiee.com.projet_e3.R;

public class ProfileActivity extends AppCompatActivity {

    //public static boolean isLoggedInSpotify = false;
    //private String PERSONAL = "personal.txt";
    private String authToken = "";
    private ArrayList<String> availableGenresList;
    private String MY_PREFS = "my_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);

        availableGenresList = new ArrayList<>();

        TextView userName = findViewById(R.id.userName);
        userName.setText("Bonjour " +myprefs_name());

        TextView textSpotify = findViewById(R.id.textSpotify);
        if (pref.contains("userAccountSpotify")){
            textSpotify.setText("Connecté avec le compte : " + myprefs_accountSpotify());
        }else
            textSpotify.setText("Non connecté");

        ImageButton logoSpotify = findViewById(R.id.imageButton);
        logoSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                if(isConnectivityOn())
                {
                    if (!pref.contains("userAccountSpotify")){
                        Intent intent = new Intent(ProfileActivity.this, LoginActivitySpotify.class);
                        startActivityForResult(intent, 1);
                    }else
                        Toast.makeText(ProfileActivity.this,"Compte déjà connecté", Toast.LENGTH_LONG).show();
                    /*else{
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.spotify.com"));
                        startActivity(browserIntent);
                    }*/
                }else {
                    Toast.makeText(ProfileActivity.this,"Aucune connexion internet détectée", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton buttonDisconnect = findViewById(R.id.disconnectButton);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                if(pref.contains("userAccountSpotify")){
                    pref.edit().remove("userAccountSpotify").apply(); //clear pref user account Spotify
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
                intent.putStringArrayListExtra("availableGenres", availableGenresList);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode == 1){
            Bundle bundle = intent.getExtras();
            HashMap<String, Float> userGenres = null;
            String userAccountSpotify = null;
            if(bundle != null) {
                userGenres = (HashMap<String, Float>) bundle.getSerializable("userGenres");
                userAccountSpotify = bundle.getString("userAccountSpotify", "");
                authToken = bundle.getString("authToken", "");
                availableGenresList = bundle.getStringArrayList("availableGenres");
            }

            if(userAccountSpotify != null){
                TextView textSpotify = findViewById(R.id.textSpotify);
                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("userAccountSpotify", userAccountSpotify);
                editor.apply();
                textSpotify.setText("Connecté avec le compte : " + myprefs_accountSpotify());
            }

            //HashMap<String, Float> userGenres = this.getIntent().getSerializableExtra("genres");
            //HashMap<String, Float> userGenres = LoginActivitySpotify.userGenres;
            if(userGenres != null)
                this.writeJSONfile(userGenres);
        }
    }

    private boolean isConnectivityOn(){
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private void writeJSONfile(HashMap<String, Float> userGenres){
        if(!userGenres.isEmpty()){
            try {
                //MainActivity.this.deleteFile("userGenres.json");
                File file = new File(this.getFilesDir(), "userGenres.json");

                JSON json = JSON.std.with(new JacksonJrsTreeCodec())
                        .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                        .without(JSON.Feature.WRITE_NULL_PROPERTIES);

                json.write(userGenres, file);
                Log.i("FileLength", "" + file.length());
                Log.i("MainActivity", "Fichier créé !");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String myprefs_name() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        return pref.getString("user_name", null);//null is the default value.
    }

    private String myprefs_accountSpotify() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        return pref.getString("userAccountSpotify", getString(R.string.non_connect));
    }
}