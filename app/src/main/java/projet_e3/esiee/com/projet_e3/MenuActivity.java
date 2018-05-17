package projet_e3.esiee.com.projet_e3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {
    Button Hbtn, Gbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setWork();
        exqBtn();

    }

    public void setWork() {
        Hbtn = findViewById(R.id.H_btn);
        Hbtn.setText("HOST");
        Gbtn = findViewById(R.id.G_btn);
        Gbtn.setText("GUEST");
    }

    public void exqBtn(){
        Hbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent HostIntent = new Intent(MenuActivity.this, HostActivity.class);
                startActivity(HostIntent);
            }
        });

        Gbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GuestIntent = new Intent(MenuActivity.this, GuestActivity.class);
                startActivity(GuestIntent);
            }
        });

    }

}
