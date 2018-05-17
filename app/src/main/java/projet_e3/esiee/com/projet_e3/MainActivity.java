package projet_e3.esiee.com.projet_e3;

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

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

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
                TextView textSpotify = findViewById(R.id.textSpotify);
                textSpotify.setText("Non connecté");
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
        //String userName = LoginActivitySpotify.userName;
        if(userName != null && !userName.equals(""))
            textSpotify.setText("Connecté avec le compte : " + userName);

        HashMap<String, Float> userGenres = (HashMap<String, Float>) this.getIntent().getSerializableExtra("genres");
        //HashMap<String, Float> userGenres = LoginActivitySpotify.userGenres;
        if(userGenres != null)
            this.writeJSONfile(userGenres);
    }

    private void writeJSONfile(HashMap<String, Float> userGenres){
        //String json = JSON.std.asString(map);
        try {
            File file = new File(this.getFilesDir(), "userGenres");
            //JSON.std.write(userGenres, file);
            //file.delete();
            Log.i("FileExist", "" + file.exists());
            Log.i("FileIsHidden", "" + file.isHidden());

            JSON json = JSON.std.with(new JacksonJrsTreeCodec())
                    .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                    .without(JSON.Feature.WRITE_NULL_PROPERTIES);
            if(file.canWrite())
                json.write(userGenres, file);

            file.setReadable(true);
            Log.i("FileToString", file.toString());
            Log.i("FileToString", "" + file.length());
            Log.i("MainActivity", "Fichier créé !");

            FileInputStream inputStream;
            inputStream = openFileInput("userGenres");
            byte[] bytes = new byte[12288];
            if(file.canRead())
                inputStream.read(bytes);
            //byte[] bytes = doc.getBytes("UTF-8");
            String userGenresString = new String(bytes, "UTF-8");
            Log.i("userGenres", userGenresString);
            inputStream.close();
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
