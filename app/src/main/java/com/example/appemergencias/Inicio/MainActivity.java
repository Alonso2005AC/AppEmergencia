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

import com.example.appemergencias.R;
import com.example.appemergencias.fragments.HomeFragment;
import com.example.appemergencias.fragments.LoginFragment;
import com.example.appemergencias.fragments.LoginAdminFragment;
import com.example.appemergencias.fragments.MenuFragment;
import com.example.appemergencias.fragments.MisReportesFragment;
import com.example.appemergencias.fragments.MisReportesAdminFragment;
import com.example.appemergencias.fragments.AdminChartsFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements
        LoginFragment.OnLoginSuccessListener,
        LoginAdminFragment.OnAdminLoginSuccess {

    private static final int CONTAINER_ID = R.id.nav_host_fragment;
    public static final String APP_TITLE = "AppEmergencias";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("SESSION", MODE_PRIVATE);
        boolean isUserLoggedIn = prefs.contains("usuario");
        isAdmin = "admin".equals(prefs.getString("rol", ""));

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(APP_TITLE);
        }

        if (isUserLoggedIn) {
            setupNavigationDrawer();
            if (savedInstanceState == null) {
                if (isAdmin) {
                    loadFragment(new HomeFragment(), false);
                } else {
                    loadFragment(new HomeFragment(), false);
                }
            }
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            if (savedInstanceState == null) {
                loadFragment(new LoginFragment(), false);
            }
        }
    }

    private void setupNavigationDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        // Limpiar cualquier menú anterior
        navigationView.getMenu().clear();

        // Cambiar menú según rol
        if (isAdmin) {
            navigationView.inflateMenu(R.menu.drawer_menu_admin);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu);
        }

        // Actualizar header
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvUserName = headerView.findViewById(R.id.tv_user_name);
            String nombreUsuario = isAdmin ? "Admin" : prefs.getString("usuario", "Usuario Invitado");
            tvUserName.setText(nombreUsuario);
        }

        // **Recrear toggle para que aparezca el botón de hamburguesa**
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }


    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(CONTAINER_ID, fragment);
        if (addToBackStack) {
            ft.addToBackStack(fragment.getClass().getSimpleName());
        }
        ft.commit();
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment nextFragment = null;

        if (!isAdmin) {
            if (id == R.id.nav_registrar) nextFragment = new MenuFragment();
            else if (id == R.id.nav_listar) nextFragment = new MisReportesFragment();
            else if (id == R.id.nav_cerrar) {
                prefs.edit().clear().apply();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                loadFragment(new LoginFragment(), false);
                drawerLayout.closeDrawers();
                return true;
            }
        } else {
            // Admin menu
            if (id == R.id.nav_home_admin) nextFragment = new HomeFragment();
            else if (id == R.id.nav_ver_reportes_admin) nextFragment = new MisReportesAdminFragment();
            else if (id == R.id.nav_graficos_admin) nextFragment = new AdminChartsFragment();
            else if (id == R.id.nav_cerrar_admin) {
                prefs.edit().clear().apply();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                loadFragment(new LoginAdminFragment(), false);
                drawerLayout.closeDrawers();
                return true;
            }
        }

        if (nextFragment != null) loadFragment(nextFragment, true);
        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onLoginSuccessNavigateToMenu() {
        isAdmin = false;
        setupNavigationDrawer();
        loadFragment(new HomeFragment(), false);
    }

    @Override
    public void onLoginAdminSuccess() {
        isAdmin = true;
        setupNavigationDrawer();
        loadFragment(new HomeFragment(), false);
    }

    @Override
    public void onRegisterClicked() {
        Toast.makeText(this, "Funcionalidad de Registro de Usuario No Disponible", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}
