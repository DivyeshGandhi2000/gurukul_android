package com.gurukul.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gurukul.Models.GalleryModel;
import com.gurukul.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private Context context;
    private List<GalleryModel> galleryList;
    private OnGalleryActionListener listener;

    // ─── Action Listener Interface ────────────────────────────────────────────────
    public interface OnGalleryActionListener {
        void onView(GalleryModel model, int position);
        void onEdit(GalleryModel model, int position);
        void onDelete(GalleryModel model, int position);
    }

    // ─── Constructor ──────────────────────────────────────────────────────────────
    public GalleryAdapter(Context context, List<GalleryModel> galleryList) {
        this.context = context;
        this.galleryList = galleryList;
    }

    public void setOnGalleryActionListener(OnGalleryActionListener listener) {
        this.listener = listener;
    }

    // ─── onCreateViewHolder ───────────────────────────────────────────────────────
    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    // ─── onBindViewHolder ─────────────────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryModel model = galleryList.get(position);

        // ── Image ──
        Glide.with(context)
                .load(model.getImageUrl())
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(holder.ivGalleryImage);

        // ── Title ──
        holder.tvGalleryTitle.setText(
                (model.getTitle() != null && !model.getTitle().isEmpty())
                        ? model.getTitle() : "No Title");

        // ── Description ──
        holder.tvGalleryDescription.setText(
                (model.getDescription() != null && !model.getDescription().isEmpty())
                        ? model.getDescription() : "");

        // ── Category Badge ──
        if (model.getCategory() != null && !model.getCategory().isEmpty()) {
            // Capitalize first letter
            String cat = model.getCategory().substring(0, 1).toUpperCase()
                    + model.getCategory().substring(1);
            holder.tvGalleryCategory.setText(cat);
            holder.tvGalleryCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvGalleryCategory.setVisibility(View.GONE);
        }

        // ── Date (format: 2026-02-22T06:13:40.422863Z → 22 Feb 2026) ──
        holder.tvGalleryDate.setText(formatDate(model.getCreatedAt()));

        // ── Button Listeners ──
        holder.btnView.setOnClickListener(v -> {
            if (listener != null) listener.onView(model, holder.getAdapterPosition());
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(model, holder.getAdapterPosition());
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(model, holder.getAdapterPosition());
        });
    }

    // ─── getItemCount ─────────────────────────────────────────────────────────────
    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    // ─── Remove item helper ───────────────────────────────────────────────────────
    public void removeItem(int position) {
        galleryList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, galleryList.size());
    }

    // ─── Date Formatter ───────────────────────────────────────────────────────────
    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        try {
            SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            Date date = inputFmt.parse(rawDate);
            SimpleDateFormat outputFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFmt.format(date);
        } catch (ParseException e) {
            // Try shorter format
            try {
                SimpleDateFormat inputFmt2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFmt2.parse(rawDate.substring(0, 10));
                SimpleDateFormat outputFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                return outputFmt.format(date);
            } catch (ParseException ex) {
                return rawDate.substring(0, Math.min(10, rawDate.length()));
            }
        }
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────────
    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivGalleryImage;
        TextView tvGalleryTitle, tvGalleryDescription, tvGalleryCategory, tvGalleryDate;
        FrameLayout btnView, btnEdit, btnDelete;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGalleryImage       = itemView.findViewById(R.id.ivGalleryImage);
            tvGalleryTitle       = itemView.findViewById(R.id.tvGalleryTitle);
            tvGalleryDescription = itemView.findViewById(R.id.tvGalleryDescription);
            tvGalleryCategory    = itemView.findViewById(R.id.tvGalleryCategory);
            tvGalleryDate        = itemView.findViewById(R.id.tvGalleryDate);
            btnView              = itemView.findViewById(R.id.btnView);
            btnEdit              = itemView.findViewById(R.id.btnEdit);
            btnDelete            = itemView.findViewById(R.id.btnDelete);
        }
    }
}