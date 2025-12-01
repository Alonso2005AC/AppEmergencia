package com.example.appemergencias.Inicio;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.appemergencias.R;
import com.example.appemergencias.fragments.HomeAdminFragment;
import com.example.appemergencias.fragments.LoginAdminFragment;
import com.example.appemergencias.fragments.MisReportesAdminFragment;
import com.google.android.material.navigation.NavigationView;

public class HomeAdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        // Toolbar
        toolbar = findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);

        // DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawer_layout_admin);
        navigationView = findViewById(R.id.nav_view_admin);

        // Configurar ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Listener para menú lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home_admin) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, new HomeAdminFragment())
                        .commit();
            } else if (id == R.id.nav_ver_reportes_admin) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, new MisReportesAdminFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (id == R.id.nav_graficos_admin) {
                // Aquí puedes agregar tu fragmento de gráficos si lo tienes
                Toast.makeText(this, "Sección de gráficos", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_cerrar_admin) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, new LoginAdminFragment())
                        .commit();
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Cargar fragmento inicial
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, new HomeAdminFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
