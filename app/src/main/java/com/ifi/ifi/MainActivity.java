package com.ifi.ifi;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private static final int SELECT_PHOTO = 1;
    private TextView latestImagePlaceholder;
    private final Map<String, View> formInputMap = new HashMap<>();
    private Uri selectedImageUri = null;
    private Space space;
    Button submitButton;
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

        fetchFormFields();


         space = new Space(this);
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                50
        );
        space.setLayoutParams(spaceParams);


         submitButton = new Button(this);
        submitButton.setText("Submit");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        submitButton.setPadding(30, 20, 30, 20);
        submitButton.setTextSize(18);
        submitButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        submitButton.setTextColor(getResources().getColor(android.R.color.white));

        submitButton.setOnClickListener(view -> {
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
                    if ("photo".equals(key) && selectedImageUri != null) {
                        jsonBody.put(key, selectedImageUri.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Log.e("FormData", jsonBody.toString());

            String url = "https://demo.brpnn.net/api/personal-details/";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> Toast.makeText(MainActivity.this, "Submitted successfully", Toast.LENGTH_LONG).show(),
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
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            latestImagePlaceholder.setText(selectedImageUri.toString());
        }
    }

    private void fetchFormFields() {
        String url = "https://demo.brpnn.net/api/personal-details/";
        Log.d("dsftesr","Calllllllled");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Log the full response for debugging
                        Log.d("RAW_RESPONSE", response.toString());

                        // Corrected key name: "formfields"
                        JSONArray fieldsArray = response.getJSONArray("formfields");

                        for (int i = 0; i < fieldsArray.length(); i++) {
                            JSONObject fieldObj = fieldsArray.getJSONObject(i);
                            String fieldName = fieldObj.getString("field");
                            String fieldType = fieldObj.getString("type");

                            addFieldToForm(fieldName, fieldType); // your method for dynamically building the UI
                        }
                        linearLayout.addView(space);
                        linearLayout.addView(submitButton);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON parse error", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    Log.e("FETCH_FIELDS", "Error fetching form fields: " + error.toString());
                    Toast.makeText(this, "Failed to fetch form fields", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void addFieldToForm(String fieldName, String fieldType) {
        TextView label = new TextView(this);
        label.setText(capitalize(fieldName.replace("_", " ")));
        label.setTextSize(18);
        label.setTypeface(null, Typeface.BOLD);
        linearLayout.addView(label);

        switch (fieldType) {
            case "text":
            case "number":
                EditText editText = new EditText(this);
                editText.setHint(capitalize(fieldName.replace("_", " ")));
                if ("number".equals(fieldType)) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                editText.setBackgroundResource(R.drawable.edit_text_border);
                formInputMap.put(fieldName, editText);
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
                multiLine.setBackgroundResource(R.drawable.edit_text_border);
                formInputMap.put(fieldName, multiLine);
                linearLayout.addView(multiLine);
                break;

            case "select":
                Spinner spinner = new Spinner(this);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        new String[]{"1", "2"});
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setBackgroundResource(R.drawable.edit_text_border);
                formInputMap.put(fieldName, spinner);
                linearLayout.addView(spinner);
                break;

            case "image":
                TextView imagePlaceholder = new TextView(this);
                imagePlaceholder.setText("Choose Image: " + capitalize(fieldName));
                imagePlaceholder.setBackgroundResource(R.drawable.edit_text_border);
                imagePlaceholder.setPadding(20, 20, 20, 20);
                latestImagePlaceholder = imagePlaceholder;
                imagePlaceholder.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_PHOTO);
                });
                linearLayout.addView(imagePlaceholder);
                break;

            default:
                break;
        }
    }

    private String capitalize(String input) {
        if (input.length() == 0) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public void openMainActivity2(View view) {
        Intent intent =new Intent(this, MainActivity2.class);
        startActivity(intent);
    }
}
