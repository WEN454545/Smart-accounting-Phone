package com.example.autobookkeep.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.Budget;
import com.example.myapplication.data.repository.BillRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monthly budget settings page.
 * Sets budget for this month and all future months (up to FUTURE_MONTHS).
 */
public class BudgetSettingsActivity extends AppCompatActivity {

    private static final int FUTURE_MONTHS = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_settings);

        EditText etBudget = findViewById(R.id.et_budget);
        Button btnSave = findViewById(R.id.btn_save_budget);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();
        BillRepository repo = MyApplication.getRepository();

        String ym = getYearMonth();
        repo.getBudget(ym, userId).observe(this, budget -> {
            if (budget != null && budget.getTotalBudget() > 0) {
                etBudget.setText(String.valueOf((int) budget.getTotalBudget()));
                etBudget.selectAll();
            }
        });

        btnSave.setOnClickListener(v -> {
            String val = etBudget.getText().toString().trim();
            if (val.isEmpty()) {
                Toast.makeText(this, "\u8BF7\u8F93\u5165\u9884\u7B97\u91D1\u989D", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double amount = Double.parseDouble(val);
                if (amount <= 0) {
                    Toast.makeText(this, "\u91D1\u989D\u5FC5\u987B\u5927\u4E8E0", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnSave.setEnabled(false);
                btnSave.setText("\u4FDD\u5B58\u4E2D...");
                ExecutorService executor = Executors.newSingleThreadExecutor();
                final double finalAmount = amount;
                executor.execute(() -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
                    int savedCount = 0;
                    for (int i = 0; i < FUTURE_MONTHS; i++) {
                        String yearMonth = sdf.format(cal.getTime());
                        Budget b = new Budget(yearMonth, finalAmount, System.currentTimeMillis(), userId);
                        repo.saveBudget(b);
                        savedCount++;
                        cal.add(Calendar.MONTH, 1);
                    }
                    final int count = savedCount;
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                "\u5DF2\u8BBE\u7F6E" + ym + "\u8D77" + count + "\u4E2A\u6708\u7684\u9884\u7B97",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "\u8BF7\u8F93\u5165\u6709\u6548\u91D1\u989D", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getYearMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
        return sdf.format(new Date());
    }
}
