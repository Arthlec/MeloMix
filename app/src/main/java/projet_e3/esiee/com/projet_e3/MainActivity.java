package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton logoSpotify = findViewById(R.id.imageButton);
        logoSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivitySpotify.class);
                startActivity(intent);
            }
        });

        Toast.makeText(MainActivity.this, "MainActivity",
                Toast.LENGTH_LONG).show();

    }
}
