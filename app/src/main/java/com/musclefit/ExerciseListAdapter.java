package com.musclefit;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.musclefit.Helper.CollapsibleTextView;
import com.musclefit.Helper.FilterBottomSheetFragment;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class ExerciseListAdapter extends ArrayAdapter<Exercise> implements FilterBottomSheetFragment.FilterListener {

    private Context context;
    private List<Exercise> exercises;
    private static final String TAG = "ExerciseListAdapter";
    private List<String> favoritesGlobal;
    private List<Exercise> filteredExercises;  // Add a new list to store filtered exercises
    private String currentFilter = "all";
    @Override
    public void onFilterSelected(String filter) {
        // Apply the filter and refresh the list
        currentFilter = filter;
        filterExercises();
    }

    private void filterExercises() {
        filteredExercises.clear();
        // Apply the filter
        if (currentFilter.equals("all")) {
            // Show all exercises if the filter is "all"
                // Add the first exercise to the filtered list
                filteredExercises.addAll(exercises);

        } else {
            // Filter exercises based on the selected level
            for (Exercise exercise : exercises) {
                if (exercise.getLevel().equalsIgnoreCase(currentFilter)) {
                    // Add the first exercise that matches the filter
                    filteredExercises.add(exercise);
                }
            }
        }

        logFilteredExercises();
        notifyDataSetChanged();
    }
    private void logFilteredExercises() {
        System.out.println("-----------"+filteredExercises.size());
        for (Exercise exercise : filteredExercises) {
            Log.d(TAG, "Name: " + exercise.getName() + ", Level: " + exercise.getLevel());
        }
    }
    public ExerciseListAdapter(Context context, List<Exercise> exercises) {
        super(context, 0, exercises);
        this.context = context;
        this.exercises = exercises;
        this.filteredExercises = new ArrayList<>();  // Initialize the list
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            fetchUserFavorites(userId);
            filterExercises();

        }
    }

    private void fetchUserFavorites(String userId) {
        // Assuming you have a Firestore reference to the user's document
        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId);
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    List<String> favorite = (List<String>) document.get("favorites");
                    if (favorite != null && favorite.size() > 0) {
                        this.favoritesGlobal = favorite;

                        // Notify the adapter that data has changed
                        notifyDataSetChanged();
                    }
                } else {
                    Log.e(TAG, "User document does not exist");
                }
            } else {
                Log.e(TAG, "Error getting user document from Firestore", task.getException());
            }
        });
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_exercise, parent, false);
        }
        if(filteredExercises.isEmpty()){
            filteredExercises.addAll(exercises);
        }

        if (!filteredExercises.isEmpty()) {

            Exercise currentExercise = filteredExercises.get(position);

            if (currentExercise != null) {
                ViewPager viewPager = listItemView.findViewById(R.id.viewPagerExercise);
                ImageViewPagerAdapter pagerAdapter = new ImageViewPagerAdapter(context, currentExercise.getImages());
                viewPager.setAdapter(pagerAdapter);

                DotsIndicator dotsIndicator = listItemView.findViewById(R.id.dotsIndicator);
                dotsIndicator.setViewPager(viewPager);

                TextView nameTextView = listItemView.findViewById(R.id.textViewExerciseName);
                nameTextView.setText(currentExercise.getName());

                TextView forceTextView = listItemView.findViewById(R.id.textViewForce);
                if (currentExercise.getForce() != null && !currentExercise.getForce().isEmpty()) {
                    forceTextView.setText(currentExercise.getForce());
                    forceTextView.setVisibility(View.VISIBLE);
                } else {
                    forceTextView.setVisibility(View.GONE);
                }

                TextView levelTextView = listItemView.findViewById(R.id.textViewLevel);
                if (currentExercise.getLevel() != null && !currentExercise.getLevel().isEmpty()) {
                    levelTextView.setText(currentExercise.getLevel());
                    levelTextView.setVisibility(View.VISIBLE);
                } else {
                    levelTextView.setVisibility(View.GONE);
                }

                TextView mechanicTextView = listItemView.findViewById(R.id.textViewMechanic);
                if (currentExercise.getMechanic() != null && !currentExercise.getMechanic().isEmpty()) {
                    mechanicTextView.setText(currentExercise.getMechanic());
                    mechanicTextView.setVisibility(View.VISIBLE);
                } else {
                    mechanicTextView.setVisibility(View.GONE);
                }

                TextView equipmentTextView = listItemView.findViewById(R.id.textViewEquipment);
                if (currentExercise.getEquipment() != null && !currentExercise.getEquipment().isEmpty()) {
                    equipmentTextView.setText(currentExercise.getEquipment());
                    equipmentTextView.setVisibility(View.VISIBLE);
                } else {
                    equipmentTextView.setVisibility(View.GONE);
                }


                CollapsibleTextView instructionsTextView = listItemView.findViewById(R.id.collapsibleTextViewInstructions);
                List<String> instructions = currentExercise.getInstructions();
                Spannable formattedInstructions = formatInstructions(instructions);
                instructionsTextView.setVisibility(instructions != null && !instructions.isEmpty() ? View.VISIBLE : View.GONE);
                instructionsTextView.setText(formattedInstructions);
                instructionsTextView.toggleExpandedState(false);

                ImageButton likeButton = listItemView.findViewById(R.id.likeButton);
                boolean isInFavorites = favoritesGlobal != null && favoritesGlobal.contains(currentExercise.getId());

                int likeIcon = isInFavorites ? R.drawable.favorite : R.drawable.favorite_border;
                likeButton.setImageResource(likeIcon);

                likeButton.setOnClickListener(view -> {
                    toggleLikeState(likeButton, currentExercise);
                });
            }
        } else {
            // Handle the case where filteredExercises is empty or position is out of bounds
            Log.e(TAG, "Invalid position or filteredExercises is empty");
            return listItemView;  // You may want to return an empty or placeholder view
        }

        return listItemView;
    }

    private void toggleLikeState(ImageButton likeButton, Exercise exercise) {
        exercise.setLiked(!exercise.isLiked());
        updateLikeButtonState(likeButton, exercise);
        updateFirestoreFavorites(exercise.getId(), exercise.isLiked());
    }

    private void updateLikeButtonState(ImageButton likeButton, Exercise exercise) {
        int likeIcon = exercise.isLiked() ? R.drawable.favorite : R.drawable.favorite_border;
        likeButton.setImageResource(likeIcon);
    }

    private void updateFirestoreFavorites(String exerciseName, boolean isLiked) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId);

            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        List<String> favorites = (List<String>) document.get("favorites");
                        if (favorites == null) {
                            favorites = new ArrayList<>();
                        }

                        if (isLiked) {
                            if (!favorites.contains(exerciseName)) {
                                favorites.add(exerciseName);
                            }
                        } else {
                            favorites.remove(exerciseName);
                        }

                        // Update the favorites array in Firestore inside the onComplete callback
                        userDocRef.update("favorites", favorites)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Favorites updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating favorites", e));
                    } else {
                        Log.e(TAG, "User document does not exist");
                    }
                } else {
                    Log.e(TAG, "Error getting user document from Firestore", task.getException());
                }
            });
        }
    }

    private Spannable formatInstructions(List<String> instructions) {
        StringBuilder instructionsText = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            instructionsText.append("\u2022 ") // Bullet character
                    .append(instructions.get(i))
                    .append("\n");
        }

        Spannable spannable = new SpannableString(instructionsText.toString());
        spannable.setSpan(new LeadingMarginSpan.Standard(0, 30), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }
}
