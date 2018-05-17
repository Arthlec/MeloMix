package projet_e3.esiee.com.projet_e3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SearchForGroupActivity extends AppCompatActivity {

    ListView hostListe = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchforgroup_activity);

        hostListe = findViewById(R.id.DJ_list);
        List<String> hostRepertoire = new ArrayList<>();
        hostRepertoire.add("Host 1");
        hostRepertoire.add("Host 2");
        hostRepertoire.add("Host 3");

        ArrayAdapter<String> hostAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, hostRepertoire);
        hostListe.setAdapter(hostAdapter);
    }
}
