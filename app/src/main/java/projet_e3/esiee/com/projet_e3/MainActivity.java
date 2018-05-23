package projet_e3.esiee.com.projet_e3;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private String defaut = "Veuillez entrer un pseudo valide";
    private String PERSONAL = "personal.txt";
    private Button mEnter = null;
    private boolean condition = false;


    EditText pseudo = null;
    TextView result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* On verfie si le fichier interne est nul
         * Si le fichier est nul l'utilisateur n'a pas de pseudo enregistré
         * Sinon il a deja un pseudo et passe à l'activité suivante*/
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
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showDialogue();

        //Récupération des vues
        pseudo = findViewById(R.id.pseudo);
        result = findViewById(R.id.result);

        pseudo.addTextChangedListener(textWatcher);

        mEnter = findViewById(R.id.enter);
        mEnter.setEnabled(false);
        mEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pView) {
                if (condition) {
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
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    showDialogue();
                }
            }
        });
    }


    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().trim().length() == 0) {
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

    private void showDialogue() {

        /* Boite de dialogue
         * S'ouvre seulement au permier lancement de l'application
         */
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean agreed = sharedPreferences.getBoolean("agreed",false);
        if (!agreed) {
            new AlertDialog.Builder(this)
                    .setTitle("License agreement")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("agreed", true);
                            editor.commit();
                            condition = true;
                        }
                    })
                    .setNegativeButton("No", null)
                    .setMessage("Le fichier.txt des conditions")
                    .show();
        }


        /*AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Conditions d'utilisation")
                .setMessage("Plein de conditions.txt")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // L'utilisateur accepte les conditions
                        condition = true;
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // L'utilisateur n'accepte pas les conditions
                        Toast.makeText(MainActivity.this, "Vous ne pouvez utilisé l'application Smooth-i si vous n'accepetez les conditions d'utilisations", Toast.LENGTH_LONG).show();
                        condition = false;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();*/
    }
}
