package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
private Button buttonO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonO = (Button) findViewById(R.id.button_main);
        buttonO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent TransfertDataIntent = new Intent(MainActivity.this, TransfertData.class);
                startActivity(TransfertDataIntent);
            }
        });
    }


}
