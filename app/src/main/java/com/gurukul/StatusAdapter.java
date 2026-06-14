package com.gurukul;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.gurukul.Utils.ThemeApplier;
import com.gurukul.Utils.VideoGenerator;
import com.gurukul.Utils.VideoGenerator1;
import com.gurukul.Utils.VideoGenerator2;
import com.gurukul.Utils.VideoGeneratorNew;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.FileInputStream;
import java.io.OutputStream;
public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {
    private Context context;
    private List<Status> statusList;
    private DatabaseHelper dbHelper;
    private Uri lastSavedVideoUri;

    public StatusAdapter(Context context, List<Status> statusList, DatabaseHelper dbHelper) {
        this.context = context;
        this.statusList = statusList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Status status = statusList.get(position);

        holder.tvName.setText(status.getName());
        holder.tvDescription.setText(status.getDescription());

        String hindiDate = DateConverterHindi.convertToHindi(status.getDate());
        holder.tvDate.setText(hindiDate);
        holder.tvType.setText(status.getType());

        // Apply font to list card TextViews
        applyFont(holder.tvName);
        applyFont(holder.tvDescription);
        applyFont(holder.tvDate);
        applyFont(holder.tvType);

        File imgFile = new File(status.getImagePath());
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.ivImage.setImageBitmap(bitmap);
        }

        holder.btnShare.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String themeKey = prefs.getString("template", "classic");

            com.gurukul.model.ThemeConfig theme = com.gurukul.db.ThemeDatabaseHelper.getInstance(context).getTheme(themeKey);
            int layoutRes = ThemeApplier.getLayoutResId(context, themeKey);
            View xmlView = LayoutInflater.from(context).inflate(layoutRes, null);

            populateXmlView(xmlView, status, theme);

            Bitmap bitmap = convertViewToBitmap(xmlView);
            if (bitmap != null) shareImageBitmap(bitmap, status);
            else Toast.makeText(context, "Failed to create image", Toast.LENGTH_SHORT).show();
        });


        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddStatusActivity.class);
            intent.putExtra("STATUS_ID", status.getId());
            v.getContext().startActivity(intent);
        });
