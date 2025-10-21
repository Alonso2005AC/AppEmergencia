package com.example.appemergencias.Inicio;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.appemergencias.fragments.HomeFragment;
import com.example.appemergencias.fragments.LoginFragment;
import com.example.appemergencias.R;
import com.example.appemergencias.fragments.MenuFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginSuccessListener {

    private static final int CONTAINER_ID = R.id.nav_host_fragment;
    public static final String APP_TITLE = "AppEmergencias"; // Título base

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("SESSION", MODE_PRIVATE);
        boolean isUserLoggedIn = prefs.contains("usuario");

        // 1. Inicializar componentes del menú lateral
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Establecer el título por defecto de la aplicación
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(APP_TITLE);
        }

        // 2. Configuración del flujo de inicio
        if (isUserLoggedIn) {
            setupNavigationDrawer();
            if (savedInstanceState == null) {
                loadFragment(new HomeFragment(), false);
            }
        } else {
            // Si NO está logueado, mostramos el LoginFragment
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            if (savedInstanceState == null) {
                loadFragment(new LoginFragment(), false);
            }
        }
    }

    private void setupNavigationDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        // Actualizar header con el nombre del usuario
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvUserName = headerView.findViewById(R.id.tv_user_name);
            String nombreUsuario = prefs.getString("usuario", "Usuario Invitado");
            tvUserName.setText(nombreUsuario);
        }

        // Configurar toggle del Drawer con la Toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Listener para los items del menú lateral
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    // Método central para cargar fragments
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(CONTAINER_ID, fragment);

        if (addToBackStack) {
            ft.addToBackStack(fragment.getClass().getSimpleName());
        }
        ft.commit();
    }

    // ======================================================================
    // MÉTODOS DE NAVEGACIÓN (Drawer y Login Success)
    // ======================================================================

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment nextFragment = null;

        if(id == R.id.nav_registrar) {
            nextFragment = new MenuFragment();
        }
        else if(id == R.id.nav_listar){
            Toast.makeText(this, "Lista de Emergencias (Fragmento Pendiente)", Toast.LENGTH_SHORT).show();
        } else if(id == R.id.nav_cerrar){
            prefs.edit().clear().apply();

            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            loadFragment(new LoginFragment(), false);

            drawerLayout.closeDrawers();
            return true;
        }

        if (nextFragment != null) {
            loadFragment(nextFragment, true);
        }

        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onLoginSuccessNavigateToMenu() {
        loadFragment(new HomeFragment(), false);
        setupNavigationDrawer();
    }

    @Override
    public void onRegisterClicked() {
        Toast.makeText(this, "Funcionalidad de Registro de Usuario No Disponible", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(navigationView)){
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}