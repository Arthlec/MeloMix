package projet_e3.esiee.com.projet_e3.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import projet_e3.esiee.com.projet_e3.R;

public class ChooseGroupActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosegroup_activity);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Button buttonDJ = findViewById(R.id.DJ_button);
        buttonDJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWifi()) {
                    enablewifi();
                }

                showNumberPicker(v);
            }
        });

        Button buttonInformation = findViewById(R.id.buttonInfo);
        buttonInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(ChooseGroupActivity.this).create();
                alertDialog.setTitle("Information");
                alertDialog.setMessage("Si vous choisissez d'organiser l'évènement, les autres utilisateurs participants se connecteront à votre appareil. Vous serez le propriétaire de l'évènement. \n Les participants ne peuvent se connecter qu'à un seul organisateur.");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        Button buttonSearchForGroup = findViewById(R.id.searchforgroup_button);
        buttonSearchForGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWifi()) {
                    enablewifi();
                }
                Intent intent = new Intent(ChooseGroupActivity.this, GuestActivity.class);
                intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                intent.putExtra("host", 0);
                startActivity(intent);
            }
        });
    }

    public boolean isWifi() {
        assert wifiManager != null;
        return !wifiManager.isWifiEnabled();
    }

    public void enablewifi() {
        wifiManager.setWifiEnabled(true);
        while (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            Log.i("tag", wifiManager.getWifiState() + " " + WifiManager.WIFI_STATE_ENABLED);
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
    }

    public void showNumberPicker(View view){
        NumberDialogFragment newFragment = new NumberDialogFragment();
        newFragment.setValueChangeListener(this);
        newFragment.show(getFragmentManager(), "time picker");
    }

    public static class NumberDialogFragment extends DialogFragment {
        private NumberPicker.OnValueChangeListener valueChangeListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final NumberPicker numberPicker = new NumberPicker(getActivity());
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(10);
            int maxValue = numberPicker.getMaxValue()-numberPicker.getMinValue()+1;
            String[] displayedValues = new String[maxValue];
            String texte = "";
            for (int i=0;i<maxValue;i++)
            {
                if(i<2)
                    texte = " invité(e)";
                else
                    texte = " invité(e)s";
                displayedValues[i] = i+texte;
            }
            numberPicker.setDisplayedValues(displayedValues);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Nombre minimum d'invités avant de lancer la musique");
            builder.setMessage("A partir de combien d'invités voulez-vous commencer le traitement ? (d'autres personnes pourront vous rejoindre par la suite)");

            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    valueChangeListener.onValueChange(numberPicker,
                            numberPicker.getValue(), numberPicker.getValue());

                        Intent intent = new Intent(getActivity().getApplicationContext()/*ChooseGroupActivity.this*/, LoadingHostActivity.class);
                        intent.putExtra("authToken", getActivity().getIntent()/*getIntent()*/.getStringExtra("authToken"));
                        intent.putExtra("availableGenres", getActivity().getIntent()/*getIntent()*/.getStringArrayListExtra("availableGenres"));
                        intent.putExtra("host", 1);
                        intent.putExtra("guestNumber", numberPicker.getValue());
                        startActivity(intent);
                }
            });
            builder.setNegativeButton("annuler", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    valueChangeListener.onValueChange(numberPicker,
                            numberPicker.getValue(), numberPicker.getValue());
                    NumberDialogFragment.this.getDialog().cancel();
                }
            });

            builder.setView(numberPicker);
            return builder.create();
        }

        public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
            this.valueChangeListener = valueChangeListener;
        }

    }
}
