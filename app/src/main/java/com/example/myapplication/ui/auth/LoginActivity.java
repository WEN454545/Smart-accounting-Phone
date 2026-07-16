package com.example.myapplication.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.User;
import com.example.myapplication.data.repository.BillRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private SessionManager sessionManager;
    private BillRepository repository;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ImageView ivLoginBg;
    private SharedPreferences profilePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        repository = MyApplication.getRepository();

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        ivLoginBg = findViewById(R.id.iv_login_bg);
        profilePrefs = getSharedPreferences("profile_settings", Context.MODE_PRIVATE);

        // Load login background
        loadLoginBackground();

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void loadLoginBackground() {
        String savedPath = profilePrefs.getString("login_bg_uri", null);
        if (savedPath != null) {
            try {
                if (savedPath.startsWith("content://")) {
                    Uri uri = Uri.parse(savedPath);
                    ivLoginBg.setImageURI(uri);
                } else {
                    ivLoginBg.setImageURI(Uri.fromFile(new java.io.File(savedPath)));
                }
                ivLoginBg.setVisibility(android.view.View.VISIBLE);
            } catch (Exception e) {
                ivLoginBg.setVisibility(android.view.View.GONE);
            }
        } else {
            ivLoginBg.setVisibility(android.view.View.GONE);
        }
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }

        executor.execute(() -> {
            User user = repository.login(username, password);
            runOnUiThread(() -> {
                if (user != null) {
                    sessionManager.createLoginSession(user.getId());
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }
        if (username.length() < 3) {
            etUsername.setError("用户名至少3个字符");
            return;
        }
        if (password.length() < 4) {
            etPassword.setError("密码至少4个字符");
            return;
        }

        executor.execute(() -> {
            User existing = repository.getUserByUsername(username);
            runOnUiThread(() -> {
                if (existing != null) {
                    Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
                } else {
                    User newUser = new User(username, password, false, System.currentTimeMillis());
                    repository.insertUser(newUser, userId -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                            etPassword.setText("");
                        });
                    });
                }
            });
        });
    }
}