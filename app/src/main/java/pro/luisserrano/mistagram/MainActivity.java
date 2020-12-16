package pro.luisserrano.mistagram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.luisserrano.mistagram.databinding.ActivityMainBinding;
import pro.luisserrano.mistagram.ui.HomeFragment;
import pro.luisserrano.mistagram.ui.NotificationFragment;
import pro.luisserrano.mistagram.ui.ProfileFragment;
import pro.luisserrano.mistagram.ui.SearchFragment;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    private ActivityMainBinding binding;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Bundle intent = getIntent().getExtras();
        if (intent!=null) {
            String publisher = intent.getString( "publisherid" );

            //TODO(1): Escribir en las preferencias "PREFS" el par con clave "profileid" y con valor publisher DONE?
            SharedPreferences preferences = getApplicationContext().getSharedPreferences( "PREFS", Context.MODE_PRIVATE );
            preferences.getString( "profileid", publisher);

            getSupportFragmentManager().beginTransaction().replace( R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace( R.id.fragment_container,
                    new HomeFragment()).commit();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                   switch ( item.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            break;
                        case R.id.nav_add:
                            selectedFragment = null;
                            startActivity( new Intent( MainActivity.this, PostActivity.class ) );
                            break;
                        case R.id.nav_heart:
                            selectedFragment = new NotificationFragment();
                            break;
                        case R.id.nav_profile:
                            //TODO(2): Escribir en las preferencias "PREFS" el par con clave "profileid" y con valor el Uid del usuario de Firebase

                            SharedPreferences preferences = getApplicationContext().getSharedPreferences( "PREFS", Context.MODE_PRIVATE );
                            preferences.getString( "profileid", user.getUid());
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    if(selectedFragment != null){
                    //TODO(3): Si selectedFragment no es null entonces mostrar el dicho fragment en R.id.fragment_container DONE?
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        transaction.replace(R.id.fragment_container, selectedFragment);
                        transaction.addToBackStack(null);

                        transaction.commit();
                    }
                    return true;
                }
            };


}