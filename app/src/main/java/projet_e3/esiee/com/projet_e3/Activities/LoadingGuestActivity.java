package projet_e3.esiee.com.projet_e3.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import projet_e3.esiee.com.projet_e3.R;

public class LoadingGuestActivity extends AppCompatActivity {
    private HostActivity hostActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_guest);
        hostActivity = new HostActivity();
        startActivity();
    }

    public void startActivity()
    {
        Intent intent = new Intent(LoadingGuestActivity.this, hostActivity.getClass());
        intent.putExtra("authToken", getIntent().getStringExtra("authToken"));
        intent.putExtra("host",0);
        findViewById(R.id.loading_panel).setVisibility(View.VISIBLE);
        startActivity(intent);
    }
}
