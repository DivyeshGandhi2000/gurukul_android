package com.gurukul.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gurukul.AddStatusActivity;
import com.gurukul.DatabaseHelper;
import com.gurukul.R;
import com.gurukul.Status;
import com.gurukul.StatusAdapter;

import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private StatusAdapter adapter;
    private DatabaseHelper dbHelper;
    private Button btnAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        btnAdd = view.findViewById(R.id.btnAdd);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadStatuses();

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddStatusActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatuses(); // Refresh data when returning to home
    }

    private void loadStatuses() {
        List<Status> statusList = dbHelper.getAllStatus();
        adapter = new StatusAdapter(getContext(), statusList, dbHelper);
        recyclerView.setAdapter(adapter);
    }
}