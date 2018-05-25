package projet_e3.esiee.com.projet_e3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ChooseGroupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosegroup_activity);

        Button buttonDJ = findViewById(R.id.DJ_button);
        buttonDJ.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseGroupActivity.this, HostActivity.class);
                intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                startActivity(intent);
            }
        });

        Button buttonSearchForGroup = findViewById(R.id.searchforgroup_button);
        buttonSearchForGroup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseGroupActivity.this, GuestActivity.class);
                intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
                startActivity(intent);
            }
        });
    }
}