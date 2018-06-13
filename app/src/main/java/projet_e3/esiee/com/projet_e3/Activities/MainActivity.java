package projet_e3.esiee.com.projet_e3.Activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import projet_e3.esiee.com.projet_e3.R;

public class MainActivity extends AppCompatActivity {

    private String defaut = "Veuillez entrer un pseudo valide";
    private Button mEnter = null;
    private String MY_PREFS = "my_prefs";
    private String userName = null;
    private boolean agreed = false;


    EditText pseudo = null;
    TextView result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //splash screen
        setTheme(R.style.MeloTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (myprefs_name() != null && myprefs_license()) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }

        if (!myprefs_license())
            showDialogue();

        //Récupération des vues
        pseudo = findViewById(R.id.pseudo);
        result = findViewById(R.id.result);

        pseudo.addTextChangedListener(textWatcher);

        /* use ENTER key on softkeyboard */

        pseudo.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("user_name", pseudo.getText().toString());
                            editor.apply();
                            if (myprefs_name() != null && myprefs_license()/*les conditions sont respectées*/) {
                                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                startActivity(intent);
                            } else if (!myprefs_license()) {
                                showDialogue();
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        mEnter = findViewById(R.id.enter);
        mEnter.setEnabled(false);
        mEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pView) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("user_name", pseudo.getText().toString());
                editor.apply();
                if (myprefs_name() != null && myprefs_license()/*les conditions sont respectées*/) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else if (!myprefs_license()) {
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
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Conditions d'utilisation")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setPositiveButton("J'accepte", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("agreed", true);
                            editor.apply();
                        }
                    })
                    .setNegativeButton("Je refuse", null)
                    .setMessage(readFile())
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile() throws IOException {
        Scanner scanner = null;
        String text = "";
        InputStream inputStream = this.getResources().openRawResource(R.raw.license_agreement);
        try {
            scanner = new Scanner(inputStream);
        } catch (Exception e) {
            //Send error message.
        }
        if (scanner != null) {
            while (scanner.hasNext()) {
                String[] lines = scanner.nextLine().split("\t");
                for (String line : lines) {
                    text += line + "\n";
                }
            }
        }
        return text;
    }

    public String myprefs_name() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        userName = pref.getString("user_name", null);//null is the default value.
        return userName;
    }

    private boolean myprefs_license() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        agreed = pref.getBoolean("agreed", false);//false is the default value.
        return agreed;
    }


}
