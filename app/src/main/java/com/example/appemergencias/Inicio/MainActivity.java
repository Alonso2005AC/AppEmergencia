package com.example.appemergencias.Inicio;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback; // Importación clave para manejo moderno
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
import com.example.appemergencias.fragments.MisReportesFragment;
import com.example.appemergencias.fragments.RegisterFragment;
// ***************************************************************
// 1. IMPORTACIÓN NECESARIA PARA EDITAR PERFIL
import com.example.appemergencias.fragments.EditarPerfilFragment;
// ***************************************************************
import com.google.android.material.navigation.NavigationView;

// IMPLEMENTACIÓN DE INTERFACES
public class MainActivity extends AppCompatActivity implements
        LoginFragment.OnLoginSuccessListener,
        RegisterFragment.OnRegistrationSuccessListener {

    private static final int CONTAINER_ID = R.id.nav_host_fragment;
    public static final String APP_TITLE = "AppEmergencias";

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

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(APP_TITLE);
        }

        // 1. MANEJO MODERNO DEL BOTÓN ATRÁS (Soluciona la advertencia/deprecación)
        setupOnBackPressed();

        // 2. Configuración del flujo de inicio
        if (isUserLoggedIn) {
            setupNavigationDrawer();
            if (savedInstanceState == null) {
                loadFragment(new HomeFragment(), false);
            }
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            if (savedInstanceState == null) {
                loadFragment(new LoginFragment(), false);
            }
        }
    }

    // Método que maneja la lógica de cerrar el Drawer al presionar Atrás (Moderno)
    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawers();
                } else {
                    // Si el Drawer está cerrado, permite que el sistema maneje la navegación normal
                    setEnabled(false);
                    MainActivity.super.onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void setupNavigationDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvUserName = headerView.findViewById(R.id.tv_user_name);
            String nombreUsuario = prefs.getString("usuario", "Usuario Invitado");
            tvUserName.setText(nombreUsuario);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
    // MÉTODOS DE NAVEGACIÓN
    // ======================================================================

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment nextFragment = null;

        if(id == R.id.nav_registrar) {
            nextFragment = new MenuFragment();
        }
        else if(id == R.id.nav_listar){
            nextFragment = new MisReportesFragment();
        }
        // ***************************************************************
        // LÓGICA DE NAVEGACIÓN A EDITAR PERFIL (USANDO ID SUGERIDO)
        else if(id == R.id.nav_editar_perfil){
            nextFragment = new EditarPerfilFragment();
        }
        // ***************************************************************
        else if(id == R.id.nav_cerrar){
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
        loadFragment(new RegisterFragment(), true);
    }

    @Override
    public void onRegistrationSuccessNavigateToLogin() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(new LoginFragment(), false);
        Toast.makeText(this, "¡Registro completado! Por favor, inicia sesión.", Toast.LENGTH_LONG).show();
    }
}