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

import com.example.autobookkeep.util.CategoryManager;



import java.io.BufferedReader;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Date;

import java.util.HashMap;

import java.util.List;

import java.util.Locale;

import java.util.Map;



public class CsvImportPreviewActivity extends AppCompatActivity {



    public static final String EXTRA_CSV_URI = "csv_uri";



    /**

     * Built-in note keyword table for CSV import: {category, keyword...}.

     * A hit is only used when the mapped category exists in the current preset list.

     */

    private static final String[][] EXPENSE_KEYWORDS = {

            {"餐饮", "外卖", "早餐", "午餐", "晚餐", "餐厅", "就餐", "小吃", "奶茶", "咖啡", "美团", "饿了么", "肯德基", "麦当劳", "火锅", "烧烤"},

            {"交通", "打车", "滴滴", "地铁", "公交", "加油", "停车", "过路费", "出租车", "网约车", "火车", "高铁", "共享单车"},

            {"购物", "淘宝", "京东", "拼多多", "天猫", "超市", "网购", "商城", "百货", "日用品"},

            {"住房", "房租", "房贷", "物业", "水费", "电费", "燃气", "水电费"},

            {"旅行", "酒店", "机票", "民宿", "景点", "门票", "旅游", "度假"},

            {"通讯", "话费", "流量", "宽带", "网费", "手机充值"},

            {"娱乐", "电影", "游戏", "KTV", "演唱会", "密室", "桌游"},

            {"人情", "送礼", "份子钱", "礼金", "请客", "人情"},

            {"医疗", "医院", "药店", "挂号", "体检", "看病", "药品"},

            {"教育", "学费", "培训", "课程", "网课", "教材", "考试"},

            {"美容", "理发", "美发", "化妆", "护肤", "美甲"},

            {"宠物", "猫粮", "狗粮", "宠物", "猫砂"},

            {"运动", "健身", "游泳", "瑜伽", "跑步", "球馆"},

            {"数码", "充电器", "耳机", "电脑", "数码"},

            {"家居", "家具", "家电", "沙发", "装修"},

            {"服饰", "衣服", "服装", "鞋子", "外套", "牛仔裤"},

            {"礼物", "生日礼物", "纪念日"},

    };

    private static final String[][] INCOME_KEYWORDS = {

            {"转账", "转账", "收款", "打款", "汇款", "工资", "薪资", "报销"},

            {"红包", "红包", "收红包"},

            {"退款", "退款", "退货", "售后"},

    };



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

                // Encoding auto-detection: CSVs saved by Excel on Chinese Windows are GBK,
                // while our exports/templates are UTF-8. Try strict UTF-8 first and fall
                // back to GB18030 (superset of GBK) so Chinese notes never turn garbled.
                byte[] all = readAllBytes(is);
                is.close();
                String charset = isStrictUtf8(all) ? "UTF-8" : "GB18030";
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new java.io.ByteArrayInputStream(all), charset));
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



                        if (category.isEmpty()) {

                            category = inferCategory(note, dbCategory);

                        }



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



    /** Reads the whole stream into memory (CSV files are small). */
    private byte[] readAllBytes(InputStream is) throws java.io.IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }

    /** Strict UTF-8 test: any malformed byte sequence means the file is not UTF-8. */
    private boolean isStrictUtf8(byte[] data) {
        try {
            java.nio.charset.CharsetDecoder decoder = java.nio.charset.StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
            decoder.decode(java.nio.ByteBuffer.wrap(data));
            return true;
        } catch (java.nio.charset.CharacterCodingException e) {
            return false;
        }
    }

    /**
     * Infers a category for a bill whose CSV category column is empty.
     * Priority: built-in note keyword table (only if the mapped category is a current preset)
     * -> history vote by note keyword -> fallback "其他".
     * Must be called off the main thread (CSV parsing runs in a worker thread).
     */
    private String inferCategory(String note, String dbCategory) {
        String fallback = "其他";
        String n = note == null ? "" : note.trim().toLowerCase(Locale.CHINA);
        if (n.isEmpty()) return fallback;

        boolean isExpense = "expense".equals(dbCategory);
        List<String> presets = isExpense
                ? CategoryManager.getExpenseCategories(this)
                : CategoryManager.getIncomeCategories(this);

        // 1. Built-in keyword table
        String[][] table = isExpense ? EXPENSE_KEYWORDS : INCOME_KEYWORDS;
        for (String[] entry : table) {
            if (!presets.contains(entry[0])) continue;
            for (int i = 1; i < entry.length; i++) {
                if (n.contains(entry[i].toLowerCase(Locale.CHINA))) return entry[0];
            }
        }

        // 2. History vote by note keyword (long notes are truncated to keep LIKE useful)
        String keyword = n.length() > 12 ? n.substring(0, 12) : n;
        List<String> cats = repository.getCategoriesByNoteKeywordSync(
                sessionManager.getUserId(), dbCategory, keyword, 50);
        if (!cats.isEmpty()) {
            Map<String, Integer> votes = new HashMap<>();
            String mostRecent = null;
            for (String c : cats) {
                if (c == null || c.isEmpty()) continue;
                if (mostRecent == null) mostRecent = c;
                votes.put(c, votes.getOrDefault(c, 0) + 1);
            }
            String best = null;
            int max = 0;
            for (Map.Entry<String, Integer> e : votes.entrySet()) {
                if (e.getValue() > max) {
                    max = e.getValue();
                    best = e.getKey();
                }
            }
            if (best != null) return best;
            if (mostRecent != null) return mostRecent;
        }
        return fallback;
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