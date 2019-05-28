package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.example.hashpotatoesv20.Alert.AlertActivity;
import com.example.hashpotatoesv20.Feature.FeatureActivity;
import com.example.hashpotatoesv20.Main.MainActivity;
import com.example.hashpotatoesv20.Profile.ProfileActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.example.hashpotatoesv20.R;

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.ic_home:
                        Intent intent1 = new Intent(context, MainActivity.class);
                        context.startActivity(intent1);
                        break;

                    case R.id.ic_featured:
                        Intent intent2 = new Intent(context, FeatureActivity.class);
                        context.startActivity(intent2);
                        break;

                    case R.id.ic_alert:
                        Intent intent3 = new Intent(context, AlertActivity.class);
                        context.startActivity(intent3);
                        break;

                    case R.id.ic_profile:
                        Intent intent4 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent4);
                        break;
                }

                return false;
            }
        });
    }
}
