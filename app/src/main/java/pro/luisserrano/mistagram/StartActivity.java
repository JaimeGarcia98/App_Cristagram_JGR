package pro.luisserrano.mistagram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.luisserrano.mistagram.databinding.ActivityStartBinding;

public class StartActivity extends AppCompatActivity {

    Button login, register;
    FirebaseUser firebaseUser;
    private ActivityStartBinding binding;

    @Override
    protected void onStart() {
        super.onStart();

        //TODO(31) Inicializar firebaseUser al actual usuario de Firebase, si el usuario está autenticado (no es null) lanzar la actividad MainActivity y finalizar la actividad actual DONE.
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(main);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        //TODO(32): Crear dos listener para el evento click para login y register que lleven a LoginActivity y RegisterActivity respectivamente DONE

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(log);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(reg);
            }
        });
    }
}