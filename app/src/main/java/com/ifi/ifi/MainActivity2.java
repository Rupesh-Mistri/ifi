package com.ifi.ifi;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    private static final int FILE_REQUEST_CODE = 101;

    private LinearLayout participantsContainer;
    private LinearLayout photosContainer;
    private View currentFilePickerRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });

        participantsContainer = findViewById(R.id.participants_container);
        photosContainer = findViewById(R.id.photos_container);

        addParticipantRow(null);
        addPhotoRow(null);
    }

    // Add a participant input row
    public void addParticipantRow(View view) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setPadding(0, 8, 0, 8);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Enter name");
        nameInput.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button removeBtn = createButton("Remove", "#e74c3c", v -> participantsContainer.removeView(row));
        Button addBtn = createButton("Add", "#3498db", v -> addParticipantRow(null));

        row.addView(nameInput);
        row.addView(removeBtn);
        row.addView(addBtn);

        participantsContainer.addView(row);
    }

    // Add a photo input row
    public void addPhotoRow(View view) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setPadding(0, 8, 0, 8);

        Button chooseBtn = new Button(this);
        chooseBtn.setText("Choose File");
        chooseBtn.setOnClickListener(v -> {
            currentFilePickerRow = row;
            pickFile();
        });

        TextView fileName = new TextView(this);
        fileName.setText("No file chosen");
        fileName.setPadding(16, 0, 16, 0);

        Button removeBtn = createButton("Remove", "#e74c3c", v -> photosContainer.removeView(row));
        Button addBtn = createButton("Add", "#3498db", v -> addPhotoRow(null));

        row.addView(chooseBtn);
        row.addView(fileName);
        row.addView(removeBtn);
        row.addView(addBtn);

        photosContainer.addView(row);
    }

    private Button createButton(String text, String color, View.OnClickListener onClick) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(color)));
        btn.setOnClickListener(onClick);
        return btn;
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Choose File"), FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String name = getFileName(uri);

            if (currentFilePickerRow != null && currentFilePickerRow instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) currentFilePickerRow;
                for (int i = 0; i < row.getChildCount(); i++) {
                    View child = row.getChildAt(i);
                    if (child instanceof TextView && !(child instanceof Button)) {
                        ((TextView) child).setText(name != null ? name : "File selected");
                        break;
                    }
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return result;
    }
}
