package com.parliamentary.androidapp.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.parliamentary.androidapp.FavouriteActivity;
import com.parliamentary.androidapp.MainActivity;
import com.parliamentary.androidapp.MpActivity;
import com.parliamentary.androidapp.ProfileActivity;
import com.parliamentary.androidapp.R;

/**
 * Created by jg413 on 18/01/2018.
 */

public class NavigationHelper implements BottomNavigationView.OnNavigationItemSelectedListener {

    Context context;

    public NavigationHelper(Context context) {
        this.context = context;
    }

    public void onBottomNavigationViewClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_profile:
                context.startActivity(new Intent(context, ProfileActivity.class));
                break;
            case R.id.navigation_list:
                context.startActivity(new Intent(context, MainActivity.class));
                break;
            case R.id.navigation_mp:
                context.startActivity(new Intent(context, MpActivity.class));
                break;
            case R.id.navigation_favourite:
                context.startActivity(new Intent(context, FavouriteActivity.class));
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        onBottomNavigationViewClick(item);
        return false;
    }
}