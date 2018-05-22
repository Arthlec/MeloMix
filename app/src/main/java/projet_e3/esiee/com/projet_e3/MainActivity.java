package projet_e3.esiee.com.projet_e3;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String defaut = "Veuillez entrer un pseudo valide";
    private String PERSONAL = "personal.txt";
    private Button mEnter = null;

    EditText pseudo = null;
    TextView result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            if (input != null) {
                input.close();
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                startActivity(intent);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Récupération des vues
        pseudo = findViewById(R.id.pseudo);
        result = findViewById(R.id.result);

        pseudo.addTextChangedListener(textWatcher);

        mEnter = findViewById(R.id.enter);
        mEnter.setEnabled(false);
        mEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pView) {
                try {
                    /* Ecrit dans un fichier interne le pseudo de l'utilisateur */
                    // Flux interne
                    FileOutputStream output = openFileOutput(PERSONAL, MODE_PRIVATE);
                    // On écrit dans le flux interne
                    output.write(pseudo.getText().toString().getBytes());

                    if (output != null)
                        output.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });
    }


    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().trim().length()==0){
                mEnter.setEnabled(false);
            } else {
                mEnter.setEnabled(true);
                result.setText(defaut);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}