//        holder.btndownload.setOnClickListener(v -> {
//            ProgressDialog progressDialog = new ProgressDialog(context);
//            progressDialog.setTitle("Creating Video");
//            progressDialog.setMessage("Rendering frame 0%");
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            progressDialog.setMax(100);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//
//            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//            String videoColor = prefs.getString("video_color", "blue");
//
//            if ("brown".equals(videoColor)) {
//                VideoGeneratorNew.createAnimatedVideo(context, status, new VideoGeneratorNew.VideoGenerationCallback() {
//                    @Override
//                    public void onProgress(int percentage) {
//                        ((Activity) context).runOnUiThread(() -> {
//                            progressDialog.setProgress(percentage);
//                            progressDialog.setMessage("Rendering frame " + percentage + "%");
//                        });
//                    }
//
//                    @Override
//                    public void onFinished(File videoFile) {
//                        ((Activity) context).runOnUiThread(() -> {
//                            progressDialog.dismiss();
//                            // FIX: Save to MediaStore first to generate thumbnail
//                            Uri savedUri = saveVideoToDownloadsAndGetUri(context, videoFile);
//                            if (savedUri!= null) {
//                                shareVideoUri(context, savedUri);
//                            } else {
//                                shareVideo(context, videoFile); // fallback
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        ((Activity) context).runOnUiThread(() -> {
//                            progressDialog.dismiss();
//                            android.util.Log.e("VideoGen", "Failed", e);
//                            Toast.makeText(context, "Error: " + e.getClass().getSimpleName() + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        });
//                    }
//                });
//
//            } else {
//                VideoGenerator.createAnimatedVideo(context, status, new VideoGenerator.VideoGenerationCallback() {
//                    @Override
//                    public void onProgress(int percentage) {
//                        ((Activity) context).runOnUiThread(() -> {
//                            progressDialog.setProgress(percentage);
//                            progressDialog.setMessage("Rendering frame " + percentage + "%");
//                        });
//                    }
//
//                    @Override
//                    public void onFinished(File videoFile) {
//                        ((Activity) context).runOnUiThread(() -> {
//                            progressDialog.dismiss();
//                            // FIX: Save to MediaStore first to generate thumbnail
//                            Uri savedUri = saveVideoToDownloadsAndGetUri(context, videoFile);
//                            if (savedUri!= null) {
//                                shareVideoUri(context, savedUri);
//                            } else {
//                                shareVideo(context, videoFile); // fallback
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        ((Activity) context).runOnUiThread(() -> {
//                            progressDialog.dismiss();
//                            android.util.Log.e("VideoGen", "Failed", e);
//                            Toast.makeText(context, "Error: " + e.getClass().getSimpleName() + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        });
//                    }
//                });
//            }
//        });
        holder.btndownload.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Creating Video");
            progressDialog.setMessage("Rendering frame 0%");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();

            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String videoColor = prefs.getString("video_color", "blue");

            switch (videoColor) {
                case "brown":
                    VideoGenerator2.createAnimatedVideo(context, status, new VideoGenerator2.VideoGenerationCallback() {
                        @Override
                        public void onProgress(int percentage) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.setProgress(percentage);
                                progressDialog.setMessage("Rendering frame " + percentage + "%");
                            });
                        }

                        @Override
                        public void onFinished(File videoFile) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                lastSavedVideoUri = saveVideoToDownloadsAndGetUri(context, videoFile);
                                if (lastSavedVideoUri != null) {
                                    Toast.makeText(context, "Video saved to gallery successfully!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Failed to save video.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Log.e("VideoGen", "Failed", e);
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                    break;

                case "red":
                    VideoGenerator1.createAnimatedVideo(context, status, new VideoGenerator1.VideoGenerationCallback() {
                        @Override
                        public void onProgress(int percentage) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.setProgress(percentage);
                                progressDialog.setMessage("Rendering frame " + percentage + "%");
                            });
                        }

                        @Override
                        public void onFinished(File videoFile) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                lastSavedVideoUri = saveVideoToDownloadsAndGetUri(context, videoFile);
                                if (lastSavedVideoUri != null) {
                                    Toast.makeText(context, "Video saved to gallery successfully!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Failed to save video.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Log.e("VideoGen", "Failed", e);
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                    break;

                case "green":
                    VideoGeneratorNew.createAnimatedVideo(context, status, new VideoGeneratorNew.VideoGenerationCallback() {
                        @Override
                        public void onProgress(int percentage) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.setProgress(percentage);
                                progressDialog.setMessage("Rendering frame " + percentage + "%");
                            });
                        }

                        @Override
                        public void onFinished(File videoFile) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                lastSavedVideoUri = saveVideoToDownloadsAndGetUri(context, videoFile);
                                if (lastSavedVideoUri != null) {
                                    Toast.makeText(context, "Video saved to gallery successfully!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Failed to save video.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Log.e("VideoGen", "Failed", e);
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                    break;

                case "blue":
                default:
                    VideoGenerator.createAnimatedVideo(context, status, new VideoGenerator.VideoGenerationCallback() {
                        @Override
                        public void onProgress(int percentage) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.setProgress(percentage);
                                progressDialog.setMessage("Rendering frame " + percentage + "%");
                            });
                        }

                        @Override
                        public void onFinished(File videoFile) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                lastSavedVideoUri = saveVideoToDownloadsAndGetUri(context, videoFile);
                                if (lastSavedVideoUri != null) {
                                    Toast.makeText(context, "Video saved to gallery successfully!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Failed to save video.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            ((Activity) context).runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Log.e("VideoGen", "Failed", e);
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                    break;
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            dbHelper.deleteStatus(status.getId());
            statusList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, statusList.size());
            Toast.makeText(context, "Status deleted", Toast.LENGTH_SHORT).show();
        });
    }

    private Uri saveVideoToDownloadsAndGetUri(Context context, File videoFile) {
        try {
            if (videoFile == null ||!videoFile.exists()) {
                Toast.makeText(context, "Video file not found", Toast.LENGTH_LONG).show();
                return null;
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, "Shantidhara_" + System.currentTimeMillis() + ".mp4");
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Shantidhara");
            values.put(MediaStore.Video.Media.IS_PENDING, 1);

            Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri uri = context.getContentResolver().insert(collection, values);
            if (uri == null) return null;

            try (OutputStream out = context.getContentResolver().openOutputStream(uri);
                 FileInputStream in = new FileInputStream(videoFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer))!= -1) {
                    out.write(buffer, 0, read);
                }
            }

            values.clear();
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            context.getContentResolver().update(uri, values, null, null);
            return uri;

        } catch (Exception e) {
            Log.e("VIDEO_SAVE_ERROR", "saveVideoToDownloads: " + e.getMessage(), e);
            return null;
        }
    }

    // FIX: New method to share MediaStore Uri
    private void shareVideoUri(Context context, Uri videoUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share Status Video via..."));
    }

    private void shareVideo(Context context, File videoFile) {
        if (videoFile == null ||!videoFile.exists()) {
            Toast.makeText(context, "Video file not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri videoUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                videoFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share Status Video via..."));
    }


    private void saveVideoToDownloads(Context context, File videoFile) {

        try {

            Log.d("VIDEO_SAVE", "Saving started");

            if (videoFile == null || !videoFile.exists()) {

                Log.e("VIDEO_SAVE_ERROR", "Video file does not exist");

                Toast.makeText(context,
                        "Video file not found",
                        Toast.LENGTH_LONG).show();

                return;
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME,
                    "Shantidhara_" + System.currentTimeMillis() + ".mp4");

            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

            values.put(MediaStore.Video.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + "/Shantidhara");

            values.put(MediaStore.Video.Media.IS_PENDING, 1);

            Uri uri = context.getContentResolver().insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values);

            Log.d("VIDEO_SAVE", "Uri : " + uri);

            if (uri == null) {

                Log.e("VIDEO_SAVE_ERROR", "Uri is null");

                Toast.makeText(context,
                        "Failed to create media file",
                        Toast.LENGTH_LONG).show();

                return;
            }

            OutputStream out =
                    context.getContentResolver().openOutputStream(uri);

            if (out == null) {

                Log.e("VIDEO_SAVE_ERROR", "OutputStream is null");

                Toast.makeText(context,
                        "Failed to open output stream",
                        Toast.LENGTH_LONG).show();

                return;
            }

            FileInputStream in = new FileInputStream(videoFile);

            byte[] buffer = new byte[4096];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

            values.clear();
            values.put(MediaStore.Video.Media.IS_PENDING, 0);

            context.getContentResolver().update(uri, values, null, null);

            Log.d("VIDEO_SAVE", "Video saved successfully");

            Toast.makeText(context,
                    "Video saved to Downloads/Shantidhara",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            Log.e("VIDEO_SAVE_ERROR",
                    "saveVideoToDownloads: " + e.getMessage(),
                    e);

            Toast.makeText(context,
                    "Save failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
//    private void shareVideo(Context context, File videoFile) {
//        if (videoFile == null || !videoFile.exists()) {
//            Toast.makeText(context, "Video file not found!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 1. Get the secure URI using FileProvider
//        Uri videoUri = FileProvider.getUriForFile(
//                context,
//                context.getPackageName() + ".fileprovider", // This must match your Manifest
//                videoFile
//        );
//
//        // 2. Create the Share Intent
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("video/mp4");
//        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
//
//        // 3. Grant temporary read permission to whichever app receives it (WhatsApp, etc.)
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        // 4. Open the Share Sheet
//        context.startActivity(Intent.createChooser(shareIntent, "Share Status Video via..."));
//    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    private void populateXmlView(View view, Status status, com.gurukul.model.ThemeConfig theme) {
        // 1. Apply theme colors/drawables (single call — no if/else chain)
       ThemeApplier.applyToStatusView(context, view, theme);

        TextView tvName        = view.findViewById(R.id.name);
        TextView tvDescription = view.findViewById(R.id.discription);
        TextView tvDate        = view.findViewById(R.id.date);
        TextView tvType        = view.findViewById(R.id.type);
        ImageView ivImage      = view.findViewById(R.id.image);
        LinearLayout isVisibleName = view.findViewById(R.id.isVisibleName);

        // 2. Populate data
        if (tvName != null)        tvName.setText(status.getName());
        if (tvDescription != null) tvDescription.setText(status.getDescription());
        if (tvDate != null)        tvDate.setText(DateConverterHindi.convertToHindi(status.getDate()));

        // 3. Font
        applyFontToViewGroup((ViewGroup) view);

        // 4. Type visibility
        if (tvType != null) {
            String type = status.getType();
            if (type != null && type.equals("अन्य")) {
                tvType.setVisibility(View.GONE);
            } else {
                tvType.setText(type);
                tvType.setVisibility(View.VISIBLE);
            }
        }

        // 5. Occasion label / icon
        TextView occasionLabel = view.findViewById(R.id.occasionLabel);
        ImageView occasionIcon = view.findViewById(R.id.occasionIcon);
        if (occasionLabel != null) occasionLabel.setText(getOccasionLabel(status.getType()));
        if (occasionIcon != null)  occasionIcon.setImageResource(getOccasionIcon(status.getType()));

        // 6. isVisibleName
        if (isVisibleName != null)
            isVisibleName.setVisibility(status.isVisible() ? View.VISIBLE : View.GONE);

        // 7. Image
        if (ivImage != null) {
            File imgFile = new File(status.getImagePath());
            if (imgFile.exists()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opts);
                opts.inSampleSize = calculateInSampleSize(opts, 800, 600);
                opts.inJustDecodeBounds = false;
                ivImage.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opts));
            } else {
                Log.e("ImageError", "Image not found at: " + status.getImagePath());
            }
        }
    }

    private void applyFontToViewGroup(ViewGroup vg) {
        if (vg == null) return;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextView) {
                applyFont((TextView) child);
            } else if (child instanceof ViewGroup) {
                applyFontToViewGroup((ViewGroup) child);
            }
        }
    }

    private void applyFont(TextView tv) {
        if (tv == null) return;
        android.content.SharedPreferences prefs =
                context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String font = prefs.getString("font_family", "default");

        android.graphics.Typeface typeface;
        switch (font) {
            case "serif":
                typeface = android.graphics.Typeface.SERIF;
                break;
            case "monospace":
                typeface = android.graphics.Typeface.MONOSPACE;
                break;
            case "sans-serif":
                typeface = android.graphics.Typeface.SANS_SERIF;
                break;
            default:
                typeface = android.graphics.Typeface.DEFAULT;
                break;
        }

        int existingStyle = tv.getTypeface() != null
                ? tv.getTypeface().getStyle()
                : android.graphics.Typeface.NORMAL;
        tv.setTypeface(typeface, existingStyle);
    }

    public static String getOccasionLabel(String type) {
        if (type == null) return "✨ विशेष अवसर";
        if (type.contains("जन्मदिन"))    return "🎂 Happy Birthday";
        if (type.contains("विवाह"))      return "💍 Happy Anniversary";
        if (type.contains("पुण्यतिथि")) return "🪔 श्रद्धांजलि";
        if (type.contains("शान्तिधारा")) return "🙏 शान्तिधारा";
        return "✨ विशेष अवसर";
    }

    public static int getOccasionIcon(String type) {
        if (type == null) return R.drawable.ic_star;
        if (type.contains("जन्मदिन"))    return R.drawable.ic_cake;
        if (type.contains("विवाह"))      return R.drawable.ic_favorite;
        if (type.contains("पुण्यतिथि")) return R.drawable.ic_diya;
        if (type.contains("शान्तिधारा")) return R.drawable.ic_prayer;
        return R.drawable.ic_star;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap convertViewToBitmap(View view) {
        try {
            android.util.DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;

            // Step 1: Find ScrollView and its child LinearLayout
            ScrollView scrollView = view.findViewById(R.id.rootScroll);

            // Step 2: Measure ScrollView child first to get FULL content height
            int fullHeight = 0;
            if (scrollView != null && scrollView.getChildCount() > 0) {
                View scrollChild = scrollView.getChildAt(0);

                // Measure child with unlimited height
                int childWidthSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY);
                int childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                scrollChild.measure(childWidthSpec, childHeightSpec);
                fullHeight = scrollChild.getMeasuredHeight();
            }

            // Step 3: Measure root FrameLayout with full height
            int widthSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(fullHeight, View.MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);

            // Step 4: Layout everything with full height
            view.layout(0, 0, screenWidth, fullHeight);

            // Step 5: Force ScrollView and its child to full height
            if (scrollView != null && scrollView.getChildCount() > 0) {
                scrollView.layout(0, 0, screenWidth, fullHeight);
                scrollView.getChildAt(0).layout(0, 0, screenWidth, fullHeight);
            }

            // Step 6: Draw to bitmap
            Bitmap bitmap = Bitmap.createBitmap(screenWidth, fullHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(android.graphics.Color.WHITE);
            view.draw(canvas);

            Log.d("BitmapCreation", "Bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            return bitmap;

        } catch (Exception e) {
            Log.e("BitmapError", "Error converting view to bitmap", e);
            return null;
        }
    }
    private void shareImageBitmap(Bitmap bitmap, Status status) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();

            File file = new File(cachePath, "status_" + System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri imageUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );

            Log.d("ShareDebug", "Image URI: " + imageUri);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//            shareIntent.putExtra(Intent.EXTRA_TEXT,
//                    status.getName() + "\n" + status.getDescription());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share Status"));

        } catch (Exception e) {
            Log.e("ShareError", "Error while sharing bitmap", e);
            Toast.makeText(context, "Share failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void copyStatus(Status status) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Status", status.getStatusText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Status copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDescription, tvDate, tvType;
        FrameLayout btnShare, btnDelete, btnEdit,btndownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivStatusImage);
            tvName = itemView.findViewById(R.id.tvStatusName);
            tvDescription = itemView.findViewById(R.id.tvStatusDescription);
            tvDate = itemView.findViewById(R.id.tvStatusDate);
            tvType = itemView.findViewById(R.id.tvStatusType);
            btnShare = itemView.findViewById(R.id.btnShare);
            btndownload = itemView.findViewById(R.id.btndownload);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
    // Add this inside your StatusAdapter class
    public void updateList(List<Status> filteredList) {
        // Overwrite the current list with the filtered results
        this.statusList = filteredList; // assuming your internal list is named statusList

        // Notify the RecyclerView to refresh
        notifyDataSetChanged();
    }
}