package com.ifi.ifi;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private static final int SELECT_PHOTO = 1;
    private TextView latestImagePlaceholder;
    private final Map<String, View> formInputMap = new HashMap<>();
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        linearLayout = findViewById(R.id.dynamic_form_layout);

        // Sample simplified structure
        List<Map<String, Object>> formFields = getFormFields();

        for (Map<String, Object> field : formFields) {
            String fieldName = (String) field.get("field");
            String fieldType = (String) field.get("type");

            TextView label = new TextView(this);
            label.setText(capitalize(fieldName.replace("_", " ")));
// Make text bold and slightly larger
            label.setTextSize(18); // Size in SP (scaled pixels)
            label.setTypeface(null, Typeface.BOLD); // Bold text
            linearLayout.addView(label);

            switch (fieldType) {
                case "text":
                case "number":
                    EditText editText = new EditText(this);
                    editText.setHint(capitalize(fieldName.replace("_", " ")));
                    if ("number".equals(fieldType)) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                    formInputMap.put(fieldName, editText);
                    editText.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    editText.setBackgroundResource(R.drawable.edit_text_border);
                    linearLayout.addView(editText);
                    break;

                case "textarea":
                    EditText multiLine = new EditText(this);
                    multiLine.setHint(capitalize(fieldName));
                    multiLine.setMinLines(3);
                    multiLine.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    multiLine.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    formInputMap.put(fieldName, multiLine);
                    multiLine.setBackgroundResource(R.drawable.edit_text_border);
                    linearLayout.addView(multiLine);
                    break;

                case "select":
                    Spinner spinner = new Spinner(this);
                    // Example static data; replace with real options if needed
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item,
                            new String[]{"1", "2"});
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    formInputMap.put(fieldName, spinner);
                    spinner.setBackgroundResource(R.drawable.edit_text_border);
                    linearLayout.addView(spinner);
                    break;

                case "image":
                    // You might want to add a button to pick an image
                    TextView imagePlaceholder = new TextView(this);
                    imagePlaceholder.setText("Choose Image: " + capitalize(fieldName));
                    imagePlaceholder.setBackgroundResource(R.drawable.edit_text_border);
                    imagePlaceholder.setPadding(20, 20, 20, 20);
                    latestImagePlaceholder = imagePlaceholder;
                    imagePlaceholder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_PHOTO);
                        }
                    });
                    linearLayout.addView(imagePlaceholder);

                    break;

                default:
                    break;
            }
        }
        Space space = new Space(this);
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                50 // height in pixels; adjust as needed
        );
        space.setLayoutParams(spaceParams);
        linearLayout.addView(space); // Add space to layout

        Button submitButton = new Button(this);
        submitButton.setText("Submit");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
// Optional styling
        submitButton.setPadding(30, 20, 30, 20);

        submitButton.setTextSize(18);
        submitButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        submitButton.setTextColor(getResources().getColor(android.R.color.white));

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonBody = new JSONObject();

                for (Map.Entry<String, View> entry : formInputMap.entrySet()) {
                    String key = entry.getKey();
                    View inputView = entry.getValue();

                    try {
                        if (inputView instanceof EditText) {
                            jsonBody.put(key, ((EditText) inputView).getText().toString());
                        } else if (inputView instanceof Spinner) {
                            jsonBody.put(key, ((Spinner) inputView).getSelectedItem().toString());

                        }
                        // Add image URI as string if present
                        if ("photo".equals(key) && selectedImageUri != null) {
                            jsonBody.put(key, selectedImageUri.toString()); // Optional: convert to base64 if backend expects image content
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("FormData", jsonBody.toString());
                    System.out.println(jsonBody.toString());
                   // Toast.makeText(MainActivity.this, "Submit clicked", Toast.LENGTH_SHORT).show();
                }

                // Make POST request
                String url = "https://demo.brpnn.net/api/personal-details/"; // Replace with actual URL
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        response -> {
                            Toast.makeText(MainActivity.this, "Submitted successfully", Toast.LENGTH_LONG).show();
                        },
                        error -> {

                                Log.e("VOLLEY_ERROR", "Error: " + error.toString());
                                if (error.networkResponse != null) {
                                    Log.e("VOLLEY_ERROR", "Status Code: " + error.networkResponse.statusCode);
                                    try {
                                        String body = new String(error.networkResponse.data, "UTF-8");
                                        Log.e("VOLLEY_ERROR", "Response Body: " + body);
                                    } catch (Exception e) {
                                        Log.e("VOLLEY_ERROR", "Failed to parse error body");
                                    }
                                }
                            Toast.makeText(MainActivity.this, "Submission failed", Toast.LENGTH_LONG).show();
                        });

                Volley.newRequestQueue(MainActivity.this).add(request);
            }
        });


// Add to layout
        linearLayout.addView(submitButton);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Uri of the selected image
            Uri selectedImageUri = data.getData();

            // TODO: Handle the selected image (e.g., show preview or save)
            //Toast.makeText(this, "Image selected: " + selectedImageUri.toString(), Toast.LENGTH_SHORT).show();
            selectedImageUri = data.getData();
            latestImagePlaceholder.setText(selectedImageUri.toString());

        }
    }


    private List<Map<String, Object>> getFormFields() {
        List<Map<String, Object>> fields = new ArrayList<>();

        fields.add(createField("name", "text"));
        fields.add(createField("photo", "image"));
        fields.add(createField("father_name", "text"));
        fields.add(createField("mother_name", "text"));
        fields.add(createField("mobile_number", "text"));
        fields.add(createField("aadhaar_number", "text"));
        fields.add(createField("state", "text"));
        fields.add(createField("district", "text"));
        fields.add(createField("block", "text"));
        fields.add(createField("village", "text"));
        fields.add(createField("gram_panchayat", "text"));
        fields.add(createField("nyay_panchayat", "text"));
        fields.add(createField("post_office", "text"));
        fields.add(createField("tehsil", "text"));
        fields.add(createField("pin_code", "text"));
        fields.add(createField("educational_qualification", "text"));
        fields.add(createField("geo_location", "text"));
        fields.add(createField("family_details", "textarea"));
        fields.add(createField("number_of_sons_married", "number"));
        fields.add(createField("number_of_sons_unmarried", "number"));
        fields.add(createField("number_of_daughters_married", "number"));
        fields.add(createField("number_of_daughters_unmarried", "number"));
        fields.add(createField("user", "select"));
        fields.add(createField("other_members", "textarea"));

        return fields;
    }

    private Map<String, Object> createField(String name, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("field", name);
        map.put("type", type);
        return map;
    }

    private String capitalize(String input) {
        if (input.length() == 0) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
