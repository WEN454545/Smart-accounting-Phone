package com.example.myapplication.ui.stats;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.repository.BillRepository;
import com.example.myapplication.ui.adapter.CsvImportPreviewAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvImportPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_CSV_URI = "csv_uri";

    private RecyclerView recyclerPreview;
    private TextView tvTotalCount, tvSelectedCount, btnSelectAll, btnDeselectAll, btnImport, btnBack;
    private CsvImportPreviewAdapter adapter;
    private BillRepository repository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(com.example.myapplication.MyApplication.getThemeResId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv_import_preview);

        repository = MyApplication.getRepository();
        sessionManager = new SessionManager(this);

        recyclerPreview = findViewById(R.id.recycler_preview);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        btnSelectAll = findViewById(R.id.btn_select_all);
        btnDeselectAll = findViewById(R.id.btn_deselect_all);
        btnImport = findViewById(R.id.btn_import);
        btnBack = findViewById(R.id.btn_back);

        recyclerPreview.setLayoutManager(new LinearLayoutManager(this));

        Uri csvUri = getIntent().getParcelableExtra(EXTRA_CSV_URI);
        if (csvUri == null) {
            Toast.makeText(this, "未找到CSV文件", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        parseCsv(csvUri);

        btnBack.setOnClickListener(v -> finish());
        btnImport.setOnClickListener(v -> importSelectedBills());
        btnSelectAll.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.selectAll(true);
                updateCounts();
            }
        });
        btnDeselectAll.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.selectAll(false);
                updateCounts();
            }
        });
    }

    private void parseCsv(Uri uri) {
        new Thread(() -> {
            try {
                List<Bill> bills = new ArrayList<>();
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String dateStr = parts[0].trim();
                        String typeStr = parts[1].trim();
                        String category = parts[2].trim();
                        String amountStr = parts[3].trim();
                        String note = parts.length > 4 ? parts[4].trim() : "";

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                        Date date = sdf.parse(dateStr);
                        long timestamp = date != null ? date.getTime() : System.currentTimeMillis();

                        double amount = Double.parseDouble(amountStr);
                        if ("支出".equals(typeStr)) amount = -Math.abs(amount);

                        String dbCategory = "支出".equals(typeStr) ? "expense" : "income";

                        Bill bill = new Bill(category, amount, timestamp, "", note, "import", dbCategory, sessionManager.getUserId());
                        bills.add(bill);
                    }
                }
                reader.close();

                runOnUiThread(() -> {
                    if (bills.isEmpty()) {
                        Toast.makeText(this, "CSV文件无有效数据", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    adapter = new CsvImportPreviewAdapter(bills);
                    adapter.setOnSelectionChangedListener(() -> runOnUiThread(this::updateCounts));
                    recyclerPreview.setAdapter(adapter);
                    updateCounts();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void updateCounts() {
        if (adapter == null) return;
        tvTotalCount.setText("共 " + adapter.getItemCount() + " 条");
        tvSelectedCount.setText("已选 " + adapter.getSelectedCount() + " 条");
    }

    private void importSelectedBills() {
        if (adapter == null || adapter.getSelectedCount() == 0) {
            Toast.makeText(this, "请至少选择一条账单", Toast.LENGTH_SHORT).show();
            return;
        }

        btnImport.setEnabled(false);
        boolean[] selected = adapter.getSelected();
        List<Bill> allBills = adapter.getBills();

        new Thread(() -> {
            int imported = 0;
            for (int i = 0; i < allBills.size(); i++) {
                if (selected[i]) {
                    repository.insert(allBills.get(i));
                    imported++;
                }
            }
            final int count = imported;
            runOnUiThread(() -> {
                Toast.makeText(this, "成功导入 " + count + " 条账单", Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_OK);
                finish();
            });
        }).start();
    }
}