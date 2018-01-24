package pt.ipca.tg_reservas;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //private TextView mTextMessage;
    Menu menu;
    MenuItem menuItem;
    private FirebaseAuth mAuth;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_pratos:
                    ChangeFragment(findViewById(R.id.navigation_pratos));
                    return true;
                case R.id.navigation_reserva:
                    ChangeFragment(findViewById(R.id.navigation_reserva));
                    return true;
                case R.id.navigation_historico:
                    ChangeFragment(findViewById(R.id.navigation_historico));
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null)
        {
            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        menu = navigation.getMenu();
        menuItem = menu.getItem(1);
        menuItem.setChecked(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void ChangeFragment(View view)
    {
        Fragment fragment;

        if (view == findViewById(R.id.navigation_pratos))
        {
            fragment = new PratosFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.replace(R.id.fragment, fragment);
            ft.commit();
            menuItem = menu.getItem(1);
            menuItem.setChecked(true);
        }

        if (view == findViewById(R.id.navigation_historico))
        {
            fragment = new HistoryFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.replace(R.id.fragment, fragment);
            ft.commit();
            menuItem = menu.getItem(0);
            menuItem.setChecked(true);
        }

        if (view == findViewById(R.id.navigation_reserva))
        {
            fragment = new ReservasFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.replace(R.id.fragment, fragment);
            ft.commit();
            menuItem = menu.getItem(2);
            menuItem.setChecked(true);
        }

    }

}
