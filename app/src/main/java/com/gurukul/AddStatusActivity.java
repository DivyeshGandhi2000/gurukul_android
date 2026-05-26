package com.gurukul;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddStatusActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_CODE = 100;

    private ImageView ivPreview;
    private TextInputEditText etName, etDescription, etDate;
    private MaterialAutoCompleteTextView spinnerType;
    private Button btnSelectImage, btnSave;
    private DatabaseHelper dbHelper;
    private String imagePath = "";

    // Add these lines
    private RadioGroup rgVisibility;
    private RadioButton rbHide, rbVisible;

    // Edit mode variables
    private boolean isEditMode = false;

    // Edit mode variables
    private int statusId = -1;
    private Status existingStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_add_status);
        View rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) {
            EdgeToEdgeHelper.applyWindowInsets(rootLayout);
        }
        dbHelper = new DatabaseHelper(this);

        ivPreview = findViewById(R.id.ivPreview);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        spinnerType = findViewById(R.id.spinnerType);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        rgVisibility = findViewById(R.id.HideVisibileGroup);
        rbHide = findViewById(R.id.Hide);
        rbVisible = findViewById(R.id.Visible);

        setupSpinner();
        setupDatePicker();

        // Check if we're in edit mode
        checkEditMode();

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> saveStatus());
    }

    /**
     * Check if activity was launched in edit mode and load existing data
     */
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent.hasExtra("STATUS_ID")) {
            isEditMode = true;
            statusId = intent.getIntExtra("STATUS_ID", -1);

            existingStatus = dbHelper.getStatusById(statusId);

            if (existingStatus != null) {
                loadExistingData(existingStatus);

                btnSave.setText("Update");

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("स्टेटस संपादित करें");
                }
            } else {
                Toast.makeText(this, "Error loading status", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Load existing status data into form fields
     */
    private void loadExistingData(Status status) {
        // Load name
        etName.setText(status.getName());

        // Load description
        etDescription.setText(status.getDescription());

        // Load date
        etDate.setText(status.getDate());

        // Load type
        spinnerType.setText(status.getType(), false);

        // Load visibility state - ADD THIS
        if (status.isVisible()) {
            rbVisible.setChecked(true);
        } else {
            rbHide.setChecked(true);
        }

        // Load image
        imagePath = status.getImagePath();
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ivPreview.setImageBitmap(bitmap);
        }
    }

    private void setupSpinner() {
        String[] types = {"जन्मदिन के अवसर पर", "विवाह वर्षगांठ के अवसर पर", "पुण्यतिथि के अवसर पर","स्थाई शान्तिधारा एवं पूजन", "अन्य"};
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        spinnerType.setOnItemClickListener((parent, view, position, id) -> {
            updateOccasionUI(types[position]);
        });
    }

    private void updateOccasionUI(String type) {
    }

    private void setupDatePicker() {
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            // If editing, parse existing date to set initial calendar value
            if (isEditMode && !etDate.getText().toString().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    calendar.setTime(sdf.parse(etDate.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddStatusActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void selectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivPreview.setImageBitmap(bitmap);
                imagePath = saveImageToInternalStorage(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        File directory = getDir("images", MODE_PRIVATE);
        File file = new File(directory, "status_" + System.currentTimeMillis() + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveStatus() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String type = spinnerType.getText().toString().trim();

        // Add this line to get visibility
        boolean isVisible = rbVisible.isChecked();

        if (name.isEmpty() || description.isEmpty() || date.isEmpty() || type.isEmpty() || imagePath.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            // Update existing status - ADD isVisible parameter
            boolean result = dbHelper.updateStatus(statusId, imagePath, name, description, date, type, isVisible);
            if (result) {
                Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insert new status - ADD isVisible parameter
            long result = dbHelper.insertStatus(imagePath, name, description, date, type, isVisible);
            if (result != -1) {
                Toast.makeText(this, "Status added successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error adding status", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            }
        }
    }
}