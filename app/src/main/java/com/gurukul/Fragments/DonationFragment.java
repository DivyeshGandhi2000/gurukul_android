package com.gurukul.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gurukul.Models.DonationModel;
import com.gurukul.R;
import com.gurukul.Utils.Constants;
import com.gurukul.Utils.Utils;
import com.gurukul.adapters.DonationAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DonationFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DonationAdapter adapter;
    LinearLayout emptyStateLayout;
    private List<DonationModel> donationList;

    // TODO: Add DONATION_API to your Constants class
    private static final String API_URL = Constants.BASE_URL + Constants.DONATION_API;
    private static final String TAG = "DonationAPI";

    public DonationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donation, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDonations);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        donationList = new ArrayList<>();
        adapter = new DonationAdapter(getContext(), donationList);
        recyclerView.setAdapter(adapter);

        fetchDonations();

        return view;
    }

    private void fetchDonations() {

        Utils.hideEmptyState(emptyStateLayout, recyclerView);
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "========== API REQUEST ==========");
        Log.d(TAG, "URL: " + API_URL);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                API_URL,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "========== API RESPONSE ==========");
                    Log.d(TAG, response);

                    try {
                        donationList.clear();

                        JSONObject rootObject = new JSONObject(response);
                        boolean status = rootObject.optBoolean("status", false);

                        if (!status) {
                            String message = rootObject.optString("message", getString(R.string.something_went_wrong));
                            Log.e(TAG, "API returned status false: " + message);
                            Utils.showEmptyState(emptyStateLayout, recyclerView,
                                    android.R.drawable.ic_dialog_alert,
                                    getString(R.string.something_went_wrong),
                                    message,
                                    true,
                                    v -> fetchDonations());
                            return;
                        }

                        // Use the correct key: "donations" instead of "data"
                        JSONArray jsonArray = rootObject.getJSONArray("donations");
                        Log.d(TAG, "Total Records: " + jsonArray.length());

                        if (jsonArray.length() == 0) {
                            // Success, but list is empty -> Show No Data UI
                            Utils.showEmptyState(emptyStateLayout, recyclerView,
                                    R.drawable.no_data,
                                    getString(R.string.no_donations_found),
                                    getString(R.string.no_donations_message),
                                    false,
                                    null);
                            return;
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String id          = jsonObject.optString("id", "");
                            String userName    = jsonObject.optString("user_name", "").trim();
                            String description = jsonObject.optString("description", "").trim();
                            String createdAt   = jsonObject.optString("created_at", "");
                            String imageUrl    = jsonObject.optString("image", "");

                            if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                                imageUrl = Constants.IMAGE_BASE_URL + imageUrl;
                            }

                            donationList.add(new DonationModel(id, userName, description, imageUrl, createdAt));
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON ERROR", e);
                        Utils.showEmptyState(emptyStateLayout, recyclerView,
                                android.R.drawable.ic_dialog_alert,
                                getString(R.string.data_error),
                                getString(R.string.failed_to_read_data),
                                true,
                                v -> fetchDonations());
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "========== API ERROR ==========");

                    // Differentiate between Network Error and Server Error
                    if (error instanceof com.android.volley.NoConnectionError || error instanceof com.android.volley.TimeoutError) {
                        Utils.showEmptyState(emptyStateLayout, recyclerView,
                                android.R.drawable.ic_dialog_dialer, // Replace with your no_internet drawable if you have one
                                getString(R.string.no_internet_connection),
                                getString(R.string.check_network_settings),
                                true,
                                v -> fetchDonations());
                    } else {
                        Utils.showEmptyState(emptyStateLayout, recyclerView,
                                android.R.drawable.ic_dialog_alert,
                                getString(R.string.server_error),
                                getString(R.string.unable_to_connect_server),
                                true,
                                v -> fetchDonations());
                    }
                }
        ) {
            @Override
            protected String getParamsEncoding() {
                return "UTF-8";
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=UTF-8";
            }

            // Preserving your UTF-8 encoding fix to correctly display Hindi strings
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return super.parseNetworkResponse(response);
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }
}