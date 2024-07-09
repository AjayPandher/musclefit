package com.musclefit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private MusclePagerAdapter musclePagerAdapter;
    private List<String> storeList = new ArrayList<>();

    private Handler sliderHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        fetchMuscleData();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
    }

    private void fetchMuscleData() {
        ApiClient.getMuscleList(new Callback<Muscle>() {
            @Override
            public void onResponse(Call<Muscle> call, Response<Muscle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    storeList.addAll(response.body().getData());
                    if (isAdded()) {
                        initAdapter();
                    }
                }
            }

            @Override
            public void onFailure(Call<Muscle> call, Throwable t) {
                // Handle failure, like showing an error message
            }
        });
    }

    private void initAdapter() {
        musclePagerAdapter = new MusclePagerAdapter(requireContext(), storeList, new MusclePagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String muscleName) {
                // Open ExerciseListActivity and pass the muscle name
                Intent intent = new Intent(requireContext(), ExerciseListActivity.class);
                intent.putExtra("muscleName", muscleName);
                startActivity(intent);
            }
        });
        viewPager.setAdapter(musclePagerAdapter);  // Set the adapter first
        viewPager.setOffscreenPageLimit(3);
        viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(20));

        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.80f + r * 0.20f);
        });
        viewPager.setPageTransformer(compositePageTransformer);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 20000); // slide duration 2 seconds

                // Implement infinite scrolling
                if (position == 0) {
                    viewPager.setCurrentItem(storeList.size() - 2, false);
                } else if (position == storeList.size() - 1) {
                    viewPager.setCurrentItem(1, false);
                }
            }
        });

        DotsIndicator dotsIndicator = getView().findViewById(R.id.dotsIndicator);

        dotsIndicator.setViewPager2(viewPager);
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    };
}
