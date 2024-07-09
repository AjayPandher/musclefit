package com.musclefit;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musclefit.Helper.FilterBottomSheetFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseListActivity extends AppCompatActivity implements FilterBottomSheetFragment.FilterListener {

    private ListView listView;
    private ExerciseListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        // Step 1: Retrieve the muscle name from the intent
        String muscleName = getIntent().getStringExtra("muscleName");

        // Step 2: Make an API call using ApiClient
        String apiUrl = muscleName;
        makeApiCall(apiUrl);

        // Step 3: Initialize the ListView and Adapter
        listView = findViewById(R.id.listViewExercises);
        adapter = new ExerciseListAdapter(this, new ArrayList<>());
        listView.setAdapter(adapter);

        // Set up the filter button click to open the bottom sheet
        FloatingActionButton fabFilter = findViewById(R.id.fabFilter);
        fabFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment bottomSheetFragment = new FilterBottomSheetFragment();
            bottomSheetFragment.setFilterListener(this); // Set the activity as the filter listener
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        });
    }

    private void makeApiCall(String apiUrl) {
        // Use the ApiClient for making the API call
        ApiClient.getMuscleExercises(apiUrl, new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update the adapter with the parsed data
                    adapter.clear();
                    adapter.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                // Handle API call failure
                t.printStackTrace();
            }
        });
    }

    // Implement the FilterListener interface method
    @Override
    public void onFilterSelected(String filter) {
        // Apply the filter and refresh the list

        adapter.onFilterSelected(filter);
    }
}
