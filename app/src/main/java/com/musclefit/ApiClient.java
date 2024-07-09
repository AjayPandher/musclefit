package com.musclefit;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static void getMuscleExercises(String muscleName, Callback<List<Exercise>> callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://137.184.166.232:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MuscleService service = retrofit.create(MuscleService.class);

        // Log the URL before making the API call
        String apiUrl = "exercises/" + muscleName;
        Log.d("API_URL", "URL: " + apiUrl);

        Call<List<Exercise>> call = service.getMuscleExercises(muscleName);
        Log.d("API_REQUEST", "Making API call...");

        call.enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                callback.onFailure(call, t);
                Log.e("API_FAILURE", "Request failed: " + t.getMessage());
            }
        });
    }

    public static void getMuscleList(Callback<Muscle> callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://137.184.166.232:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MuscleService service = retrofit.create(MuscleService.class);

        Call<Muscle> call = service.getMuscles();
        Log.d("API_REQUEST", "Making API call...");

        call.enqueue(new Callback<Muscle>() {
            @Override
            public void onResponse(Call<Muscle> call, Response<Muscle> response) {
                Log.d("API_RESPONSE", "Received API response.");
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                    Log.d("API_RESPONSE", "Response: " + response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<Muscle> call, Throwable t) {
                callback.onFailure(call, t);
                Log.e("API_FAILURE", "Request failed: " + t.getMessage());
            }
        });
    }
}

