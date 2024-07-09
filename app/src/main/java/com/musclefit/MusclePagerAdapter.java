package com.musclefit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

// MusclePagerAdapter.java
// MusclePagerAdapter.java
public class MusclePagerAdapter extends RecyclerView.Adapter<MusclePagerAdapter.MuscleViewHolder> {

    private List<String> muscleList;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(String muscleName);
    }

    private OnItemClickListener onItemClickListener;

    public MusclePagerAdapter(Context context, List<String> muscleList, OnItemClickListener listener) {
        this.context = context;
        this.muscleList = muscleList;
        this.onItemClickListener = listener;
    }
    @NonNull
    @Override
    public MuscleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel, parent, false);
        return new MuscleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MuscleViewHolder holder, int position) {
        String muscleName = muscleList.get(position);
        holder.textViewMuscleName.setText(muscleName);

        // Assuming your images are stored in the "assets" folder with the same name as muscleName
        String imagePath = "file:///android_asset/" + muscleName + ".png";
        Glide.with(context).load(imagePath).into(holder.imageView);

        // Set an OnClickListener to handle item clicks
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(muscleName);
            }
        });
    }
    @Override
    public int getItemCount() {
        return muscleList.size();
    }

    static class MuscleViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewMuscleName;

        public MuscleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewMuscleName = itemView.findViewById(R.id.textViewMuscleName);
        }
    }
}
