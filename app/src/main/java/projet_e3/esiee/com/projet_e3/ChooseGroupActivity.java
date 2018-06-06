package projet_e3.esiee.com.projet_e3;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

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
                /*Intent intent = new Intent(ChooseGroupActivity.this, LoadingHostActivity.class);
                intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                intent.putExtra("host", 1);
                startActivity(intent);*/
                showNumberPicker(v);
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
        Toast.makeText(this,
                "selected number " + numberPicker.getValue(), Toast.LENGTH_SHORT).show();
    }

    public void showNumberPicker(View view){
        NumberDialogFragment newFragment = new NumberDialogFragment();
        newFragment.setValueChangeListener(this);
        newFragment.show(getFragmentManager(), "time picker");
    }

    @SuppressLint("ValidFragment")
    public class NumberDialogFragment extends DialogFragment {
        private NumberPicker.OnValueChangeListener valueChangeListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final NumberPicker numberPicker = new NumberPicker(getActivity());
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(10);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("How many");
            builder.setMessage("Choose a number :");

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    valueChangeListener.onValueChange(numberPicker,
                            numberPicker.getValue(), numberPicker.getValue());
                    Intent intent = new Intent(ChooseGroupActivity.this, LoadingHostActivity.class);
                    intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                    intent.putExtra("host", 1);
                    intent.putExtra("guestNumber", numberPicker.getValue());
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
