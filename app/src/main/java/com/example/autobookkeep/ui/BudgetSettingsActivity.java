package com.example.autobookkeep.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.entity.Budget;
import com.example.myapplication.data.repository.BillRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 月度预算设置页
 */
public class BudgetSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_settings);

        EditText etBudget = findViewById(R.id.et_budget);
        Button btnSave = findViewById(R.id.btn_save_budget);

        String ym = getYearMonth();
        BillRepository repo = MyApplication.getRepository();

        btnSave.setOnClickListener(v -> {
            String val = etBudget.getText().toString().trim();
            if (val.isEmpty()) {
                Toast.makeText(this, "请输入预算金额", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double amount = Double.parseDouble(val);
                if (amount <= 0) {
                    Toast.makeText(this, "金额必须大于0", Toast.LENGTH_SHORT).show();
                    return;
                }
                Budget b = new Budget(ym, amount, System.currentTimeMillis(), 1);
                repo.saveBudget(b);
                Toast.makeText(this, "预算已更新", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效金额", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getYearMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
        return sdf.format(new Date());
    }
}