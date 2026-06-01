package com.gurukul.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private List<Status> statusList = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        btnAdd = view.findViewById(R.id.btnAdd);
        searchInput = view.findViewById(R.id.inlineSearchInput);
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

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        return view;
    }
    private void filter(String text) {
        List<Status> filteredList = new ArrayList<>();

        for (Status item : statusList) {
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            String date = item.getDate() != null ? item.getDate().toLowerCase() : "";

            if (name.contains(text.toLowerCase()) || date.contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        loadStatuses();

        if (searchInput != null && !searchInput.getText().toString().isEmpty()) {
            filter(searchInput.getText().toString());
        }
    }

    private void loadStatuses() {
        statusList = dbHelper.getAllStatus();
        adapter = new StatusAdapter(getContext(), statusList, dbHelper);
        recyclerView.setAdapter(adapter);
    }
}