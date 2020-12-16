package pro.luisserrano.mistagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import pro.luisserrano.mistagram.databinding.ActivityOptionsBinding;

public class OptionsActivity extends AppCompatActivity {

    TextView logout,settings;
    ActivityOptionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityOptionsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView( view );

        setSupportActionBar( binding.toolbar );
        getSupportActionBar().setTitle( getString(R.string.options) );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        binding.toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );

        binding.logout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity( new Intent( pro.luisserrano.mistagram.OptionsActivity.this,StartActivity.class )
                .setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP ));
            }
        } );

    }
}