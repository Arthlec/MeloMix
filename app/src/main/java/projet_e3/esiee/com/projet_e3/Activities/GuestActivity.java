package projet_e3.esiee.com.projet_e3.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import projet_e3.esiee.com.projet_e3.R;


public class GuestActivity extends AppCompatActivity {

    ListView hostList = null;

    //Contient les users
    private String[] mHosts = {"Host 1","Host 2","Host 3"};
    /** Bouton pour choisir le host **/
    private Button mSend = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosehost_activity);

        hostList = findViewById(R.id.DJ_list);
        mSend = findViewById(R.id.send_button);


        //On ajoute un adaptateur qui affiche des boutons radio
        hostList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, mHosts));
        //On déclare qu'on sélectionne de base le premier élément
        hostList.setItemChecked(0, true);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GuestActivity.this, "Merci ! Les données ont été envoyées !", Toast.LENGTH_LONG).show();

                //On déclare qu'on ne peut plus sélectionner d'élément
                hostList.setChoiceMode(ListView.CHOICE_MODE_NONE);
                //On affiche un layout qui ne permet pas de sélection
                hostList.setAdapter(new ArrayAdapter<>(GuestActivity.this, android.R.layout.simple_list_item_1,
                        mHosts));

                //On désactive le bouton
                mSend.setEnabled(false);

            }
        });

        /* Voici une autre méthode mais laquelle utilisée?
        List<String> hostRepertoire = new ArrayList<>();
        hostRepertoire.add("Host 1");
        hostRepertoire.add("Host 2");
        hostRepertoire.add("Host 3");

        ArrayAdapter<String> hostAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, hostRepertoire);
        hostListe.setAdapter(hostAdapter);

        //ca marche pas ce truc
        hostListe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Que faire quand on clique sur un élément de la liste ?
            }
        });*/
    }
}
