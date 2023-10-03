package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.utils.APIUtils;

public class APISetupActivity extends AppCompatActivity {

    private String apiToken = "";
    private String username = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APIUtils.hasApiBeenSetup(this)) {
            startActivity(new Intent(APISetupActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_api_setup);
        setup();
    }

    private void setup() {
        final AppCompatEditText apiTokenInput = findViewById(R.id.apiTokenInput);
        final AppCompatEditText usernameInput = findViewById(R.id.usernameInput);
        final AppCompatButton submitButton = findViewById(R.id.submitButton);

        apiTokenInput.setBackgroundColor(getResources().getColor(android.R.color.white));
        apiTokenInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                apiToken = charSequence.toString();
                updateButton(submitButton);
            }
        });

        usernameInput.setBackgroundColor(getResources().getColor(android.R.color.white));
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                username = charSequence.toString();
                updateButton(submitButton);
            }
        });

        submitButton.setOnClickListener((view) -> {
            APIUtils.setupApi(apiToken, username);
            startActivity(new Intent(APISetupActivity.this, MainActivity.class));
            finish();
        });
    }

    private void updateButton(final AppCompatButton button) {
        button.setEnabled(!apiToken.isEmpty() && !username.isEmpty());
    }
}
