package com.parliamentary.androidapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.parliamentary.androidapp.adapters.CommonsDivisionsAdapter;
import com.parliamentary.androidapp.data.AsyncResponse;
import com.parliamentary.androidapp.helpers.NavigationHelper;
import com.parliamentary.androidapp.models.CommonsDivision;
import com.parliamentary.androidapp.tasks.GetCommonsDivisionTask;
import com.parliamentary.androidapp.tasks.GetMpCommonsDivisionsTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.support.design.widget.BottomNavigationView.GONE;
import static android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import static android.support.design.widget.BottomNavigationView.VISIBLE;

public class FavouriteActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private View favProgressBar;
    private TextView progressBarText;
    private HashMap<String, Long> favourites;
    private String TAG = FavouriteActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        favProgressBar = findViewById(R.id.favouriteProgressBar);
        progressBarText = favProgressBar.findViewById(R.id.progressBarText);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        OnNavigationItemSelectedListener onNavigationItemSelectedListener = new NavigationHelper(this);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        navigation.getMenu().getItem(2).setChecked(true);

        getFavourites();
    }

    private void getFavourites() {
        favProgressBar.setVisibility(VISIBLE);
        progressBarText.setText("Getting User Favourites...");
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        // Read from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ListView listView = findViewById(R.id.favouritesListView);
                if (!dataSnapshot.child(user.getUid()).child("favourites").exists()) {
                    listView.setVisibility(GONE);
                    TextView favInfoTextView = findViewById(R.id.favInfoTextView);
                    favInfoTextView.setText("No favourites found");
                    favInfoTextView.setVisibility(VISIBLE);
                    favProgressBar.setVisibility(GONE);
                    return;
                }
                GenericTypeIndicator<HashMap<String, Long>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Long>>() {
                };
                favourites = dataSnapshot.child(user.getUid()).child("favourites").getValue(genericTypeIndicator);
                if (favourites == null || favourites.isEmpty()) {
                    listView.setVisibility(GONE);
                    TextView favInfoTextView = findViewById(R.id.favInfoTextView);
                    favInfoTextView.setText("No favourites found...");
                    favInfoTextView.setVisibility(VISIBLE);
                } else {
                    getSingleCommonsDivisions();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void getSingleCommonsDivisions() {
        final ArrayList<CommonsDivision> commonsDivisions = new ArrayList<>();
        for (Map.Entry<String, Long> entry : favourites.entrySet()) {
            GetCommonsDivisionTask commonsDivisionTask = new GetCommonsDivisionTask(new AsyncResponse() {
                @Override
                public void processFinish(Object output) {
                    CommonsDivision commonsDivision = (CommonsDivision) output;
                    if (commonsDivision != null) {
                        int found = commonsDivisions.size();
                        progressBarText.setText("Commons Divisions Found: " + ++found);
                        commonsDivisions.add((CommonsDivision) output);
                        displayData(commonsDivisions);
                    }
                }
            });
            String divisionUrl = "http://lda.data.parliament.uk/commonsdivisions/id/" + entry.getValue() + ".json";
            commonsDivisionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, divisionUrl, favourites);
        }
    }

    private void displayData(ArrayList<CommonsDivision> commonsDivisions) {
        if (favourites.size() == commonsDivisions.size()) {
            ListView listView = findViewById(R.id.favouritesListView);
            CommonsDivisionsAdapter adapter = new CommonsDivisionsAdapter(FavouriteActivity.this, firebaseAuth, commonsDivisions);
            listView.setAdapter(adapter);
            favProgressBar.setVisibility(GONE);
        }
    }
}
