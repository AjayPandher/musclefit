package com.musclefit;

import com.musclefit.Muscle;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MuscleService {
    @GET("exercises/{muscleName}")
    Call<List<Exercise>> getMuscleExercises(@Path("muscleName") String muscleName);
    @GET("muscles")
    Call<Muscle> getMuscles();
}
