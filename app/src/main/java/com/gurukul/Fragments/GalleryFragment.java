package com.gurukul.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<GalleryModel> galleryList;

    private static final String API_URL = Constants.BASE_URL +Constants.GALLERY_IMAGE_API;
    private static final String TAG = "GalleryAPI";

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGallery);
        progressBar = view.findViewById(R.id.progressBar);

        // Set up 2-column Grid
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        galleryList = new ArrayList<>();
        adapter = new GalleryAdapter(getContext(), galleryList);
        recyclerView.setAdapter(adapter);

        // Call the API
        fetchGalleryImages();

        return view;
    }

    private void fetchGalleryImages() {

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

                        // ✅ FIX: API returns a JSONObject wrapper, not a plain JSONArray
                        JSONObject rootObject = new JSONObject(response);

                        boolean status = rootObject.optBoolean("status", false);

                        if (!status) {
                            String message = rootObject.optString("message", "Unknown error");
                            Log.e(TAG, "API returned status false: " + message);
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONArray jsonArray = rootObject.getJSONArray("data");

                        Log.d(TAG, "Total Records: " + jsonArray.length());

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            try {
                                String readable = new String(jsonObject.toString().getBytes("ISO-8859-1"), "UTF-8");
                                Log.d(TAG, "Item " + i + ": " + readable);
                            } catch (Exception e) {
                                Log.d(TAG, "Item " + i + ": " + jsonObject.toString());
                            }
                            String id          = jsonObject.optString("id", "");
                            String title       = jsonObject.optString("title", "").trim();
                            String description = jsonObject.optString("description", "").trim();  // ← ADD
                            String category    = jsonObject.optString("category", "").trim();     // ← ADD
                            String createdAt   = jsonObject.optString("created_at", "");          // ← ADD
                            String imageUrl    = jsonObject.optString("image", "");

                            if (!imageUrl.startsWith("http")) {
                                imageUrl = Constants.IMAGE_BASE_URL + imageUrl;
                            }

                            Log.d(TAG, "ID: " + id);
                            Log.d(TAG, "Image URL: " + imageUrl);

                            // ← UPDATED: pass all 6 fields
                            galleryList.add(new GalleryModel(id, title, description, category, imageUrl, createdAt));
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {

                        Log.e(TAG, "JSON ERROR", e);
                        Log.e(TAG, "Raw Response: " + response);

                        Toast.makeText(
                                getContext(),
                                "JSON Parsing Error",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },

                error -> {

                    progressBar.setVisibility(View.GONE);

                    Log.e(TAG, "========== API ERROR ==========");

                    if (error.networkResponse != null) {

                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);

                        try {
                            String errorBody = new String(
                                    error.networkResponse.data,
                                    "UTF-8"
                            );
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

                    Toast.makeText(
                            getContext(),
                            "Failed to load images",
                            Toast.LENGTH_LONG
                    ).show();
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