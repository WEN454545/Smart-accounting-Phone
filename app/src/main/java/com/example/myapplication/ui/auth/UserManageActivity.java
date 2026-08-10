package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.entity.User;
import com.example.myapplication.data.repository.BillRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserManageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private BillRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(com.example.myapplication.MyApplication.getThemeResId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        repository = MyApplication.getRepository();

        recyclerView = findViewById(R.id.recycler_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);

        LiveData<List<User>> users = repository.getAllUsers();
        users.observe(this, userList -> {
            if (userList != null) {
                adapter.setUsers(userList);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etPassword = dialogView.findViewById(R.id.et_password);

        etUsername.setText(user.getUsername());
        etPassword.setText(user.getPassword());

        new AlertDialog.Builder(this)
                .setTitle("编辑用户")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newUsername = etUsername.getText().toString().trim();
                    String newPassword = etPassword.getText().toString().trim();

                    if (newUsername.isEmpty()) {
                        Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPassword.isEmpty()) {
                        Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        User existingUser = repository.getUserByUsername(newUsername);
                        if (existingUser != null && existingUser.getId() != user.getId()) {
                            runOnUiThread(() -> Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        user.setUsername(newUsername);
                        user.setPassword(newPassword);
                        repository.updateUser(user);

                        runOnUiThread(() -> Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;

        public void setUsers(List<User> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvUsername.setText(user.getUsername());
            holder.tvRole.setText(user.isAdmin() ? "管理员" : "普通用户");
            holder.tvRole.setTextColor(user.isAdmin() ?
                    getResources().getColor(R.color.primary) :
                    getResources().getColor(R.color.muted));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            holder.tvCreatedAt.setText("创建于 " + sdf.format(new Date(user.getCreatedAt())));

            holder.itemView.setOnClickListener(v -> showEditUserDialog(user));

            holder.itemView.setOnLongClickListener(v -> {
                if (!user.isAdmin()) {
                    new AlertDialog.Builder(UserManageActivity.this)
                            .setTitle("删除用户")
                            .setMessage("确定要删除用户 " + user.getUsername() + " 吗？该用户的所有账单数据也将被删除。")
                            .setPositiveButton("删除", (dialog, which) -> {
                                repository.deleteUser(user.getId());
                                Toast.makeText(UserManageActivity.this, "用户已删除", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    Toast.makeText(UserManageActivity.this, "不能删除管理员账户", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername, tvRole, tvCreatedAt;

            UserViewHolder(View itemView) {
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tv_username);
                tvRole = itemView.findViewById(R.id.tv_role);
                tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            }
        }
    }
}