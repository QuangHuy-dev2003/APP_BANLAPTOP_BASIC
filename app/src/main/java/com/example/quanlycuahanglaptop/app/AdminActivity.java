package com.example.quanlycuahanglaptop.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.activity.OnBackPressedCallback;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.service.AuthService;
import com.example.quanlycuahanglaptop.util.CustomToast;
import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        setTitle(R.string.title_admin_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle back with OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });

        // Default content
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_content, new com.example.quanlycuahanglaptop.ui.admin.DashboardFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.menu_dashboard);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // onBackPressed is handled by OnBackPressedDispatcher above

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_dashboard) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_content, new com.example.quanlycuahanglaptop.ui.admin.DashboardFragment())
                    .commit();
        } else if (id == R.id.menu_products) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_content, new com.example.quanlycuahanglaptop.ui.admin.ProductsFragment())
                    .commit();
        } else if (id == R.id.menu_orders) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_content, new com.example.quanlycuahanglaptop.ui.admin.OrdersFragment())
                    .commit();
        } else if (id == R.id.menu_reports) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_content, new com.example.quanlycuahanglaptop.ui.admin.ReportsFragment())
                    .commit();
        } else if (id == R.id.menu_logout) {
            new AuthService(this).logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("SHOW_LOGOUT_SUCCESS", true);
            startActivity(intent);
            return true;
        }
        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }
}


