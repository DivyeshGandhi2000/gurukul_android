package com.gurukul.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.gurukul.Utils.Constants;
import com.gurukul.Utils.Utils;
import com.gurukul.adapters.GalleryAdapter;
import com.gurukul.Models.GalleryModel;
import com.gurukul.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GalleryAdapter adapter;
    private LinearLayout emptyStateLayout;
    private List<GalleryModel> galleryList;

    private static final String API_URL = Constants.BASE_URL + Constants.GALLERY_IMAGE_API;
    private static final String TAG = "GalleryAPI";

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGallery);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        // Set up 1-column Grid (matches your current code)
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        galleryList = new ArrayList<>();
        adapter = new GalleryAdapter(getContext(), galleryList);
        recyclerView.setAdapter(adapter);

        // Call the API
        fetchGalleryImages();

        return view;
    }

    private void fetchGalleryImages() {

        Utils.hideEmptyState(emptyStateLayout, recyclerView);
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "========== API REQUEST ==========");
        Log.d(TAG, "URL: " + API_URL);
        Log.d(TAG, "CURL: curl -X GET \"" + API_URL + "\"");

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                API_URL,

                response -> {

                    progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "========== API RESPONSE ==========");
                    Log.d(TAG, response);

                    try {

                        galleryList.clear();

                        // API returns a JSONObject wrapper
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
                                    v -> fetchGalleryImages());
                            return;
                        }

                        JSONArray jsonArray = rootObject.getJSONArray("data");

                        Log.d(TAG, "Total Records: " + jsonArray.length());

                        if (jsonArray.length() == 0) {
                            // Success, but list is empty -> Show No Data UI
                            Utils.showEmptyState(emptyStateLayout, recyclerView,
                                    R.drawable.no_data,
                                    getString(R.string.no_data_found), // Replace with your string if needed
                                    getString(R.string.no_data_message), // Replace with your string if needed
                                    false,
                                    null);
                            return;
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String id          = jsonObject.optString("id", "");
                            String title       = jsonObject.optString("title", "").trim();
                            String description = jsonObject.optString("description", "").trim();
                            String category    = jsonObject.optString("category", "").trim();
                            String createdAt   = jsonObject.optString("created_at", "");
                            String imageUrl    = jsonObject.optString("image", "");

                            if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                                imageUrl = Constants.IMAGE_BASE_URL + imageUrl;
                            }

                            Log.d(TAG, "ID: " + id);
                            Log.d(TAG, "Image URL: " + imageUrl);

                            galleryList.add(new GalleryModel(id, title, description, category, imageUrl, createdAt));
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {

                        Log.e(TAG, "JSON ERROR", e);
                        Log.e(TAG, "Raw Response: " + response);

                        Utils.showEmptyState(emptyStateLayout, recyclerView,
                                android.R.drawable.ic_dialog_alert,
                                getString(R.string.data_error),
                                getString(R.string.failed_to_read_data),
                                true,
                                v -> fetchGalleryImages());
                    }
                },

                error -> {

                    progressBar.setVisibility(View.GONE);

                    Log.e(TAG, "========== API ERROR ==========");

                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error Body: " + errorBody);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error reading body", ex);
                        }
                    } else {
                        Log.e(TAG, "Network Response NULL");
                        if (error.getCause() != null) {
                            Log.e(TAG, "Cause: " + error.getCause().toString());
                        }
                    }

                    Log.e(TAG, "Volley Message: " + error.toString(), error);

                    // Differentiate between Network Error and Server Error
                    if (error instanceof com.android.volley.NoConnectionError || error instanceof com.android.volley.TimeoutError) {
                        Utils.showEmptyState(emptyStateLayout, recyclerView,
                                android.R.drawable.ic_dialog_dialer, // Replace with your no_internet drawable
                                getString(R.string.no_internet_connection),
                                getString(R.string.check_network_settings),
                                true,
                                v -> fetchGalleryImages());
                    } else {
                        Utils.showEmptyState(emptyStateLayout, recyclerView,
                                android.R.drawable.ic_dialog_alert,
                                getString(R.string.server_error),
                                getString(R.string.unable_to_connect_server),
                                true,
                                v -> fetchGalleryImages());
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

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return super.parseNetworkResponse(response);
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        Log.d(TAG, "Sending request to API...");
        requestQueue.add(stringRequest);
    }
}