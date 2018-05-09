package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
private Button buttonG;
private Button buttonH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonH = (Button) findViewById(R.id.button_H);
        buttonG = (Button) findViewById(R.id.button_G);

        buttonH.setText("HOST");
        buttonG.setText("GUEST");

        buttonG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent TransfertDataIntent = new Intent(MainActivity.this, TransfertData.class);
                startActivity(TransfertDataIntent);
            }
        });

        buttonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent TransfertDataIntent = new Intent(MainActivity.this, TransfertData.class);
                startActivity(TransfertDataIntent);
            }
        });
    }


}
