package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        setupBottomNavigation();
        setupBackPressedHandler();
    }

    private void setupBackPressedHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Minimize the app instead of closing
                moveTaskToBack(true);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_income) {
                return true;
            } else if (itemId == R.id.nav_expense) {
                startActivity(new Intent(MainActivity2.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(MainActivity2.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MainActivity2.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_income);
    }
}