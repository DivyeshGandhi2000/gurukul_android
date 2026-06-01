package com.gurukul.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gurukul.DateConverterHindi;
import com.gurukul.R;
import com.gurukul.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class VideoGeneratorNew {

    public interface VideoGenerationCallback {
        void onProgress(int percentage);
        void onFinished(File videoFile);
        void onError(Exception e);
    }

    public interface ThumbnailGenerationCallback {
        void onFinished(File thumbnailFile);
        void onError(Exception e);
    }

    private static float easeOutCubic(float x) {
        return 1f - (float) Math.pow(1f - x, 3);
    }

    private static float easeOutBack(float x) {
        final float c1 = 1.70158f, c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(x - 1f, 3) + c1 * (float) Math.pow(x - 1f, 2);
    }

    private static float window(float t, float start, float end) {
        return Math.max(0f, Math.min(1f, (t - start) / (end - start)));
    }

    private static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.BRAND.startsWith("generic")
                || android.os.Build.DEVICE.startsWith("generic");
    }

    private static MediaCodec createBestEncoder(MediaFormat format) throws Exception {
        try {
            MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
            String encoderName = codecList.findEncoderForFormat(format);
            if (encoderName != null) {
                MediaCodec codec = MediaCodec.createByCodecName(encoderName);
                android.util.Log.d("VideoGenerator", "Using encoder: " + encoderName);
                return codec;
            }
        } catch (Exception e) {
            android.util.Log.w("VideoGenerator", "Hardware encoder failed, trying software...");
        }
        try {
            return MediaCodec.createByCodecName("OMX.google.h264.encoder");
        } catch (Exception e) {
            android.util.Log.w("VideoGenerator", "OMX.google fallback failed");
        }
        return MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
    }

    public static void createThumbnail(Context context, Status status,
                                       ThumbnailGenerationCallback callback) {
        new Thread(() -> {
            HandlerThread handlerThread = new HandlerThread("ThumbnailThread");
            handlerThread.start();
            Handler viewHandler = new Handler(handlerThread.getLooper());

            try {
                File cacheDir = context.getExternalCacheDir();
                if (cacheDir == null) cacheDir = context.getCacheDir();
                File outputFile = new File(cacheDir,
                        "Shantidhara_thumb_" + System.currentTimeMillis() + ".jpg");

                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int nativeWidth  = metrics.widthPixels;
                int nativeHeight = (nativeWidth * 16) / 9;

                CountDownLatch viewLatch = new CountDownLatch(1);
                AtomicReference<Bitmap>    bitmapRef = new AtomicReference<>();
                AtomicReference<Exception> errorRef  = new AtomicReference<>();

                viewHandler.post(() -> {
                    try {
                        View v = LayoutInflater.from(context)
                                .inflate(R.layout.item_video2_new_xml, null);

                        ImageView    mainImage  = v.findViewById(R.id.image);
                        ImageView    mainTemple = v.findViewById(R.id.mainTemple);
                        TextView     tvType     = v.findViewById(R.id.type);
                        TextView     tvName     = v.findViewById(R.id.name);
                        TextView     tvDesc     = v.findViewById(R.id.discription);
                        TextView     tvDate     = v.findViewById(R.id.date);
                        LinearLayout footerCard = v.findViewById(R.id.footerCard);
//                        View         vSpace     = v.findViewById(R.id.vSpace);

                        // Thumbnail represents Scene 1 components
                        if (footerCard != null) footerCard.setVisibility(View.GONE);
                        if (mainTemple != null) mainTemple.setVisibility(View.GONE);
//                        if (vSpace != null) vSpace.setVisibility(View.VISIBLE);

                        String typeText = status.getType();
                        if (typeText != null && typeText.equals("अन्य")) {
                            tvType.setVisibility(View.GONE);
                        } else {
                            tvType.setVisibility(View.VISIBLE);
                            tvType.setText(typeText);
                        }
                        tvName.setText(status.getName());
                        tvDesc.setText(status.getDescription());
                        tvDate.setText(DateConverterHindi.convertToHindi(status.getDate()));
                        applyFontToViewGroup(context, (ViewGroup) v);
                        String imgPath = status.getImagePath();
                        if (imgPath != null && !imgPath.isEmpty()) {
                            File imgFile = new File(imgPath);
                            if (imgFile.exists()) {
                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inSampleSize = 1;
                                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                Bitmap bmp = BitmapFactory.decodeFile(
                                        imgFile.getAbsolutePath(), opts);
                                if (bmp != null) mainImage.setImageBitmap(bmp);
                            } else {
                                mainImage.setVisibility(View.GONE);
                            }
                        } else {
                            mainImage.setVisibility(View.GONE);
                        }

                        v.measure(
                                View.MeasureSpec.makeMeasureSpec(nativeWidth,  View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(nativeHeight, View.MeasureSpec.EXACTLY));
                        v.layout(0, 0, nativeWidth, nativeHeight);

                        Bitmap bitmap = Bitmap.createBitmap(
                                nativeWidth, nativeHeight, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawColor(0xFFFFFFFF);
                        v.draw(canvas);
                        bitmapRef.set(bitmap);

                    } catch (Exception e) {
                        errorRef.set(e);
                    } finally {
                        viewLatch.countDown();
                    }
                });

                viewLatch.await();
                if (errorRef.get() != null) throw errorRef.get();

                Bitmap finalBitmap = bitmapRef.get();
                if (finalBitmap != null) {
                    try (FileOutputStream out = new FileOutputStream(outputFile)) {
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    }
                    if (callback != null) callback.onFinished(outputFile);
                } else {
                    throw new Exception("Failed to generate bitmap from view.");
                }

            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            } finally {
                handlerThread.quitSafely();
            }
        }).start();
    }

    public static void createAnimatedVideo(Context context, Status status,
                                           VideoGenerationCallback callback) {
        new Thread(() -> {
            MediaCodec     encoder        = null;
            MediaMuxer     muxer          = null;
            MediaExtractor audioExtractor = null;
            android.view.Surface inputSurface = null;

            HandlerThread handlerThread = new HandlerThread("VideoViewThread");
            handlerThread.start();
            Handler viewHandler = new Handler(handlerThread.getLooper());

            try {
                boolean onEmulator = isEmulator();
                android.util.Log.d("VideoGenerator", "Running on emulator: " + onEmulator);

                File cacheDir = context.getExternalCacheDir();
                if (cacheDir == null) cacheDir = context.getCacheDir();
                File outputFile = new File(cacheDir,
                        "Shantidhara_status_" + System.currentTimeMillis() + ".mp4");

                final int  encWidth        = onEmulator ? 720  : 1080;
                final int  encHeight       = onEmulator ? 1280 : 1920;
                final int  frameRate       = onEmulator ? 15   : 30;
                final int  totalFrames     = onEmulator ? 180  : 360;
                final int  bitRate         = onEmulator ? 2_000_000 : 8_000_000;
                final long frameDurationUs = 1_000_000L / frameRate;

                MediaFormat videoFormat = MediaFormat.createVideoFormat(
                        MediaFormat.MIMETYPE_VIDEO_AVC, encWidth, encHeight);
                videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,         bitRate);
                videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,       frameRate);
                videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

                encoder = createBestEncoder(videoFormat);
                encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                inputSurface = encoder.createInputSurface();
                encoder.start();

                android.util.Log.d("VideoGenerator", "Encoder started successfully");

                DisplayMetrics metrics     = context.getResources().getDisplayMetrics();
                int            nativeWidth = metrics.widthPixels;
                int            nativeHeight= (nativeWidth * 16) / 9;

                // WARMUP
                {
                    CountDownLatch warmupLatch = new CountDownLatch(1);
                    AtomicReference<View> warmupViewRef = new AtomicReference<>();

                    viewHandler.post(() -> {
                        try {
                            View v = LayoutInflater.from(context)
                                    .inflate(R.layout.item_video2_new_xml, null);

                            ImageView    mainImage  = v.findViewById(R.id.image);
                            ImageView    mainTemple = v.findViewById(R.id.mainTemple);
                            TextView     tvType     = v.findViewById(R.id.type);
                            TextView     tvName     = v.findViewById(R.id.name);
                            TextView     tvDesc     = v.findViewById(R.id.discription);
                            TextView     tvDate     = v.findViewById(R.id.date);
                            LinearLayout footerCard = v.findViewById(R.id.footerCard);
                            LinearLayout headerCard = v.findViewById(R.id.headerCard);
                            TextView     tvWebsite  = v.findViewById(R.id.tvWebsiteUrl);
//                            View         vSpace     = v.findViewById(R.id.vSpace);

                            if (mainTemple != null) mainTemple.setVisibility(View.GONE);
//                            if (vSpace != null) vSpace.setVisibility(View.VISIBLE);

                            String typeText = status.getType();
                            if (typeText != null && typeText.equals("अन्य")) {
                                tvType.setVisibility(View.GONE);
                            } else {
                                tvType.setVisibility(View.VISIBLE);
                                tvType.setText(typeText);
                            }
                            tvName.setText(status.getName());
                            tvDesc.setText(status.getDescription());
                            tvDate.setText(DateConverterHindi.convertToHindi(status.getDate()));
                            applyFontToViewGroup(context, (ViewGroup) v);
                            if (footerCard != null) { footerCard.setAlpha(0f); }
                            if (headerCard != null) { headerCard.setAlpha(1f); headerCard.setTranslationY(0f); }
                            mainImage.setAlpha(1f);
                            mainImage.setScaleX(1f);
                            mainImage.setScaleY(1f);
                            tvType.setAlpha(1f); tvType.setTranslationY(0f);
                            tvName.setAlpha(1f); tvName.setTranslationY(0f);
                            tvDesc.setAlpha(1f); tvDesc.setTranslationY(0f);
                            if (tvWebsite != null) {
                                tvWebsite.setAlpha(0f);
                            }

                            String imgPath = status.getImagePath();
                            if (imgPath != null && !imgPath.isEmpty()) {
                                File imgFile = new File(imgPath);
                                if (imgFile.exists()) {
                                    BitmapFactory.Options opts = new BitmapFactory.Options();
                                    opts.inSampleSize = 1;
                                    opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                    Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opts);
                                    if (bmp != null) mainImage.setImageBitmap(bmp);
                                } else {
                                    mainImage.setVisibility(View.GONE);
                                }
                            } else {
                                mainImage.setVisibility(View.GONE);
                            }

                            v.measure(
                                    View.MeasureSpec.makeMeasureSpec(nativeWidth,  View.MeasureSpec.EXACTLY),
                                    View.MeasureSpec.makeMeasureSpec(nativeHeight, View.MeasureSpec.EXACTLY));
                            v.layout(0, 0, nativeWidth, nativeHeight);
                            warmupViewRef.set(v);
                        } finally {
                            warmupLatch.countDown();
                        }
                    });

                    warmupLatch.await();

                    Canvas warmupCanvas = inputSurface.lockCanvas(null);
                    if (warmupCanvas != null) {
                        warmupCanvas.drawColor(0xFFFFFFFF);
                        warmupCanvas.scale(
                                (float) encWidth  / nativeWidth,
                                (float) encHeight / nativeHeight);
                        warmupViewRef.get().draw(warmupCanvas);
                        inputSurface.unlockCanvasAndPost(warmupCanvas);
                    }

                    MediaCodec.BufferInfo warmupInfo = new MediaCodec.BufferInfo();
                    boolean formatReceived = false;
                    int attempts = 0;
                    while (!formatReceived && attempts < 500) {
                        int idx = encoder.dequeueOutputBuffer(warmupInfo, 10_000);
                        if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            formatReceived = true;
                            android.util.Log.d("VideoGenerator",
                                    "Output format received after " + attempts + " attempts");
                        } else if (idx >= 0) {
                            encoder.releaseOutputBuffer(idx, false);
                        }
                        attempts++;
                    }
                    if (!formatReceived)
                        throw new Exception("Encoder warmup failed after 500 attempts.");
                }

                muxer = new MediaMuxer(
                        outputFile.getAbsolutePath(),
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                int muxVideoTrack = muxer.addTrack(encoder.getOutputFormat());
                muxer.start();

                CountDownLatch viewLatch = new CountDownLatch(1);
                AtomicReference<View>         viewRef       = new AtomicReference<>();
                AtomicReference<ImageView>    mainImageRef  = new AtomicReference<>();
                AtomicReference<ImageView>    mainTempleRef = new AtomicReference<>();
                AtomicReference<TextView>     tvTypeRef     = new AtomicReference<>();
                AtomicReference<TextView>     tvNameRef     = new AtomicReference<>();
                AtomicReference<TextView>     tvDescRef     = new AtomicReference<>();
                AtomicReference<LinearLayout> dateBarRef    = new AtomicReference<>();
                AtomicReference<LinearLayout> footerCardRef = new AtomicReference<>();
                AtomicReference<LinearLayout> headerCardRef = new AtomicReference<>();
                AtomicReference<TextView>     tvWebsiteRef  = new AtomicReference<>();
//                AtomicReference<View>         vSpaceRef     = new AtomicReference<>();

                viewHandler.post(() -> {
                    try {
                        View v = LayoutInflater.from(context)
                                .inflate(R.layout.item_video2_new_xml, null);

                        ImageView    mainImage  = v.findViewById(R.id.image);
                        ImageView    mainTemple = v.findViewById(R.id.mainTemple);
                        TextView     tvType     = v.findViewById(R.id.type);
                        TextView     tvName     = v.findViewById(R.id.name);
                        TextView     tvDesc     = v.findViewById(R.id.discription);
                        LinearLayout dateBar    = v.findViewById(R.id.dateBar);
                        TextView     tvDate     = v.findViewById(R.id.date);
                        LinearLayout footerCard = v.findViewById(R.id.footerCard);
                        LinearLayout headerCard = v.findViewById(R.id.headerCard);
                        TextView     tvWebsite  = v.findViewById(R.id.tvWebsiteUrl);
//                        View         vSpace     = v.findViewById(R.id.vSpace);

                        String typeText = status.getType();
                        if (typeText != null && typeText.equals("अन्य")) {
                            tvType.setVisibility(View.GONE);
                        } else {
                            tvType.setVisibility(View.VISIBLE);
                            tvType.setText(typeText);
                        }
                        tvName.setText(status.getName());
                        tvDesc.setText(status.getDescription());
                        tvDate.setText(DateConverterHindi.convertToHindi(status.getDate()));
                        applyFontToViewGroup(context, (ViewGroup) v);
                        String imgPath = status.getImagePath();
                        if (imgPath != null && !imgPath.isEmpty()) {
                            File imgFile = new File(imgPath);
                            if (imgFile.exists()) {
                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inSampleSize      = 1;
                                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                Bitmap bmp = BitmapFactory.decodeFile(
                                        imgFile.getAbsolutePath(), opts);
                                if (bmp != null) mainImage.setImageBitmap(bmp);
                            } else {
                                mainImage.setVisibility(View.GONE);
                            }
                        } else {
                            mainImage.setVisibility(View.GONE);
                        }

                        v.measure(
                                View.MeasureSpec.makeMeasureSpec(nativeWidth,  View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(nativeHeight, View.MeasureSpec.EXACTLY));
                        v.layout(0, 0, nativeWidth, nativeHeight);

                        viewRef.set(v);
                        mainImageRef.set(mainImage);
                        mainTempleRef.set(mainTemple);
                        tvTypeRef.set(tvType);
                        tvNameRef.set(tvName);
                        tvDescRef.set(tvDesc);
                        dateBarRef.set(dateBar);
                        footerCardRef.set(footerCard);
                        headerCardRef.set(headerCard);
                        tvWebsiteRef.set(tvWebsite);
//                        vSpaceRef.set(vSpace);
                    } finally {
                        viewLatch.countDown();
                    }
                });

                viewLatch.await();

                View         view       = viewRef.get();
                ImageView    mainImage  = mainImageRef.get();
                ImageView    mainTemple = mainTempleRef.get();
                TextView     tvType     = tvTypeRef.get();
                TextView     tvName     = tvNameRef.get();
                TextView     tvDesc     = tvDescRef.get();
                LinearLayout dateBar    = dateBarRef.get();
                LinearLayout footerCard = footerCardRef.get();
                LinearLayout headerCard = headerCardRef.get();
                TextView     tvWebsite  = tvWebsiteRef.get();
//                View         vSpace     = vSpaceRef.get();

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                long presentationTimeUs = 0L;

                for (int i = 0; i < totalFrames; i++) {
                    final float tFinal = (float) i / totalFrames;
                    final int   frame  = i;
                    final int   nW     = nativeWidth;
                    final int   nH     = nativeHeight;

                    CountDownLatch animLatch = new CountDownLatch(1);
                    viewHandler.post(() -> {
                        try {
                            if (frame == 0) {
                                // Frame 0 = Thumbnail Setup (Scene 1)
                                if (mainTemple != null) mainTemple.setVisibility(View.GONE);
//                                if (vSpace != null) vSpace.setVisibility(View.VISIBLE);
                                mainImage.setVisibility(View.VISIBLE);
                                tvType.setVisibility(View.VISIBLE);
                                tvName.setVisibility(View.VISIBLE);
                                tvDesc.setVisibility(View.VISIBLE);
                                dateBar.setVisibility(View.VISIBLE);

                                footerCard.setAlpha(0f);
                                headerCard.setAlpha(1f);
                                dateBar.setAlpha(1f);
                                mainImage.setAlpha(1f);
                                tvType.setAlpha(1f);
                                tvName.setAlpha(1f);
                                tvDesc.setAlpha(1f);
                                dateBar.setTranslationX(0f);
                                headerCard.setTranslationY(0f);
                                mainImage.setScaleX(1f);
                                mainImage.setScaleY(1f);
                                tvType.setTranslationY(0f);
                                tvName.setTranslationY(0f);
                                tvDesc.setTranslationY(0f);
                                if (tvWebsite != null) {
                                    tvWebsite.setAlpha(0f);
                                }
                            } else {
                                float tAnim = (float) (frame - 1) / (totalFrames - 1);
                                boolean isScene2 = (tAnim > 0.55f);

                                if (isScene2) {
                                    // --- SCENE 2: The Temple and Footer Cards ---
                                    if (mainImage.getVisibility() == View.VISIBLE) {
                                        mainImage.setVisibility(View.GONE);
                                        tvType.setVisibility(View.GONE);
                                        tvName.setVisibility(View.GONE);
                                        tvDesc.setVisibility(View.GONE);
                                        dateBar.setVisibility(View.GONE);

                                        // KEEP SPACE VISIBLE: By keeping it visible, it sits between
                                        // headerCard and mainTemple, pushing your temple image down
                                        // into the layout space so everything doesn't crunch at the top.
//                                        if (vSpace != null) vSpace.setVisibility(View.VISIBLE);
                                        if (mainTemple != null) mainTemple.setVisibility(View.VISIBLE);

                                        // Apply updated structural calculation metrics
                                        view.measure(View.MeasureSpec.makeMeasureSpec(nW, View.MeasureSpec.EXACTLY),
                                                View.MeasureSpec.makeMeasureSpec(nH, View.MeasureSpec.EXACTLY));
                                        view.layout(0, 0, nW, nH);
                                    }

                                    float scene2Anim = window(tAnim, 0.55f, 1.0f);
                                    float templeIn   = easeOutCubic(window(scene2Anim, 0.05f, 0.25f));
                                    float footerIn   = easeOutBack (window(scene2Anim, 0.25f, 0.60f));
                                    float websiteIn  = easeOutBack (window(scene2Anim, 0.60f, 0.90f));

                                    headerCard.setAlpha(1f);
                                    headerCard.setTranslationY(0f);

                                    if (mainTemple != null) {
                                        mainTemple.setAlpha(templeIn);
                                        mainTemple.setScaleX(0.8f + 0.2f * templeIn);
                                        mainTemple.setScaleY(0.8f + 0.2f * templeIn);
                                    }

                                    footerCard.setAlpha(footerIn);
                                    footerCard.setTranslationY(100f * (1f - footerIn));

                                    if (tvWebsite != null) {
                                        tvWebsite.setAlpha(Math.min(1f, Math.max(0f, websiteIn)));
                                        float scale = 0.5f + 0.5f * websiteIn;
                                        tvWebsite.setScaleX(scale);
                                        tvWebsite.setScaleY(scale);
                                    }

                                } else {
                                    // --- SCENE 1: Status Card Details ---
                                    if (mainImage.getVisibility() == View.GONE) {
                                        mainImage.setVisibility(View.VISIBLE);
                                        tvType.setVisibility(View.VISIBLE);
                                        tvName.setVisibility(View.VISIBLE);
                                        tvDesc.setVisibility(View.VISIBLE);
                                        dateBar.setVisibility(View.VISIBLE);

//                                        if (vSpace != null) vSpace.setVisibility(View.VISIBLE);
                                        if (mainTemple != null) mainTemple.setVisibility(View.GONE);

                                        view.measure(View.MeasureSpec.makeMeasureSpec(nW, View.MeasureSpec.EXACTLY),
                                                View.MeasureSpec.makeMeasureSpec(nH, View.MeasureSpec.EXACTLY));
                                        view.layout(0, 0, nW, nH);
                                    }

                                    float scene1Anim = window(tAnim, 0.0f, 0.55f);
                                    float dateIn    = easeOutCubic(window(scene1Anim, 0.00f, 0.15f));
                                    float headerIn  = easeOutBack (window(scene1Anim, 0.05f, 0.20f));
                                    float imgIn     = easeOutCubic(window(scene1Anim, 0.10f, 0.25f));
                                    float typeIn    = easeOutCubic(window(scene1Anim, 0.15f, 0.30f));
                                    float nameIn    = easeOutCubic(window(scene1Anim, 0.20f, 0.35f));
                                    float descIn    = easeOutCubic(window(scene1Anim, 0.25f, 0.40f));
                                    float fadeOut   = easeOutCubic(window(scene1Anim, 0.85f, 1.00f));

                                    dateBar.setTranslationX(nW * (1f - dateIn) - (nW * fadeOut));
                                    dateBar.setAlpha(Math.max(0f, dateIn - fadeOut));

                                    headerCard.setTranslationY(-200f * (1f - headerIn));
                                    headerCard.setAlpha(Math.min(1f, window(scene1Anim, 0.05f, 0.20f) * 2f));

                                    float imgScale = 0.70f + 0.30f * imgIn - 0.30f * fadeOut;
                                    mainImage.setScaleX(imgScale);
                                    mainImage.setScaleY(imgScale);
                                    mainImage.setAlpha(Math.max(0f, imgIn - fadeOut));

                                    tvType.setAlpha(Math.max(0f, typeIn - fadeOut));
                                    tvType.setTranslationY(30f * (1f - typeIn) - 30f * fadeOut);

                                    tvName.setAlpha(Math.max(0f, nameIn - fadeOut));
                                    tvName.setTranslationY(30f * (1f - nameIn) - 30f * fadeOut);

                                    tvDesc.setAlpha(Math.max(0f, descIn - fadeOut));
                                    tvDesc.setTranslationY(30f * (1f - descIn) - 30f * fadeOut);

                                    footerCard.setAlpha(0f);
                                    if (tvWebsite != null) tvWebsite.setAlpha(0f);
                                }
                            }
                            view.layout(0, 0, nW, nH);
                        } finally {
                            animLatch.countDown();
                        }
                    });

                    animLatch.await();

                    Canvas canvas = inputSurface.lockCanvas(null);
                    if (canvas != null) {
                        canvas.drawColor(0xFFFFFFFF);
                        canvas.scale(
                                (float) encWidth  / nativeWidth,
                                (float) encHeight / nativeHeight);
                        view.draw(canvas);
                        inputSurface.unlockCanvasAndPost(canvas);
                    }

                    int idx = encoder.dequeueOutputBuffer(bufferInfo, 10_000);
                    while (idx != MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (idx >= 0) {
                            boolean isConfig =
                                    (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
                            if (!isConfig && bufferInfo.size > 0) {
                                ByteBuffer buf = encoder.getOutputBuffer(idx);
                                buf.position(bufferInfo.offset);
                                buf.limit(bufferInfo.offset + bufferInfo.size);
                                bufferInfo.presentationTimeUs = presentationTimeUs;
                                muxer.writeSampleData(muxVideoTrack, buf, bufferInfo);
                            }
                            encoder.releaseOutputBuffer(idx, false);
                        }
                        idx = encoder.dequeueOutputBuffer(bufferInfo, 5_000);
                    }

                    presentationTimeUs += frameDurationUs;
                    if (callback != null)
                        callback.onProgress((int) (tFinal * 100));
                }

                encoder.signalEndOfInputStream();
                int remainingIdx = encoder.dequeueOutputBuffer(bufferInfo, 10_000);
                while (remainingIdx != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (remainingIdx >= 0) {
                        boolean isConfig =
                                (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
                        if (!isConfig && bufferInfo.size > 0) {
                            ByteBuffer buf = encoder.getOutputBuffer(remainingIdx);
                            buf.position(bufferInfo.offset);
                            buf.limit(bufferInfo.offset + bufferInfo.size);
                            bufferInfo.presentationTimeUs = presentationTimeUs;
                            muxer.writeSampleData(muxVideoTrack, buf, bufferInfo);
                            presentationTimeUs += frameDurationUs;
                        }
                        encoder.releaseOutputBuffer(remainingIdx, false);
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                            break;
                    }
                    remainingIdx = encoder.dequeueOutputBuffer(bufferInfo, 10_000);
                }

                android.util.Log.d("VideoGenerator", "Video created: " + outputFile.getPath());

                if (callback != null) callback.onProgress(100);
                if (callback != null) callback.onFinished(outputFile);

            } catch (Exception e) {
                android.util.Log.e("VideoGenerator", "FAILED: " + e.getMessage(), e);
                if (callback != null) callback.onError(e);
            } finally {
                try { if (encoder        != null) { encoder.stop();       encoder.release();       } } catch (Exception ignored) {}
                try { if (muxer          != null) { muxer.stop();         muxer.release();         } } catch (Exception ignored) {}
                try { if (audioExtractor != null) { audioExtractor.release();                      } } catch (Exception ignored) {}
                handlerThread.quitSafely();
            }
        }).start();
    }
    private static void applyFontToViewGroup(Context context, ViewGroup vg) {
        if (vg == null) return;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextView) {
                applyFont(context, (TextView) child);
            } else if (child instanceof ViewGroup) {
                applyFontToViewGroup(context, (ViewGroup) child);
            }
        }
    }

    private static void applyFont(Context context, TextView tv) {
        if (tv == null) return;
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String font = prefs.getString("font_family", "default");

        Typeface typeface;
        switch (font) {
            case "serif":
                typeface = Typeface.SERIF;
                break;
            case "monospace":
                typeface = Typeface.MONOSPACE;
                break;
            case "sans-serif":
                typeface = Typeface.SANS_SERIF;
                break;
            default:
                typeface = Typeface.DEFAULT;
                break;
        }

        int existingStyle = tv.getTypeface() != null
                ? tv.getTypeface().getStyle()
                : Typeface.NORMAL;
        tv.setTypeface(typeface, existingStyle);
    }
}