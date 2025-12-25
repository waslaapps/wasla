package com.waslaapps.wasla.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waslaapps.wasla.R;
import com.waslaapps.wasla.models.Level;

import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

   public interface OnLevelClickListener {
      void onLevelClick(int position);
   }

   private final List<Level> levels;
   private final OnLevelClickListener listener;

   public LevelAdapter(List<Level> levels, OnLevelClickListener listener) {
      this.levels = levels;
      this.listener = listener;
   }

   @NonNull
   @Override
   public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_level, parent, false);
      return new LevelViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
      Level level = levels.get(position);

      int rows = level.getGrid().size();
      int cols = level.getGrid().get(0).size();
      int cluesCount = level.clues.size();

      holder.levelTitle.setText("Level " + level.getId());
      holder.levelInfo.setText(rows + "×" + cols + " • " + cluesCount + " clues");

      holder.itemView.setOnClickListener(v -> listener.onLevelClick(position));
   }

   @Override
   public int getItemCount() {
      return levels.size();
   }

   static class LevelViewHolder extends RecyclerView.ViewHolder {
      TextView levelTitle, levelInfo;

      LevelViewHolder(@NonNull View itemView) {
         super(itemView);
         levelTitle = itemView.findViewById(R.id.levelTitle);
         levelInfo = itemView.findViewById(R.id.levelInfo);
      }
   }
}
