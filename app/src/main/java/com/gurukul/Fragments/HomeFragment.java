package com.gurukul.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gurukul.AddStatusActivity;
import com.gurukul.DatabaseHelper;
import com.gurukul.R;
import com.gurukul.Status;
import com.gurukul.StatusAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private StatusAdapter adapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton btnAdd;
    private EditText searchInput;
    private LinearLayout noDataLayout;

    private List<Status> statusList = new ArrayList<>();
    private String currentSearchText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        btnAdd = view.findViewById(R.id.btnAdd);
        searchInput = view.findViewById(R.id.inlineSearchInput);
        noDataLayout = view.findViewById(R.id.nodata_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load data initially
        loadStatuses();

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddStatusActivity.class));
        });

        // Search Listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchText = s.toString().toLowerCase().trim();
                filter(currentSearchText); // Actually call the filter method here!
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (searchInput != null && !searchInput.getText().toString().isEmpty()) {
            searchInput.setText("");
        }

        loadStatuses();
    }

    private void loadStatuses() {
        statusList = dbHelper.getAllStatus();

        if (adapter == null) {
            adapter = new StatusAdapter(getContext(), statusList, dbHelper);
        } else {
            adapter.updateList(statusList);
        }

        if (recyclerView.getAdapter() == null) {
            recyclerView.setAdapter(adapter);
        }
        filter(currentSearchText);
    }

    // The Missing Filter Logic
    private void filter(String text) {
        List<Status> filteredList = new ArrayList<>();

        for (Status item : statusList) {
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            String date = item.getDate() != null ? item.getDate().toLowerCase() : "";

            // Check if name or date contains the searched text
            if (name.contains(text) || date.contains(text)) {
                filteredList.add(item);
            }
        }

        // Update the adapter
        if (adapter != null) {
            adapter.updateList(filteredList);
        }

        // Toggle No Data Layout
        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noDataLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noDataLayout.setVisibility(View.GONE);
        }
    }
}