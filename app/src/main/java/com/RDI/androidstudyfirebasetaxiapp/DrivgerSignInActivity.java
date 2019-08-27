package com.RDI.androidstudyfirebasetaxiapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class DrivgerSignInActivity extends AppCompatActivity {

    private TextInputLayout textInputEmail;
    private TextInputLayout textInputName;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputConfirmPassword;

    private Button loginSignUnButton;
    private TextView toggleLoginSignUpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivger_sign_in);

        textInputEmail = findViewById(R.id.textInputEmail);
        textInputName = findViewById(R.id.textInputName);
        textInputPassword = findViewById(R.id.textInputPassword);
        textInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);
        loginSignUnButton = findViewById(R.id.loginSignUnButton);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);


    }

    private boolean validateEmail() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            textInputEmail.setError("Пожалуйста введите ваш email");
            return false;
        } else {
            textInputEmail.setError("");
            return true;
        }
    }

    private boolean validateName() {
        String nameInput = textInputName.getEditText().getText().toString().trim();
        if (nameInput.isEmpty()) {
            textInputName.setError("Пожалуйста введите ваше имя");
            return false;
        } else if (nameInput.length() > 15) {
            textInputName.setError("Длинна имени не должна превышать 15 символов");
            return false;
        } else {
            textInputName.setError("");
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        String confermPasswordInput = textInputConfirmPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Пожалуйста введите ваше имя");
            return false;
        } else if (passwordInput.length() < 7) {
            textInputPassword.setError("Пароль должен быть больше 6 символов");
            return false;
        } else if (!passwordInput.equals(confermPasswordInput)) {
            textInputConfirmPassword.setError("Пароли должны совпвдать");
            return false;
        } else {
            textInputPassword.setError("");
            return true;
        }
    }

    public void loginSignUpUser(View view) {
        if (!validateEmail() && !validateName() && !validatePassword()) {
            return;
        }

        String userInput = "Email: " + textInputEmail.getEditText().getText().toString().trim() +
                "/n" + "Имя: " + textInputName.getEditText().getText().toString().trim();

    }

    public void togglLoginSignUpUser(View view) {
    }
}
