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
import com.gurukul.Models.DonationModel;
import com.gurukul.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {

    private Context context;
    private List<DonationModel> donationList;
    private OnDonationActionListener listener;

    public interface OnDonationActionListener {
        void onView(DonationModel model, int position);
        void onEdit(DonationModel model, int position);
        void onDelete(DonationModel model, int position);
    }

    public DonationAdapter(Context context, List<DonationModel> donationList) {
        this.context = context;
        this.donationList = donationList;
    }

    public void setOnDonationActionListener(OnDonationActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donation, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        DonationModel model = donationList.get(position);

        Glide.with(context)
                .load(model.getImageUrl())
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(holder.ivDonationImage);

        holder.tvDonationUserName.setText(
                (model.getUserName() != null && !model.getUserName().isEmpty())
                        ? model.getUserName() : "Unknown User");

        holder.tvDonationDescription.setText(
                (model.getDescription() != null && !model.getDescription().isEmpty())
                        ? model.getDescription() : "");

        holder.tvDonationDate.setText(formatDate(model.getCreatedAt()));

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

    @Override
    public int getItemCount() {
        return donationList.size();
    }

    public void removeItem(int position) {
        donationList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, donationList.size());
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        try {
            // API Format: "2026-06-08 09:09:13"
            SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFmt.parse(rawDate);
            SimpleDateFormat outputFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFmt.format(date);
        } catch (ParseException e) {
            return rawDate.substring(0, Math.min(10, rawDate.length()));
        }
    }

    public static class DonationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivDonationImage;
        TextView tvDonationUserName, tvDonationDescription, tvDonationDate;
        FrameLayout btnView, btnEdit, btnDelete;

        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDonationImage       = itemView.findViewById(R.id.ivDonationImage);
            tvDonationUserName    = itemView.findViewById(R.id.tvDonationUserName);
            tvDonationDescription = itemView.findViewById(R.id.tvDonationDescription);
            tvDonationDate        = itemView.findViewById(R.id.tvDonationDate);
            btnView               = itemView.findViewById(R.id.btnView);
            btnEdit               = itemView.findViewById(R.id.btnEdit);
            btnDelete             = itemView.findViewById(R.id.btnDelete);
        }
    }
}