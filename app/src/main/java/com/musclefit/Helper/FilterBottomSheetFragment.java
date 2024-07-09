// FilterBottomSheetFragment.java
package com.musclefit.Helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.musclefit.R;
public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private FilterListener filterListener;
    private String selectedFilter = "all"; // Default filter

    public interface FilterListener {
        void onFilterSelected(String filter);
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);

        RadioGroup radioGroupFilter = view.findViewById(R.id.radioGroupFilter);

        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonAll) {
                selectedFilter = "all";
            } else if (checkedId == R.id.radioButtonBeginner) {
                selectedFilter = "beginner";
            } else if (checkedId == R.id.radioButtonIntermediate) {
                selectedFilter = "intermediate";
            } else if (checkedId == R.id.radioButtonExpert) {
                selectedFilter = "expert";
            }

            // Notify the listener when the filter changes
            if (filterListener != null) {
                filterListener.onFilterSelected(selectedFilter);
            }
        });

        view.findViewById(R.id.buttonApplyFilter).setOnClickListener(v -> {
            // Apply the filter and dismiss the bottom sheet
            if (filterListener != null) {
                filterListener.onFilterSelected(selectedFilter);
            }
            dismiss();
        });

        // Set default filter to "All"
        RadioButton radioButtonAll = view.findViewById(R.id.radioButtonAll);
        radioButtonAll.setChecked(true);

        return view;
    }
}
