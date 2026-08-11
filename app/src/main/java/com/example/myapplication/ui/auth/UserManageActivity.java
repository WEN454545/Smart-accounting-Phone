package com.example.myapplication.ui.auth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.File;
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

        findViewById(R.id.btn_add_user).setOnClickListener(v -> showAddUserDialog());
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etPassword = dialogView.findViewById(R.id.et_password);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        tvTitle.setText("\u6DFB\u52A0\u7528\u6237");

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .setPositiveButton("\u6DFB\u52A0", (d, which) -> {
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if (username.isEmpty()) {
                        Toast.makeText(this, "\u7528\u6237\u540D\u4E0D\u80FD\u4E3A\u7A7A", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(this, "\u5BC6\u7801\u4E0D\u80FD\u4E3A\u7A7A", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        User existingUser = repository.getUserByUsername(username);
                        if (existingUser != null) {
                            runOnUiThread(() -> Toast.makeText(this, "\u7528\u6237\u540D\u5DF2\u5B58\u5728", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        User newUser = new User(username, password, false, System.currentTimeMillis());
                        repository.insertUser(newUser, userId -> {
                            if (userId > 0) {
                                runOnUiThread(() -> Toast.makeText(this, "\u6DFB\u52A0\u6210\u529F", Toast.LENGTH_SHORT).show());
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, "\u6DFB\u52A0\u5931\u8D25", Toast.LENGTH_SHORT).show());
                            }
                        });
                    }).start();
                })
                .setNegativeButton("\u53D6\u6D88", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_user_dialog);
        });
        dialog.show();
    }

    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etPassword = dialogView.findViewById(R.id.et_password);

        etUsername.setText(user.getUsername());
        etPassword.setText(user.getPassword());

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        tvTitle.setText("\u7F16\u8F91\u7528\u6237");

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .setPositiveButton("\u4FDD\u5B58", (d, which) -> {
                    String newUsername = etUsername.getText().toString().trim();
                    String newPassword = etPassword.getText().toString().trim();

                    if (newUsername.isEmpty()) {
                        Toast.makeText(this, "\u7528\u6237\u540D\u4E0D\u80FD\u4E3A\u7A7A", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPassword.isEmpty()) {
                        Toast.makeText(this, "\u5BC6\u7801\u4E0D\u80FD\u4E3A\u7A7A", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        User existingUser = repository.getUserByUsername(newUsername);
                        if (existingUser != null && existingUser.getId() != user.getId()) {
                            runOnUiThread(() -> Toast.makeText(this, "\u7528\u6237\u540D\u5DF2\u5B58\u5728", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        user.setUsername(newUsername);
                        user.setPassword(newPassword);
                        repository.updateUser(user);

                        runOnUiThread(() -> Toast.makeText(this, "\u4FEE\u6539\u6210\u529F", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton("\u53D6\u6D88", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_user_dialog);
        });
        dialog.show();
    }

    private void showDeleteConfirmDialog(User user) {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_RoundedDialog)
                .setTitle("\u5220\u9664\u7528\u6237")
                .setMessage("\u786E\u5B9A\u8981\u5220\u9664\u7528\u6237 " + user.getUsername() + " \u5417\uFF1F\n\u8BE5\u7528\u6237\u7684\u6240\u6709\u8D26\u5355\u6570\u636E\u4E5F\u5C06\u88AB\u5220\u9664\u3002")
                .setPositiveButton("\u5220\u9664", (d, which) -> {
                    repository.deleteUser(user.getId());
                    Toast.makeText(this, "\u7528\u6237\u5DF2\u5220\u9664", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("\u53D6\u6D88", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_user_dialog);
        });
        dialog.show();
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

            // Avatar: load from profile avatar file, fallback to initial
            File avatarFile = new File(getFilesDir(), "avatars/user_" + user.getId() + ".jpg");
            if (avatarFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
                if (bitmap != null) {
                    holder.ivUserAvatar.setImageBitmap(bitmap);
                    holder.ivUserAvatar.setVisibility(View.VISIBLE);
                    holder.tvAvatarInitial.setVisibility(View.GONE);
                } else {
                    holder.ivUserAvatar.setVisibility(View.GONE);
                    holder.tvAvatarInitial.setVisibility(View.VISIBLE);
                    String initial = user.getUsername().isEmpty() ? "?" : user.getUsername().substring(0, 1);
                    holder.tvAvatarInitial.setText(initial);
                }
            } else {
                holder.ivUserAvatar.setVisibility(View.GONE);
                holder.tvAvatarInitial.setVisibility(View.VISIBLE);
                String initial = user.getUsername().isEmpty() ? "?" : user.getUsername().substring(0, 1);
                holder.tvAvatarInitial.setText(initial);
            }

            // Role badge
            if (user.isAdmin()) {
                holder.tvRole.setText("\u7BA1\u7406\u5458");
                holder.tvRole.setTextColor(getResources().getColor(R.color.profile_icon_fg_green));
                holder.tvRole.setBackgroundResource(R.drawable.bg_user_role_admin);
            } else {
                holder.tvRole.setText("\u666E\u901A\u7528\u6237");
                holder.tvRole.setTextColor(getResources().getColor(R.color.profile_icon_fg_slate));
                holder.tvRole.setBackgroundResource(R.drawable.bg_user_role_normal);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            holder.tvCreatedAt.setText("\u521B\u5EFA\u4E8E " + sdf.format(new Date(user.getCreatedAt())));

            // Click to edit
            holder.itemView.setOnClickListener(v -> showEditUserDialog(user));
            holder.ivEdit.setOnClickListener(v -> showEditUserDialog(user));

            // Long click to delete
            holder.itemView.setOnLongClickListener(v -> {
                if (!user.isAdmin()) {
                    showDeleteConfirmDialog(user);
                } else {
                    Toast.makeText(UserManageActivity.this, "\u4E0D\u80FD\u5220\u9664\u7BA1\u7406\u5458\u8D26\u6237", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername, tvRole, tvCreatedAt, tvAvatarInitial;
            ImageView ivEdit, ivUserAvatar;

            UserViewHolder(View itemView) {
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tv_username);
                tvRole = itemView.findViewById(R.id.tv_role);
                tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
                tvAvatarInitial = itemView.findViewById(R.id.tv_avatar_initial);
                ivEdit = itemView.findViewById(R.id.iv_edit);
                ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            }
        }
    }
}
