package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;

public class HostMainActivity extends Activity {

    ListView userList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_main_activity);

        userList = findViewById(R.id.host_list);
        List<String> userRepertoire = new ArrayList<>();
        userRepertoire.add("User 1");
        userRepertoire.add("User 2");
        userRepertoire.add("User 3");

        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, userRepertoire);
        userList.setAdapter(userAdapter);
    }
}
